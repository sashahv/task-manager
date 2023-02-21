package com.olekhv.taskmanager.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query (value = "SELECT t " +
            "FROM Token t " +
            "INNER JOIN User u " +
            "ON t.user.id=u.id " +
            "WHERE u.id=?1 and (t.expired = false or t.revoked = false)"
    )
    List<Token> findAllValidTokenByUserId(Long userId);

    Optional<Token> findByToken(String token);
}
