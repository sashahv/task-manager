package com.olekhv.taskmanager.team;

import com.olekhv.taskmanager.task.Task;
import com.olekhv.taskmanager.task.TaskProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/create")
    public ResponseEntity<String> createTeam(@RequestBody Team team,
                                             @AuthenticationPrincipal UserDetails userDetails){
        teamService.createTeam(team, userDetails.getUsername());
        return ResponseEntity.ok("Team was created successfully");
    }

    @PostMapping("/tasks")
    public ResponseEntity<String> addTaskForTeamMember(@RequestParam Long teamId,
                                                       @RequestBody Task task,
                                                       @AuthenticationPrincipal UserDetails userDetails){
        teamService.addTaskForTeamMember(teamId, task, userDetails.getUsername());
        return ResponseEntity.ok("Task was added successfully");
    }

    @PostMapping("/tasks/edit")
    public ResponseEntity<String> editTeamTask(@RequestParam Long taskId,
                                               @RequestBody Task task,
                                               @AuthenticationPrincipal UserDetails userDetails){
        teamService.editTeamTask(taskId, task, userDetails.getUsername());
        return ResponseEntity.ok("Task was edited successfully");
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> listAllTasksOfTeamMember(@RequestParam("userEmail") String userEmail,
                                                               @RequestParam Long teamId){
        return ResponseEntity.ok(teamService.listAllTasksOfTeamMember(userEmail, teamId));
    }

    @PutMapping("/tasks/edit/progress")
    public ResponseEntity<String> changeTeamTaskProgress(@RequestParam Long taskId,
                                                         @RequestParam TaskProgress taskProgress,
                                                         @AuthenticationPrincipal UserDetails userDetails){
        teamService.changeTeamTaskProgress(taskId, taskProgress, userDetails.getUsername());
        return ResponseEntity.ok("Progress was changed successfully");
    }

    @DeleteMapping("/tasks")
    public ResponseEntity<String> deleteTaskFromTeamMember(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestParam Long taskId){
        teamService.deleteTaskFromTeamMember(userDetails.getUsername(), taskId);
        return ResponseEntity.ok("Task was deleted successfully");
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinTeam(@RequestParam("code") String joinCode,
                                           @AuthenticationPrincipal UserDetails userDetails){
        String joinTeamResponse = teamService.joinTeamByCode(joinCode, userDetails.getUsername());
        return ResponseEntity.ok(joinTeamResponse.equals("joined")
                ? "Joined successfully"
                : "Request was sent");
    }

    @PostMapping("/add")
    public ResponseEntity<String> addUserToTeam(@RequestParam("code") String joinCode,
                                           @RequestParam("userEmail") String userEmail,
                                           @AuthenticationPrincipal UserDetails userDetails){
        teamService.addUserToTeam(joinCode, userEmail, userDetails.getUsername());
        return ResponseEntity.ok("User was added to team");
    }

    @PostMapping("")
    public ResponseEntity<String> changeTeamMemberRole(@RequestParam String userEmail,
                                                       @RequestParam Long teamId,
                                                       @RequestParam TeamRole teamRole,
                                                       @AuthenticationPrincipal UserDetails userDetails){
        teamService.changeTeamMemberRole(userEmail, teamId, teamRole, userDetails.getUsername());
        return ResponseEntity.ok("Role successfully changed to " + teamRole.getName());
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteRequest(@RequestParam("request") Long requestId,
                                                @AuthenticationPrincipal UserDetails userDetails){
        teamService.deleteTeamJoinRequest(requestId);
        return ResponseEntity.ok("Request successfully deleted");
    }
}
