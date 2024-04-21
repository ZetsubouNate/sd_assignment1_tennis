package org.example.tennis_api.service;

import org.example.tennis_api.dto.match.MatchDTO;
import org.example.tennis_api.entity.Match;
import org.example.tennis_api.utilities.MatchExportStrategy;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchService {
    Match createMatch(MatchDTO matchDTO) throws Exception;
    Match registerPlayerToMatch(Integer matchId, Integer playerId) throws Exception;
    List<Match> findAllMatches();
    List<Match> findAllMatchesByRefereeId(Integer refereeId) throws Exception;
    Optional<Match> findMatchById(Integer matchId);
    void updateMatchScore(Integer matchId, Integer player1Score, Integer player2Score) throws Exception;
    Match updateMatch(MatchDTO matchDTO, Integer id) throws Exception;
    void deleteMatch(Integer matchId) throws Exception;
    List<Match> findMatches(LocalDate startDate, LocalDate endDate, String location, Integer refereeId, Integer playerId);
    void exportMatches(List<Match> matches, OutputStream outputStream, MatchExportStrategy strategy) throws IOException;
}



