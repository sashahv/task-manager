package com.olekhv.taskmanager.team;

import com.olekhv.taskmanager.exception.NoPermissionException;
import com.olekhv.taskmanager.exception.RequestNotFoundException;
import com.olekhv.taskmanager.exception.TeamNotFoundException;
import com.olekhv.taskmanager.exception.UserAlreadyExistsException;
import com.olekhv.taskmanager.team.teamJoinRequest.TeamJoinRequest;
import com.olekhv.taskmanager.team.teamJoinRequest.TeamJoinRequestRepository;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import jakarta.persistence.NonUniqueResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamJoinRequestRepository teamJoinRequestRepository;

    public void createTeam(Team team,
                           String username) {
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " not found")
        );

        String generatedJoinCode = generateJoinCodeIfUnique();

        generateTeam(team, user, generatedJoinCode);
    }

    public String joinTeamByCode(String joinCode,
                                 String username) {
        User user = userRepository.findByEmail(username).get();
        Team team = teamRepository.findByJoinCode(joinCode).orElseThrow(
                () -> new TeamNotFoundException("Code " + joinCode + " is invalid")
        );

        checkRequest(user, team);
        return generateResultOfTryToJoin(joinCode, username, user, team);
    }

    public void addUserToTeam(String joinCode,
                              String username,
                              String teamAdminEmail) {
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " not found")
        );
        User teamAdmin = userRepository.findByEmail(teamAdminEmail).get();
        Team team = teamRepository.findByJoinCode(joinCode).orElseThrow(
                () -> new TeamNotFoundException("Code " + joinCode + " is invalid")
        );

        log.info(teamAdminEmail);
        log.info(String.valueOf(team.getAdmins()));

        if (!team.getAdmins().contains(teamAdmin)) {
            throw new NoPermissionException("You have no permission");
        }

        team.getMembers().add(user);
        team.setNumberOfMembers(team.getNumberOfMembers() + 1);

        checkIfRequestIsPresent(username, team);

        teamRepository.save(team);
    }
    public void deleteTeamJoinRequest(Long requestId) {
        TeamJoinRequest request = teamJoinRequestRepository.findById(requestId).get();
        teamJoinRequestRepository.delete(request);
    }

    private void createTeamJoinRequest(User user, Team team) {
        TeamJoinRequest teamJoinRequest = TeamJoinRequest.builder()
                .user(user)
                .team(team)
                .build();

        teamJoinRequestRepository.save(teamJoinRequest);
    }

    private String generateJoinCodeIfUnique() {
        String generatedJoinCode = null;
        boolean isJoinCodeAvailable = false;

        while (!isJoinCodeAvailable) {
            generatedJoinCode = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            isJoinCodeAvailable = joinCodeIsAvailable(generatedJoinCode);
        }
        return generatedJoinCode;
    }

    private void generateTeam(Team team, User user, String generatedJoinCode) {
        team.setJoinCode(generatedJoinCode);
        team.setOwner(user);
        team.getAdmins().add(user);
        team.getMembers().add(user);
        team.setNumberOfMembers(1);
        teamRepository.save(team);
    }

    private boolean joinCodeIsAvailable(String generatedJoinCode) {
        return teamRepository.findByJoinCode(generatedJoinCode).isEmpty();
    }

    private void checkRequest(User user, Team team) {
        if (team.getMembers().contains(user)) {
            throw new UserAlreadyExistsException("You are already in this team");
        }

        if (teamJoinRequestRepository.findByUserEmailAndTeamId(user.getEmail(), team.getId()).isPresent()) {
            throw new NonUniqueResultException("Request already exists");
        }
    }

    private String generateResultOfTryToJoin(String joinCode, String username, User user, Team team) {
        if (team.getType().equals(TeamType.PUBLIC)) {
            addUserToTeam(joinCode, username, team.getOwner().getEmail());
            return "join";
        } else {
            createTeamJoinRequest(user, team);
            return "request";
        }
    }

    private void checkIfRequestIsPresent(String username, Team team) {
        Optional<TeamJoinRequest> joinRequest = teamJoinRequestRepository
                .findByUserEmailAndTeamId(username, team.getId());
        joinRequest.ifPresent(teamJoinRequest -> deleteTeamJoinRequest(teamJoinRequest.getId()));
    }
}
