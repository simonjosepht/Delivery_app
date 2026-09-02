package com.simon.application.repository;

import com.simon.application.entity.User;
import com.simon.application.enums.DriverStatus;
import com.simon.application.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByRole(UserRole role);

    List<User> findByRoleAndDriverStatus(UserRole role, DriverStatus driverStatus);
}