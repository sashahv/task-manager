package com.olekhv.taskmanager.task;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.olekhv.taskmanager.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String description;
//    private Category category;
    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;
    @Enumerated(EnumType.STRING)
    private TaskProgress progress;
    @ManyToOne
    private User owner;
}
