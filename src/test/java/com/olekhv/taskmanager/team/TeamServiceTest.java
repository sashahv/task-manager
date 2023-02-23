package com.olekhv.taskmanager.team;

import com.olekhv.taskmanager.exception.TeamNotFoundException;
import com.olekhv.taskmanager.exception.UserAlreadyExistsException;
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

import java.util.ArrayList;
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
                .role(Role.USER)
                .build();

        team = Team.builder()
                .id(1L)
                .name("TestTeam")
                .owner(user)
                .joinCode("Abc123")
                .admins(new ArrayList<>())
                .members(new ArrayList<>())
                .numberOfMembers(1)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(teamRepository.findByJoinCode("Abc123")).thenReturn(Optional.of(team));
    }

    @Test
    void should_create_new_team(){
        team.setType(TeamType.PUBLIC);
        teamService.createTeam(team, user.getEmail());

        verify(teamRepository, times(1)).save(team);

        assertEquals(6, team.getJoinCode().length());
        assertEquals(1, team.getMembers().size());
        assertEquals(1, team.getAdmins().size());
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
        team.getAdmins().add(user);

        teamService.joinTeamByCode("Abc123", secondUser.getEmail());

        verify(teamRepository, times(1)).save(team);

        assertThat(team.getMembers().contains(secondUser)).isTrue();
        assertEquals(2, team.getNumberOfMembers());
        assertEquals(1, team.getMembers().size());
    }

    @Test
    void should_create_invite_request_for_private_team(){
        User secondUser = User.builder()
                .firstName("Test")
                .lastName("SecondUser")
                .email("testSecondUser@gmail.com")
                .role(Role.USER)
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
        team.getMembers().add(user);
        teamRepository.save(team);

        assertThrows(UserAlreadyExistsException.class, ()->
                teamService.joinTeamByCode("Abc123", user.getEmail()));
    }

    @Test
    void should_throw_exception_if_request_already_exists(){
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
}