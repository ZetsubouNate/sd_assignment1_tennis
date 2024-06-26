package org.example.tennis_api.service;

import org.example.tennis_api.dto.user.UserDTO;
import org.example.tennis_api.dto.user.UserSignInDTO;
import org.example.tennis_api.dto.user.UserSignUpDTO;
import org.example.tennis_api.dto.user.UserUpdateCredentialsDTO;
import org.example.tennis_api.entity.User;
import org.example.tennis_api.mapper.UserMapper;
import org.example.tennis_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    private void validateUserCredentials(String username, String name, String password, String email) {
        if (username == null || username.trim().isEmpty() ||
                name == null || name.trim().isEmpty() ||
                password == null || password.trim().isEmpty()
                || email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Username, name, email and password cannot be empty.");
        }
    }

    @Override
    public User registerUser(UserSignUpDTO userSignUpDTO) throws DataIntegrityViolationException {
        validateUserCredentials(userSignUpDTO.getUsername(), userSignUpDTO.getName(), userSignUpDTO.getPassword(), userSignUpDTO.getEmail());
        if (userRepository.findByUsername(userSignUpDTO.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("Username already exists.");
        }
        userSignUpDTO.setPassword(passwordEncoder.encode(userSignUpDTO.getPassword()));
        User user = userMapper.signUpDtoToEntity(userSignUpDTO);
        return userRepository.save(user);
    }

    @Override
    public User loginUser(UserSignInDTO userSignInDTO) throws NoSuchElementException, IllegalArgumentException {
        User user = userRepository.findByUsername(userSignInDTO.getUsername())
                .orElseThrow(() -> new NoSuchElementException("User not found."));
        if (!passwordEncoder.matches(userSignInDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password.");
        }
        return user;
    }

    @Override
    public User updateUserCredentials(UserUpdateCredentialsDTO userUpdateCredentialsDTO, Integer id) throws NoSuchElementException, IllegalArgumentException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));
        validateUserCredentials(userUpdateCredentialsDTO.getUsername(), userUpdateCredentialsDTO.getName(), userUpdateCredentialsDTO.getNewPassword(), userUpdateCredentialsDTO.getEmail());
        if (!passwordEncoder.matches(userUpdateCredentialsDTO.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password.");
        }

        Optional<User> existingUser = userRepository.findByUsername(userUpdateCredentialsDTO.getUsername());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Username already in use by another account.");
        }

        Optional<User> existingName = userRepository.findByName(userUpdateCredentialsDTO.getName());
        if (existingName.isPresent() && !existingName.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Name already in use by another account.");
        }

        user.setUsername(userUpdateCredentialsDTO.getUsername());
        user.setName(userUpdateCredentialsDTO.getName());
        user.setPassword(passwordEncoder.encode(userUpdateCredentialsDTO.getNewPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findUserByRole(String role) {
        return userRepository.findByUserType(role);
    }

    @Override
    public List<User> findRegisteredPlayers() {
        return userRepository.findByIsRegisteredInTournament(true);
    }

    @Override
    public User quitTournamentUser(Integer id) throws NoSuchElementException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));
        user.setIsRegisteredInTournament(false);
        user.setTournamentRegistrationStatus("NONE");
        return userRepository.save(user);
    }

    @Override
    public User requestTournamentRegistration(Integer id) throws NoSuchElementException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        if (user.getUserType().equals("player")) {
            user.setTournamentRegistrationStatus("PENDING");
            user.setIsRegisteredInTournament(false);

            List<User> admins = userRepository.findByUserType("administrator");
            List<String> adminEmails = admins.stream().map(User::getEmail).collect(Collectors.toList());
            emailService.notifyAdmins("New Tournament Registration Request",
                    "A new tournament registration request has been received from " + user.getName() + " (" + user.getUsername() + ").", adminEmails);
        }
        return userRepository.save(user);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User addUser(UserDTO userDTO) throws DataIntegrityViolationException {
        validateUserCredentials(userDTO.getUsername(), userDTO.getName(), userDTO.getPassword(), userDTO.getEmail());
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("Username already exists.");
        }
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User user = userMapper.toEntity(userDTO);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(UserDTO userDTO, Integer id) throws NoSuchElementException{
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));
        validateUserCredentials(userDTO.getUsername(), userDTO.getName(), userDTO.getPassword(), userDTO.getEmail());

        Optional<User> userWithSameUsername = userRepository.findByUsername(userDTO.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
            throw new IllegalArgumentException("Username already in use by another account.");
        }

        Optional<User> userWithSameName = userRepository.findByName(userDTO.getName());
        if (userWithSameName.isPresent() && !userWithSameName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Name already in use by another account.");
        }

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setName(userDTO.getName());
        existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setUserType(userDTO.getUserType());
        existingUser.setIsRegisteredInTournament(userDTO.getIsRegisteredInTournament());
        existingUser.setTournamentRegistrationStatus(userDTO.getTournamentRegistrationStatus());
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Integer userId) throws NoSuchElementException{
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found.");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<User> filterUsers(String name, String username, Boolean isCompeting) {
        List<User> users = userRepository.findByUserType("player");

        if (name != null && !name.isEmpty()) {
            users = users.stream()
                    .filter(user -> user.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (username != null && !username.isEmpty()) {
            users = users.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(username.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (isCompeting != null) {
            users = users.stream()
                    .filter(user -> user.getIsRegisteredInTournament() == isCompeting)
                    .collect(Collectors.toList());
        }

        return users;
    }

    @Override
    public User acceptTournamentRegistration(Integer id) throws NoSuchElementException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));
        user.setIsRegisteredInTournament(true);
        user.setTournamentRegistrationStatus("ACCEPTED");

        emailService.notifyUser(user.getEmail(), "Tournament Registration Accepted",
                "Dear " + user.getName() + ",\n\nYour registration for the tournament has been accepted. Congrats.\n");

        return userRepository.save(user);
    }

    @Override
    public User rejectTournamentRegistration(Integer id) throws NoSuchElementException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));
        user.setIsRegisteredInTournament(false);
        user.setTournamentRegistrationStatus("REJECTED");

        emailService.notifyUser(user.getEmail(), "Tournament Registration Rejected",
                "Dear " + user.getName() + ",\n\nYour registration for the tournament has been rejected. Sorry not sorry.\n");


        return userRepository.save(user);
    }

    @Override
    public List<User> findUserByStatus(String status) {
        return userRepository.findByTournamentRegistrationStatus(status);
    }
}
