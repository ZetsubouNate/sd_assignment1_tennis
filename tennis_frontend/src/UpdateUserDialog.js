import React, { useState, useEffect } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    TextField,
    Button,
    DialogActions,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Alert,
} from '@mui/material';
import axios from 'axios';

const UpdateUserDialog = ({ open, handleClose, userId }) => {
    const [defaultUser, setDefaultUser] = useState(null);
    const [username, setUsername] = useState('');
    const [name, setName] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('');
    const [roles] = useState(['referee', 'player', 'administrator']);
    const [error, setError] = useState('');

    useEffect(() => {
        if (open) {
            const fetchUserDetails = async () => {
                try {
                    const response = await axios.get(`http://localhost:8081/api/user/${userId}`);
                    const user = response.data;
                    setDefaultUser(user);

                    setUsername(user.username);
                    setName(user.name);
                    setPassword(user.password);
                    setRole(user.userType);
                } catch (err) {
                    setError(err.response?.data || 'Failed to fetch user details.');
                }
            };

            fetchUserDetails();
        } else {
            setError('');
        }
    }, [open, userId]);

    const handleUpdate = async () => {
        try {
            const userDTO = {
                username,
                name,
                password,
                userType: role,
            };

            await axios.put(`http://localhost:8081/api/user/${userId}`, userDTO);
            handleClose(true);
        } catch (err) {
            setError(err.response?.data || 'Failed to update user.');
        }
    };

    const handleReset = () => {
        if (defaultUser) {
            setUsername(defaultUser.username);
            setName(defaultUser.name);
            setPassword(defaultUser.password);
            setRole(defaultUser.userType);
        }
    };

    return (
        <Dialog
            open={open}
            onClose={() => { handleClose(false); handleReset(); }}
            sx={{ '& .MuiPaper-root': { backgroundColor: '#f1f8e9' } }}
        >
            <DialogTitle>Update User</DialogTitle>
            <DialogContent>
                {error && (
                    <Alert severity="error" style={{ backgroundColor: '#FFF6EA', marginBottom: '5px' }}>{error}</Alert>
                )}
                <TextField
                    autoFocus
                    margin="dense"
                    label="Username"
                    required
                    type="text"
                    fullWidth
                    variant="outlined"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <TextField
                    margin="dense"
                    label="Name"
                    required
                    type="text"
                    fullWidth
                    variant="outlined"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
                <TextField
                    margin="dense"
                    label="Password"
                    type="password"
                    required
                    fullWidth
                    variant="outlined"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />

                <FormControl fullWidth margin="dense">
                    <InputLabel>Role</InputLabel>
                    <Select
                        value={role}
                        label="Role"
                        required
                        onChange={(e) => setRole(e.target.value)}
                    >
                        <MenuItem value="">
                            <em>None</em>
                        </MenuItem>
                        {roles.map((r) => (
                            <MenuItem key={r} value={r}>
                                {r}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
            </DialogContent>

            <DialogActions>
                <Button onClick={() => handleClose(false)} color="secondary">
                    Cancel
                </Button>
                <Button onClick={handleUpdate} color="primary">
                    Update
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default UpdateUserDialog;