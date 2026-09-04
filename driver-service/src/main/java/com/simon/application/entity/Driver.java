package com.simon.application.entity;

import com.simon.application.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A 1:1 "extension table" keyed by the owning user's id (from user-service),
 * not a separately generated id - see docs/MICROSERVICES.md "Identity/key choice".
 * firstName/lastName/email are a snapshot taken from the USER_REGISTERED event at
 * creation time, not a live read of user-service - see the known staleness gap
 * documented there.
 */
@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus driverStatus;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
