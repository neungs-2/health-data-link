package com.healthcare.link.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "steps_record", uniqueConstraints = { // 유니크 키 제약조건
    @UniqueConstraint(name = "uq_steps_record_user_record_period", columnNames = {"user_id", "recordkey", "period_from", "period_to"})
})
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class StepsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_record_id")
    private Long stepRecordId;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "recordkey", referencedColumnName = "recordkey", updatable = false),
        @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = false)
    })
    private Source source;

    private Integer steps;

    private Double distance;

    private Double calories;

    @Column(name = "period_from", nullable = false)
    private ZonedDateTime periodFrom;

    @Column(name = "period_to", nullable = false)
    private ZonedDateTime periodTo;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
