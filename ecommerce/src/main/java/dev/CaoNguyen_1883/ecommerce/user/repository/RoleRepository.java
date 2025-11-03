package dev.CaoNguyen_1883.ecommerce.user.repository;

import dev.CaoNguyen_1883.ecommerce.user.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT  r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.isActive = true ORDER BY r.name")
    List<Role> findAllActive();

    @Query("SELECT r FROM Role  r LEFT JOIN FETCH r.permissions WHERE r.id = :id AND r.isActive = true")
    Optional<Role> findByIdWithPermissions(UUID id);

    @Query("SELECT r FROM Role r WHERE r.id IN :ids AND r.isActive = true")
    Set<Role> findAllByIdInAndActive(Set<UUID> ids);
}
