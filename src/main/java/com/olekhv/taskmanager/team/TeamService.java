package com.olekhv.taskmanager.team;

import com.olekhv.taskmanager.exception.NoPermissionException;
import com.olekhv.taskmanager.exception.TaskNotFoundException;
import com.olekhv.taskmanager.exception.TeamNotFoundException;
import com.olekhv.taskmanager.exception.UserAlreadyExistsException;
import com.olekhv.taskmanager.task.Task;
import com.olekhv.taskmanager.task.TaskProgress;
import com.olekhv.taskmanager.task.TaskRepository;
import com.olekhv.taskmanager.team.teamJoinRequest.TeamJoinRequest;
import com.olekhv.taskmanager.team.teamJoinRequest.TeamJoinRequestRepository;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import jakarta.persistence.NonUniqueResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamJoinRequestRepository teamJoinRequestRepository;
    private final TaskRepository taskRepository;

    public void createTeam(Team team,
                           String authUserEmail) {
        User authUser = userRepository.findByEmail(authUserEmail).orElseThrow(
                () -> new UsernameNotFoundException("User " + authUserEmail + " not found")
        );

        String generatedJoinCode = generateJoinCodeIfUnique();

        generateTeam(team, authUser, generatedJoinCode);
    }

    public void editTeamInformation(Long teamId,
                                    TeamDTO teamDTO) {
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException("Team not found")
        );

        team.setName(teamDTO.getName());
        team.setDescription(teamDTO.getDescription());
        team.setType(teamDTO.getType());

        teamRepository.save(team);
    }

    public String joinTeamByCode(String joinCode,
                                 String authUserEmail) {
        User authUser = userRepository.findByEmail(authUserEmail).get();
        Team team = teamRepository.findByJoinCode(joinCode).orElseThrow(
                () -> new TeamNotFoundException("Code " + joinCode + " is invalid")
        );

        checkRequest(authUser, team);
        return generateResultOfTryToJoin(joinCode, authUserEmail, authUser, team);
    }

    public void addUserToTeam(String joinCode,
                              String providedUserEmail,
                              String authUserEmail) {
        User authUser = userRepository.findByEmail(authUserEmail).get();

        User providedUser = userRepository.findByEmail(providedUserEmail).orElseThrow(
                () -> new UsernameNotFoundException("User " + providedUserEmail + " not found")
        );

        Team team = teamRepository.findByJoinCode(joinCode).orElseThrow(
                () -> new TeamNotFoundException("Code " + joinCode + " is invalid")
        );

        if (!team.getAdmins().contains(authUser)) {
            throw new NoPermissionException("You have no permission");
        }

        if (team.getMembers().contains(providedUser)) {
            throw new UserAlreadyExistsException("User is already in this team");
        }

        team.getMembers().add(providedUser);
        team.setNumberOfMembers(team.getNumberOfMembers() + 1);

        checkIfRequestIsPresent(providedUserEmail, team);

        teamRepository.save(team);
    }

    public void changeTeamMemberRole(String providedUserEmail,
                                     Long teamId,
                                     TeamRole teamRole,
                                     String authUserEmail) {
        User authUser = userRepository.findByEmail(authUserEmail).get();

        User providedUser = userRepository.findByEmail(providedUserEmail).orElseThrow(
                () -> new UsernameNotFoundException("User " + providedUserEmail + " not found")
        );
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException("Team not found")
        );

        checkAccess(authUser, providedUser, team);

        switch (teamRole) {
            case MEMBER:
                if (team.getAdmins().contains(providedUser)) {
                    team.getAdmins().remove(providedUser);
                    team.getMembers().add(providedUser);
                }
                break;
            case ADMIN:
                if (!team.getOwner().equals(authUser)) {
                    throw new NoPermissionException("You have no permission");
                }

                team.getAdmins().add(providedUser);
                break;
        }

        teamRepository.save(team);
    }

    public void removeTeamMember(String providedUserEmail,
                                 Long teamId,
                                 String authUserEmail) {
        User authUser = userRepository.findByEmail(authUserEmail).get();

        User providedUser = userRepository.findByEmail(providedUserEmail).orElseThrow(
                () -> new UsernameNotFoundException("User " + providedUserEmail + " not found")
        );
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException("Team not found")
        );

        checkAccess(authUser, providedUser, team);

        if (team.getOwner().equals(providedUser)) {
            throw new NoPermissionException("Owner can't be removed");
        }

        if ((team.getAdmins().contains(providedUser) || team.getOwner().equals(providedUser))
                && !team.getOwner().equals(authUser)) {
            throw new NoPermissionException("You have no permission to delete this member");
        }

        team.getMembers().remove(providedUser);
        teamRepository.save(team);
    }

    public void deleteTeamJoinRequest(Long requestId) {
        TeamJoinRequest request = teamJoinRequestRepository.findById(requestId).get();
        teamJoinRequestRepository.delete(request);
    }

    public void addTaskForTeamMember(Long teamId,
                                     Task task,
                                     String authUserEmail) {
        User authUser = userRepository.findByEmail(authUserEmail).get();

        User providedUser = task.getOwner();

        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException("Team not found")
        );

        checkAccess(authUser, providedUser, team);

        task.setOwner(providedUser);
        team.getTasks().add(task);

        teamRepository.save(team);
    }

    public List<Task> listAllTasksOfTeamMember(String providedUserEmail,
                                               Long teamId) {
        User providedUser = userRepository.findByEmail(providedUserEmail).orElseThrow(
                () -> new UsernameNotFoundException("User " + providedUserEmail + " not found")
        );

        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException("Team not found")
        );

        List<Task> tasks = team.getTasks().stream()
                .filter(task -> task.getOwner().equals(providedUser))
                .collect(Collectors.toList());;

        tasks.stream()
                .filter(task -> task.getToDateTime().isBefore(LocalDateTime.now()) || task.getToDateTime().isEqual(LocalDateTime.now()))
                .forEach(task -> changeTeamTaskProgress(task.getId(), TaskProgress.OVERDUE, providedUserEmail));

        sortListOfTasks(tasks);

        return tasks;
    }

    public void editTeamTask(Long taskId,
                             Task editedTask,
                             String authUserEmail) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        Team team = teamRepository.findByTaskId(taskId).orElseThrow(
                () -> new TeamNotFoundException("Team not found")
        );

        User authUser = userRepository.findByEmail(authUserEmail).get();

        if (!team.getAdmins().contains(authUser)) {
            throw new NoPermissionException("No permission");
        }

        task.setName(editedTask.getName());
        task.setDescription(editedTask.getDescription());
        task.setPriority(editedTask.getPriority());
        task.setFromDateTime(editedTask.getFromDateTime());
        task.setToDateTime(editedTask.getToDateTime());

        taskRepository.save(task);

    }

    public void deleteTaskFromTeamMember(String username,
                                         Long taskId) {
        User providedUser = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " not found")
        );

        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        Team team = teamRepository.findByTaskId(task.getId()).orElseThrow(
                () -> new TeamNotFoundException("Team not found")
        );

        checkAccess(providedUser, providedUser, team);

        task.setProgress(TaskProgress.CLOSED);
        taskRepository.save(task);
    }

    public void changeTeamTaskProgress(Long taskId,
                                       TaskProgress taskProgress,
                                       String authUserEmail) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        Team team = teamRepository.findByTaskId(taskId).orElseThrow(
                () -> new TeamNotFoundException("Team not found")
        );

        User authUser = userRepository.findByEmail(authUserEmail).get();

        if (!task.getOwner().equals(authUser) && !team.getAdmins().contains(authUser)) {
            throw new NoPermissionException("No permission");
        }

        task.setProgress(taskProgress);
        taskRepository.save(task);
    }

    private void checkAccess(User authUser, User providedUser, Team team) {
        if (!team.getMembers().contains(providedUser)) {
            throw new UsernameNotFoundException("User does not belong to this team");
        }

        if (!team.getAdmins().contains(authUser)) {
            throw new NoPermissionException("You have no permission");
        }
    }


    private void sortListOfTasks(List<Task> tasks) {
        tasks.sort(Comparator.comparing(
                        Task::getPriority).reversed()
                .thenComparing(
                        Task::getToDateTime).reversed()
                .thenComparing(
                        Task::getFromDateTime).reversed()
                .thenComparing(
                        Task::getProgress));
    }

    private void createTeamJoinRequest(User providedUser, Team team) {
        TeamJoinRequest teamJoinRequest = TeamJoinRequest.builder()
                .user(providedUser)
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

    private void generateTeam(Team team, User providedUser, String generatedJoinCode) {
        team.setJoinCode(generatedJoinCode);
        team.setOwner(providedUser);
        team.getAdmins().add(providedUser);
        team.getMembers().add(providedUser);
        team.setNumberOfMembers(1);
        teamRepository.save(team);
    }

    private boolean joinCodeIsAvailable(String generatedJoinCode) {
        return teamRepository.findByJoinCode(generatedJoinCode).isEmpty();
    }

    private void checkRequest(User providedUser, Team team) {
        if (team.getMembers().contains(providedUser)) {
            throw new UserAlreadyExistsException("You are already in this team");
        }

        if (teamJoinRequestRepository.findByUserEmailAndTeamId(providedUser.getEmail(), team.getId()).isPresent()) {
            throw new NonUniqueResultException("Request already exists");
        }
    }

    private String generateResultOfTryToJoin(String joinCode,
                                             String userEmail,
                                             User providedUser,
                                             Team team) {
        if (team.getType().equals(TeamType.PUBLIC)) {
            addUserToTeam(joinCode, userEmail, team.getOwner().getEmail());
            return "join";
        } else {
            createTeamJoinRequest(providedUser, team);
            return "request";
        }
    }

    private void checkIfRequestIsPresent(String userEmail,
                                         Team team) {
        Optional<TeamJoinRequest> joinRequest = teamJoinRequestRepository
                .findByUserEmailAndTeamId(userEmail, team.getId());
        joinRequest.ifPresent(teamJoinRequest -> deleteTeamJoinRequest(teamJoinRequest.getId()));
    }
}
