package org.example.repository.primary;

import org.example.entity.primary.User;
import org.example.entity.enums_status.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRoles_RoleName(UserRole roleName);
    Optional<User> findByName(String username);


}
