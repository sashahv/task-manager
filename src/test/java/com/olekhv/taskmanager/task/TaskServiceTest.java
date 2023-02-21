package com.olekhv.taskmanager.task;

import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Objects;

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
                .build();

        task = Task.builder()
                .id(1L)
                .name("Task")
                .owner(user)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(taskRepository.findById(task.getId())).thenReturn(java.util.Optional.of(task));
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

        taskService.deleteTask(task.getId(), user.getUsername());

        assertEquals(0, user.getTasks().size());
    }
}