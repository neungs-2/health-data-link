package com.healthcare.link.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "steps_record")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_record_id")
    private Long stepRecordId;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "recordkey", referencedColumnName = "recordkey", nullable = false),
        @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    })
    private Source source;

    private Integer steps;

    private Double distance;

    private Double calories;

    private LocalDateTime periodFrom;

    private LocalDateTime periodTo;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
