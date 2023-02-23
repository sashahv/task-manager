package com.olekhv.taskmanager.task;

import com.olekhv.taskmanager.exception.NoPermissionException;
import com.olekhv.taskmanager.exception.TaskNotFoundException;
import com.olekhv.taskmanager.user.Role;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public void addTaskForSingleUser(String username,
                                     Task task) {
        User user = userRepository.findByEmail(username).get();

        task.setOwner(user);

        List<Task> tasks = user.getTasks() != null ? user.getTasks() : new ArrayList<>();
        tasks.add(task);
        user.setTasks(tasks);

        taskRepository.save(task);
        userRepository.save(user);
    }

    public void editTask(Long taskId, Task editedTask, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        User user = userRepository.findByEmail(username).get();

        if (!user.equals(task.getOwner()) && !user.getRole().equals(Role.ADMIN)) {
            throw new TaskNotFoundException("Task not found");
        }

        task.setName(editedTask.getName());
        task.setDescription(editedTask.getDescription());
        task.setPriority(editedTask.getPriority());
        task.setFromDateTime(editedTask.getFromDateTime());
        task.setToDateTime(editedTask.getToDateTime());
        task.setPriority(editedTask.getPriority());
        task.setProgress(editedTask.getProgress());

        taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        User user = userRepository.findByEmail(username).get();

        if (!user.equals(task.getOwner()) && !user.getRole().equals(Role.ADMIN)) {
            throw new TaskNotFoundException("Task not found");
        }

        List<Task> tasks = user.getTasks();
        tasks.remove(task);

        taskRepository.deleteById(taskId);
        userRepository.save(user);
    }

    /* Allow admin to list all tasks
    of the specific user */
    public List<Task> listTasksOfUser(String username, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail).get();

        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " not found")
        );

        if (!admin.getRole().equals(Role.ADMIN)) {
            throw new NoPermissionException("No permission");
        }

        List<Task> tasks = user.getTasks();
        sortListOfTasks(tasks);

        return tasks;
    }

    /* Allow users to watch all their tasks
    and admin to watch all existing tasks*/
    public List<Task> listAllTasks(String username) {
        User user = userRepository.findByEmail(username).get();

        List<Task> tasks;
        if (user.getRole().equals(Role.ADMIN)) {
            tasks = taskRepository.findAll();
        } else {
            tasks = user.getTasks();
            sortListOfTasks(tasks);
        }

        return tasks;
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
}
