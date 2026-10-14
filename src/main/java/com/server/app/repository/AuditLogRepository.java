package com.server.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.domain.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
