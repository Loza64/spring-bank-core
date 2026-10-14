package com.server.app.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beneficiaries", uniqueConstraints = @UniqueConstraint(name = "uk_beneficiary_user_bank_account", columnNames = {
        "user_id", "bank_name", "destination_account_number" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String alias;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "destination_account_number", nullable = false, length = 34)
    private String destinationAccountNumber;

    @Column(name = "destination_document_number", length = 50)
    private String destinationDocumentNumber;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
