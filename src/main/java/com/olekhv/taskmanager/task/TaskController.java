package com.olekhv.taskmanager.task;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<String> addTaskForSingleUser(@RequestBody Task task,
                                                       @AuthenticationPrincipal UserDetails userDetails){
        taskService.addTaskForSingleUser(userDetails.getUsername(), task);
        return ResponseEntity.ok("Task was added successfully");
    }

    @PutMapping("/edit")
    public ResponseEntity<String> editTask(@RequestParam Long id,
                                           @RequestBody Task task,
                                           @AuthenticationPrincipal UserDetails userDetails){
        taskService.editTask(id, task, userDetails.getUsername());
        return ResponseEntity.ok("Task was edited successfully");
    }

    @PutMapping("/edit/progress")
    public ResponseEntity<String> changeTaskProgress(@RequestParam Long taskId,
                                                     @RequestParam TaskProgress taskProgress,
                                                     @AuthenticationPrincipal UserDetails userDetails){
        taskService.changeTaskProgress(taskId, taskProgress, userDetails.getUsername());
        return ResponseEntity.ok("Progress was changed successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteTask(@RequestParam Long id,
                                             @AuthenticationPrincipal UserDetails userDetails){
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.ok("Task was deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<Task>> listTasksOfSpecificUser(@RequestParam String userEmail,
                                                              @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(taskService.listTasksOfSpecificUser(userEmail, userDetails.getUsername()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Task>> listAllTasks(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(taskService.listAllTasks(userDetails.getUsername()));
    }
}
