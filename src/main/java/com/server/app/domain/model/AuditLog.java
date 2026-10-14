package com.server.app.domain.model;

import java.time.LocalDateTime;

import com.server.app.domain.model.enums.Channel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_logs", indexes = @Index(name = "idx_audit_created", columnList = "created_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Channel channel;

  @Column(nullable = false, length = 500)
  private String endpoint;

  @Column(name = "request_payload", columnDefinition = "text")
  private String requestPayload;

  @Column(name = "response_payload", columnDefinition = "text")
  private String responsePayload;

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  @Column(name = "status_code")
  private Integer statusCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
