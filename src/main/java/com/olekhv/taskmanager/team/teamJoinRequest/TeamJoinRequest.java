package com.olekhv.taskmanager.team.teamJoinRequest;

import com.olekhv.taskmanager.team.Team;
import com.olekhv.taskmanager.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamJoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private User user;

    @ManyToOne
    private Team team;
}
