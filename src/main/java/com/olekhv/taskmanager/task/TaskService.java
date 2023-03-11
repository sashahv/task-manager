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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public void addTaskForSingleUser(String authUserEmail,
                                     Task task) {
        User user = userRepository.findByEmail(authUserEmail).get();

        task.setOwner(user);

        List<Task> tasks = user.getTasks();
        tasks.add(task);
        user.setTasks(tasks);

        taskRepository.save(task);
        userRepository.save(user);
    }

    public void editTask(Long taskId,
                         Task editedTask,
                         String authUserEmail) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        User user = userRepository.findByEmail(authUserEmail).get();

        if (!user.equals(task.getOwner()) && !user.getRole().equals(Role.SUPPORT)) {
            throw new TaskNotFoundException("Task not found");
        }

        task.setName(editedTask.getName());
        task.setDescription(editedTask.getDescription());
        task.setPriority(editedTask.getPriority());
        task.setFromDateTime(editedTask.getFromDateTime());
        task.setToDateTime(editedTask.getToDateTime());

        taskRepository.save(task);
    }

    public void changeTaskProgress(Long taskId,
                                   TaskProgress taskProgress,
                                   String authUserEmail) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        User authUser = userRepository.findByEmail(authUserEmail).get();

        if (!task.getOwner().equals(authUser) && !authUser.getRole().equals(Role.SUPPORT)) {
            throw new NoPermissionException("No permission");
        }

        task.setProgress(taskProgress);
        taskRepository.save(task);
    }

    public void deleteTask(Long taskId,
                           String authUserEmail) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        User authUser = userRepository.findByEmail(authUserEmail).get();

        if (!authUser.equals(task.getOwner()) && !authUser.getRole().equals(Role.SUPPORT)) {
            throw new TaskNotFoundException("Task not found");
        }

        List<Task> tasks = authUser.getTasks();
        tasks.remove(task);

        task.setProgress(TaskProgress.CLOSED);
        taskRepository.save(task);
    }

    /* Allow supporter to list all
    tasks of the specific user */
    public List<Task> listTasksOfSpecificUser(String providedUserEmail,
                                              String authUserEmail) {
        User authUser = userRepository.findByEmail(authUserEmail).get();

        User providedUser = userRepository.findByEmail(providedUserEmail).orElseThrow(
                () -> new UsernameNotFoundException("User " + providedUserEmail + " not found")
        );

        if (!authUser.getRole().equals(Role.SUPPORT)) {
            throw new NoPermissionException("No permission");
        }

        List<Task> tasks = providedUser.getTasks();

        tasks.stream()
                .filter(task -> task.getToDateTime().isBefore(LocalDateTime.now()) || task.getToDateTime().isEqual(LocalDateTime.now()))
                .forEach(task -> changeTaskProgress(task.getId(), TaskProgress.OVERDUE, authUserEmail));

        sortListOfTasks(tasks);

        return tasks;
    }

    /* Allow users
    to watch all their tasks */
    public List<Task> listAllTasks(String authUserEmail) {
        User authUser = userRepository.findByEmail(authUserEmail).get();

        List<Task> tasks = authUser.getTasks();

        tasks.stream()
                .filter(task -> task.getToDateTime().isBefore(LocalDateTime.now()) || task.getToDateTime().isEqual(LocalDateTime.now()))
                .forEach(task -> changeTaskProgress(task.getId(), TaskProgress.OVERDUE, authUserEmail));

        sortListOfTasks(tasks);

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
