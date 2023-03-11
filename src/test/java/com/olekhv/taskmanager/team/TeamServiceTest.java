package com.olekhv.taskmanager.team;

import com.olekhv.taskmanager.exception.NoPermissionException;
import com.olekhv.taskmanager.exception.TeamNotFoundException;
import com.olekhv.taskmanager.exception.UserAlreadyExistsException;
import com.olekhv.taskmanager.task.Task;
import com.olekhv.taskmanager.task.TaskProgress;
import com.olekhv.taskmanager.task.TaskRepository;
import com.olekhv.taskmanager.team.teamJoinRequest.TeamJoinRequest;
import com.olekhv.taskmanager.team.teamJoinRequest.TeamJoinRequestRepository;
import com.olekhv.taskmanager.user.Role;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import jakarta.persistence.NonUniqueResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TeamServiceTest {

    @Autowired
    private TeamService teamService;

    @MockBean
    private TeamRepository teamRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private TeamJoinRequestRepository teamJoinRequestRepository;

    private User user;
    private Team team;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .email("testUser@gmail.com")
                .build();

        team = Team.builder()
                .id(1L)
                .name("TestTeam")
                .owner(user)
                .joinCode("Abc123")
                .admins(new ArrayList<>())
                .members(new ArrayList<>())
                .numberOfMembers(1)
                .tasks(new ArrayList<>())
                .build();

        team.getMembers().add(user);
        team.getAdmins().add(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(teamRepository.findByJoinCode("Abc123")).thenReturn(Optional.of(team));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.findByTaskId(1L)).thenReturn(Optional.of(team));
    }

    @Test
    void should_create_new_team(){
        team.getMembers().remove(user);
        team.getAdmins().remove(user);

        team.setType(TeamType.PUBLIC);
        teamService.createTeam(team, user.getEmail());

        verify(teamRepository, times(1)).save(team);

        assertEquals(6, team.getJoinCode().length());
        assertEquals(1, team.getMembers().size());
        assertEquals(1, team.getAdmins().size());
    }

    @Test
    void should_add_task_for_member(){
        Task task = Task.builder()
                .name("TeamTask")
                .owner(user)
                .build();

        teamService.addTaskForTeamMember( 1L, task, user.getEmail());
        verify(teamRepository, times(1)).save(team);
        assertEquals(1, team.getTasks().size());
        assertEquals("TeamTask", team.getTasks().get(0).getName());
        assertEquals(user, team.getTasks().get(0).getOwner());
    }

    @Test
    void should_delete_task_from_member(){
        Task task = Task.builder()
                .id(1L)
                .name("TeamTask")
                .owner(user)
                .build();

        team.getTasks().add(task);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(teamRepository.findByTaskId(1L)).thenReturn(Optional.of(team));

        teamService.deleteTaskFromTeamMember(user.getEmail(), 1L);
        verify(taskRepository, times(1)).save(task);

        assertEquals(TaskProgress.CLOSED, task.getProgress());
    }

    @Test
    void should_throw_exception_if_user_try_to_delete_users_task(){
        team.getAdmins().remove(user);

        Task task = Task.builder()
                .id(1L)
                .name("TeamTask")
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(NoPermissionException.class, ()->
                teamService.deleteTaskFromTeamMember(user.getEmail(), task.getId()));
    }

    @Test
    void should_edit_team_information(){
        team.setType(TeamType.PUBLIC);

        TeamDTO teamDTO = TeamDTO.builder()
                .name("EditedTeam")
                .description("Edited")
                .type(TeamType.PRIVATE)
                .build();

        teamService.editTeamInformation(1L, teamDTO);

        verify(teamRepository, times(1)).save(team);

        assertEquals("EditedTeam", team.getName());
        assertEquals("Edited", team.getDescription());
        assertEquals(TeamType.PRIVATE, team.getType());
    }

    @Test
    @DisplayName("If generated join code is present generate new one")
    void should_generate_new_joinCode_if_present_is_not_available_and_create_team_with_new_code(){
        Team secondTeam = Team.builder()
                .name("TestSecondTeam")
                .owner(user)
                .joinCode("Abc123")
                .admins(new ArrayList<>())
                .members(new ArrayList<>())
                .numberOfMembers(1)
                .build();

        teamService.createTeam(team, user.getEmail());

        assertNotEquals(team.getJoinCode(), secondTeam.getJoinCode());
    }

    @Test
    void should_add_user_to_public_team_by_join_code(){
        User secondUser = User.builder()
                .firstName("Test")
                .lastName("SecondUser")
                .email("testSecondUser@gmail.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(secondUser.getEmail())).thenReturn(Optional.of(secondUser));

        team.setType(TeamType.PUBLIC);

        teamService.joinTeamByCode("Abc123", secondUser.getEmail());

        verify(teamRepository, times(1)).save(team);

        assertThat(team.getMembers().contains(secondUser)).isTrue();
        assertEquals(2, team.getNumberOfMembers());
        assertEquals(2, team.getMembers().size());
    }

    @Test
    void should_create_invite_request_for_private_team(){
        User secondUser = User.builder()
                .firstName("Test")
                .lastName("SecondUser")
                .email("testSecondUser@gmail.com")
                .build();

        TeamJoinRequest teamJoinRequest = TeamJoinRequest
                .builder()
                .user(secondUser)
                .team(team)
                .build();

        when(userRepository.findByEmail(secondUser.getEmail())).thenReturn(Optional.of(secondUser));

        team.setType(TeamType.PRIVATE);

        teamService.joinTeamByCode("Abc123", secondUser.getEmail());

        verify(teamJoinRequestRepository, times(1)).save(teamJoinRequest);
    }

    @Test
    void should_throw_exception_if_user_already_belongs_to_team_and_try_to_join(){
        assertThrows(UserAlreadyExistsException.class, ()->
                teamService.joinTeamByCode("Abc123", user.getEmail()));
    }

    @Test
    void should_throw_exception_if_request_already_exists(){
        team.getMembers().remove(user);
        team.getAdmins().remove(user);

        TeamJoinRequest teamJoinRequest = TeamJoinRequest
                .builder()
                .user(user)
                .team(team)
                .build();

        when(teamJoinRequestRepository.findByUserEmailAndTeamId(anyString(), anyLong()))
                .thenReturn(Optional.of(teamJoinRequest));

        assertThrows(NonUniqueResultException.class, ()->
                teamService.joinTeamByCode("Abc123", user.getEmail()));
    }

    @Test
    void should_throw_exception_if_code_invalid(){
        assertThrows(TeamNotFoundException.class, () ->
                teamService.joinTeamByCode("Cba321", user.getEmail())
        );
    }

    @Test
    void should_delete_join_request(){
        TeamJoinRequest teamJoinRequest = TeamJoinRequest
                .builder()
                .id(1L)
                .user(user)
                .team(team)
                .build();

        when(teamJoinRequestRepository.findById(teamJoinRequest.getId())).thenReturn(Optional.of(teamJoinRequest));

        teamService.deleteTeamJoinRequest(teamJoinRequest.getId());

        verify(teamJoinRequestRepository, times(1)).delete(teamJoinRequest);
    }

    @Test
    void should_remove_member(){
        User secondUser = User.builder()
                .firstName("Test")
                .lastName("SecondUser")
                .email("testSecondUser@gmail.com")
                .build();

        when(userRepository.findByEmail(secondUser.getEmail())).thenReturn(Optional.of(secondUser));

        team.getMembers().add(secondUser);

        teamService.removeTeamMember(secondUser.getEmail(), 1L, user.getEmail());
    }

    @Test
    void should_throw_exception_if_try_to_remove_owner(){
        assertThrows(NoPermissionException.class, () ->
                teamService.removeTeamMember(user.getEmail(), 1L, user.getEmail())
        );
    }

    @Test
    void should_throw_exception_if_admin_try_to_remove_other_admin(){
        User teamAdmin = User.builder()
                .firstName("Test")
                .lastName("Admin")
                .email("testAdmin@gmail.com")
                .build();

        when(userRepository.findByEmail(teamAdmin.getEmail())).thenReturn(Optional.of(teamAdmin));

        team.getMembers().add(teamAdmin);
        team.getAdmins().add(teamAdmin);

        assertThrows(NoPermissionException.class, () ->
                teamService.removeTeamMember(teamAdmin.getEmail(), 1L, teamAdmin.getEmail())
        );
    }

    @Test
    void should_throw_exception_if_user_try_to_remove_somebody(){
        User secondUser = User.builder()
                .firstName("Test")
                .lastName("SecondUser")
                .email("testSecondUser@gmail.com")
                .build();

        when(userRepository.findByEmail(secondUser.getEmail())).thenReturn(Optional.of(secondUser));

        team.getMembers().add(secondUser);

        assertThrows(NoPermissionException.class, () ->
                teamService.removeTeamMember(secondUser.getEmail(), 1L, secondUser.getEmail())
        );
    }

    @Test
    void should_change_member_role_to_admin_if_owner(){
        User secondUser = User.builder()
                .firstName("Test")
                .lastName("Second")
                .email("testSecond@gmail.com")
                .build();

        when(userRepository.findByEmail(secondUser.getEmail())).thenReturn(Optional.of(secondUser));

        team.getMembers().add(secondUser);

        teamService.changeTeamMemberRole(secondUser.getUsername(), 1L, TeamRole.ADMIN, user.getEmail());

        verify(teamRepository, times(1)).save(team);

        assertThat(team.getAdmins().contains(secondUser)).isTrue();
    }

    @Test
    void should_change_member_role_from_admin_to_member(){
        User secondUser = User.builder()
                .firstName("Test")
                .lastName("SecondUser")
                .email("testSecondUser@gmail.com")
                .build();

        when(userRepository.findByEmail(secondUser.getEmail())).thenReturn(Optional.of(secondUser));

        team.getMembers().add(secondUser);
        team.getAdmins().add(secondUser);

        teamService.changeTeamMemberRole(secondUser.getUsername(), 1L, TeamRole.MEMBER, user.getEmail());

        verify(teamRepository, times(1)).save(team);

        assertThat(team.getAdmins().contains(secondUser)).isFalse();
    }

    @Test
    void should_list_all_tasks_of_team_member(){
        Task task = Task.builder()
                .id(1L)
                .name("TeamTask")
                .owner(user)
                .toDateTime(LocalDateTime.now().plusDays(1))
                .build();

        team.getTasks().add(task);

        List<Task> tasks = teamService.listAllTasksOfTeamMember(user.getEmail(), 1L);

        assertEquals(1, tasks.size());
    }
}