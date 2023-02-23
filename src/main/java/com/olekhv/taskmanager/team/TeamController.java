package com.olekhv.taskmanager.team;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/delete")
    public ResponseEntity<String> deleteRequest(@RequestParam("request") Long requestId,
                                                @AuthenticationPrincipal UserDetails userDetails){
        teamService.deleteTeamJoinRequest(requestId);
        return ResponseEntity.ok("Request successfully deleted");
    }
}
