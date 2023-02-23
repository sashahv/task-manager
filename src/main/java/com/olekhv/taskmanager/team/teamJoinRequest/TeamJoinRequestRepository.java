package com.olekhv.taskmanager.team.teamJoinRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {
    Optional<TeamJoinRequest> findByUserEmailAndTeamId(String userEmail, Long teamId);
}
