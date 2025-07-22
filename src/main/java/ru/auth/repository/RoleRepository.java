package ru.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.auth.entity.Role;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
