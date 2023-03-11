package com.olekhv.taskmanager.task;

import com.olekhv.taskmanager.exception.NoPermissionException;
import com.olekhv.taskmanager.user.Role;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
                .firstName("Test")
                .lastName("User")
                .email("testUser@gmail.com")
                .role(Role.USER)
                .tasks(new ArrayList<>())
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
    @DisplayName("Add task for user")
    void should_add_task_for_user_and_check_whether_size_of_tasks_of_user_increased(){
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
    void should_change_task_progress(){
        taskService.changeTaskProgress(task.getId(), TaskProgress.IN_PROCESS, user.getEmail());

        verify(taskRepository, times(1)).save(any(Task.class));

        assertEquals(TaskProgress.IN_PROCESS, task.getProgress());
    }

    @Test
    void should_delete_task(){
        taskService.deleteTask(1L, user.getEmail());

        assertEquals(TaskProgress.CLOSED, task.getProgress());
    }

    @Test
    void should_list_all_tasks_for_user_if_role_user(){
        task.setToDateTime(LocalDateTime.now().plusDays(1));

        taskService.addTaskForSingleUser(Objects.requireNonNull(user).getEmail(), task);

        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(user);

        assertEquals(1, taskService.listAllTasks(user.getEmail()).size());
    }

    @Test
    void should_change_progress_if_task_overdue(){
        task.setToDateTime(LocalDateTime.now().minusHours(1));

        user.getTasks().add(task);

        taskService.listAllTasks(user.getEmail());

        assertEquals(TaskProgress.OVERDUE, task.getProgress());
    }

    @Test
    void should_list_tasks_of_certain_user_if_role_support(){
        User support = User.builder()
                .firstName("Test")
                .lastName("Support")
                .email("testSupport@gmail.com")
                .role(Role.SUPPORT)
                .build();

        task.setToDateTime(LocalDateTime.now().plusDays(1));
        taskService.addTaskForSingleUser(Objects.requireNonNull(user).getEmail(), task);
        when(userRepository.findByEmail(support.getEmail())).thenReturn(java.util.Optional.of(support));

        verify(taskRepository, times(1)).save(task);
        verify(userRepository, times(1)).save(user);

        assertEquals(1, taskService.listTasksOfSpecificUser(user.getEmail(), support.getEmail()).size());
    }

    @Test
    @DisplayName("Throw exception when try to list other user's tasks")
    void should_throw_exception_when_list_tasks_of_other_user_without_support_role(){
        assertThrows(NoPermissionException.class, () ->
                taskService.listTasksOfSpecificUser(user.getEmail(), user.getEmail())
        );
    }
}