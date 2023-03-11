package com.olekhv.taskmanager.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByJoinCode(String joinCode);

    @Query("SELECT t FROM Team t JOIN t.tasks task WHERE task.id = ?1")
    Optional<Team> findByTaskId(Long taskId);
}
