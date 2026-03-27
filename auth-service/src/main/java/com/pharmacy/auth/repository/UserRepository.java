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

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:q IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<User> findAllWithFilters(@Param("role") Role role,
                                  @Param("status") UserStatus status,
                                  @Param("q") String q,
                                  Pageable pageable);
}