package com.healthcare.link.domain.entity;

import com.healthcare.link.domain.vo.SourceId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "source")
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Source implements Persistable<SourceId> {

    @EmbeddedId
    private SourceId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_vender")
    private String productVender;

    private Integer mode;

    private String type;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Override
    public boolean isNew() {
        return createdAt == null; // EmbeddedId 직접 주입하므로 createAt으로 판단
    }
}
