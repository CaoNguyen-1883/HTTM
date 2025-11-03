package dev.CaoNguyen_1883.ecommerce.user.repository;

import dev.CaoNguyen_1883.ecommerce.user.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT p FROM Permission p WHERE p.isActive = true ORDER BY p.name")
    Set<Permission> findAllActive();

    @Query("SELECT p FROM Permission  p WHERE p.id IN :ids AND p.isActive = true")
    Set<Permission> findAllByIdInAndActive(Set<UUID> ids);

}
