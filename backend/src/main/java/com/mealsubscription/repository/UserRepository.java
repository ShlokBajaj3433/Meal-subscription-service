package com.mealsubscription.repository;

import com.mealsubscription.entity.Role;
import com.mealsubscription.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAllByRole(Role role, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();

    @Query("SELECT u FROM User u WHERE u.active = :active ORDER BY u.createdAt DESC")
    Page<User> findAllByActive(@Param("active") boolean active, Pageable pageable);
}
