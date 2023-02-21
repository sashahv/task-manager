package com.olekhv.taskmanager.task;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/task")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<String> addTaskForSingleUser(@RequestBody Task task,
                                                       @AuthenticationPrincipal UserDetails userDetails){
        String username = userDetails.getUsername();
        taskService.addTaskForSingleUser(username, task);

        return ResponseEntity.ok("Task was added successfully");
    }

    @PutMapping
    public ResponseEntity<String> editTask(@RequestParam("id") Long id,
                                           @RequestBody Task task,
                                           @AuthenticationPrincipal UserDetails userDetails){
        taskService.editTask(id, task, userDetails.getUsername());
        return ResponseEntity.ok("Task was edited successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteTask(@RequestParam("id") Long id,
                                             @AuthenticationPrincipal UserDetails userDetails){
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.ok("Task was deleted successfully");
    }
}
