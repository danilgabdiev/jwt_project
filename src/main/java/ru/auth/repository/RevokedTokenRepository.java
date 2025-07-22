package ru.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.auth.entity.RevokedToken;
import java.util.Optional;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByToken(String token);
    boolean existsByToken(String token);
}