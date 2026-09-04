package com.simon.application.repository;

import com.simon.application.entity.Driver;
import com.simon.application.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByDriverStatus(DriverStatus driverStatus);
}
