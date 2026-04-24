package com.pharmacy.auth.repository;

import java.util.Optional;
import com.pharmacy.auth.entity.Role;
import com.pharmacy.auth.entity.User;
import com.pharmacy.auth.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);

    // all three filters
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status AND (LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<User> findByRoleAndStatusAndQ(@Param("role") Role role, @Param("status") UserStatus status, @Param("q") String q, Pageable pageable);

    Page<User> findByRoleAndStatus(Role role, UserStatus status, Pageable pageable);

    // role + q
    @Query("SELECT u FROM User u WHERE u.role = :role AND (LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<User> findByRoleAndQ(@Param("role") Role role, @Param("q") String q, Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    // status + q
    @Query("SELECT u FROM User u WHERE u.status = :status AND (LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<User> findByStatusAndQ(@Param("status") UserStatus status, @Param("q") String q, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    // q only
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<User> findByQ(@Param("q") String q, Pageable pageable);
}