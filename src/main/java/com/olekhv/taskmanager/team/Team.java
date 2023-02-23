package com.olekhv.taskmanager.team;

import com.olekhv.taskmanager.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    private User owner;

    @OneToMany
    private List<User> admins = new ArrayList<>();

    private Integer numberOfMembers;

    private String joinCode;

    @Enumerated(EnumType.STRING)
    private TeamType type;

    @ManyToMany
    private List<User> members = new ArrayList<>();
}
