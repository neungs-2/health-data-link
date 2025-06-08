package com.healthcare.link.repository;

import com.healthcare.link.domain.entity.Source;
import com.healthcare.link.domain.entity.User;
import com.healthcare.link.domain.vo.SourceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<Source, SourceId> {
}
