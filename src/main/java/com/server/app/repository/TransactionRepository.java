package com.server.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.server.app.domain.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  @EntityGraph(attributePaths = { "sourceAccount", "destinationAccount" })
  Optional<Transaction> findByTransactionReference(String reference);

  @EntityGraph(attributePaths = { "sourceAccount", "destinationAccount" })
  Optional<Transaction> findById(Long id);

  @EntityGraph(attributePaths = { "sourceAccount", "destinationAccount" })
  Page<Transaction> findAll(Pageable pageable);

  // left join explícito: un depósito no tiene origen y un retiro no tiene destino
  @Query(value = """
      select t from Transaction t
        left join t.sourceAccount s
        left join t.destinationAccount d
      where s.user.id = :userId or d.user.id = :userId
      """, countQuery = """
      select count(t) from Transaction t
        left join t.sourceAccount s
        left join t.destinationAccount d
      where s.user.id = :userId or d.user.id = :userId
      """)
  Page<Transaction> findAllByParticipantUser(@Param("userId") Long userId, Pageable pageable);
}
