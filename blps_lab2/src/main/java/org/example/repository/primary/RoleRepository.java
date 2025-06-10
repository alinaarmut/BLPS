package org.example.repository.primary;

import org.example.entity.primary.Role;
import org.example.entity.enums_status.UserRole;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByRoleName(UserRole roleName);
}
