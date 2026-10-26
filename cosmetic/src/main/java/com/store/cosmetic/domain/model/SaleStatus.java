package com.store.cosmetic.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"sale_status\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sale_status_seq")
    @SequenceGenerator(name = "sale_status_seq", sequenceName = "sale_status_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "status", nullable = false, length = 100)
    private String status;

    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "date_deleted")
    private LocalDateTime dateDeleted;

    @Column(name = "user_created", nullable = false, updatable = false)
    private Long userCreated;

    @Column(name = "user_updated")
    private Long userUpdated;

    @Column(name = "user_deleted")
    private Long userDeleted;
}
