package com.healthcare.link.domain.entity;

import com.healthcare.link.domain.vo.DailySummaryId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_summary")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummary {

    @EmbeddedId
    private DailySummaryId id;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "recordkey", referencedColumnName = "recordkey", insertable = false, updatable = false),
        @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private Source source;

    @Column(name = "steps")
    private Integer steps;

    @Column(name = "calories")
    private Double calories;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
