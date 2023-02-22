package com.olekhv.taskmanager.task;

import com.olekhv.taskmanager.exception.NoPermissionException;
import com.olekhv.taskmanager.user.Role;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TaskRepository taskRepository;

    private User user;
    private Task task;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .firstName("Oleksandr")
                .lastName("Hvozditskyi")
                .email("testUser@gmail.com")
                .role(Role.USER)
                .build();

        task = Task.builder()
                .id(1L)
                .name("Task")
                .owner(user)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
    }

    @Test
    void should_add_task_to_user_and_check_whether_size_of_tasks_in_user_increased(){
        taskService.addTaskForSingleUser(Objects.requireNonNull(user).getEmail(), task);

        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(user);

        assertEquals(1, user.getTasks().size());
    }

    @Test
    void should_edit_task(){
        Task editedTask = Task.builder()
                .name("Edited task")
                .build();

        taskService.editTask(task.getId(), editedTask, user.getEmail());

        verify(taskRepository, times(1)).save(any(Task.class));

        assertEquals("Edited task", task.getName());
    }

    @Test
    void should_delete_task(){
        taskService.addTaskForSingleUser(Objects.requireNonNull(user).getEmail(), task);

        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(user);

        taskService.deleteTask(task.getId(), user.getEmail());

        assertEquals(0, user.getTasks().size());
    }

    @Test
    void should_list_all_tasks_for_user_if_role_user(){
        taskService.addTaskForSingleUser(Objects.requireNonNull(user).getEmail(), task);

        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(user);

        assertEquals(1, taskService.listAllTasks(user.getEmail()).size());
    }

    @Test
    void should_list_tasks_for_all_users_if_role_admin(){
        Task secondTask = Task.builder()
                .id(2L)
                .name("Second task")
                .owner(user)
                .build();

        User admin = User.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .email("testAdmin@gmail.com")
                .role(Role.ADMIN)
                .build();

        taskService.addTaskForSingleUser(Objects.requireNonNull(user).getEmail(), task);
        taskService.addTaskForSingleUser(Objects.requireNonNull(user).getEmail(), secondTask);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(java.util.Optional.of(admin));
        when(taskRepository.findAll()).thenReturn(List.of(task, secondTask));

        verify(taskRepository, times(1)).save(task);
        verify(taskRepository, times(1)).save(secondTask);
        verify(userRepository, times(2)).save(user);

        assertEquals(2, taskService.listAllTasks(admin.getEmail()).size());
    }

    @Test
    void should_list_tasks_of_certain_user_if_role_admin(){
        User admin = User.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .email("testAdmin@gmail.com")
                .role(Role.ADMIN)
                .build();

        taskService.addTaskForSingleUser(Objects.requireNonNull(user).getEmail(), task);
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(java.util.Optional.of(admin));

        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(user);

        assertEquals(1, taskService.listTasksOfUser(user.getEmail(), admin.getEmail()).size());
    }

    @Test
    void should_throw_exception_when_list_tasks_of_other_user_without_admin_role(){
        assertThrows(NoPermissionException.class, () ->
                taskService.listTasksOfUser(user.getEmail(), user.getEmail())
        );
    }
}