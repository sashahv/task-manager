package com.olekhv.taskmanager.task;

import com.olekhv.taskmanager.exception.TaskNotFoundException;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public void addTaskForSingleUser(String username,
                                     Task task){
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        task.setOwner(user);

        List<Task> tasks = user.getTasks()!=null ? user.getTasks() : new ArrayList<>();
        tasks.add(task);
        user.setTasks(tasks);

        taskRepository.save(task);
        userRepository.save(user);
    }

    public void editTask(Long taskId, Task editedTask, String username){
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        checkWhetherTaskBelongsToUser(task, username);

        task.setName(editedTask.getName());
        task.setDescription(editedTask.getDescription());
        task.setPriority(editedTask.getPriority());
        task.setFromDateTime(editedTask.getFromDateTime());
        task.setToDateTime(editedTask.getToDateTime());
        task.setPriority(editedTask.getPriority());
        task.setProgress(editedTask.getProgress());

        taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String username){
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found")
        );

        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        checkWhetherTaskBelongsToUser(task, username);

        List<Task> tasks = user.getTasks();
        tasks.remove(task);

        taskRepository.deleteById(taskId);
        userRepository.save(user);
    }

    private void checkWhetherTaskBelongsToUser(Task task, String username) {
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        if(!user.equals(task.getOwner())){
            throw new RuntimeException("Task not found");
        }
    }
}
