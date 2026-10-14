package com.server.app.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import com.server.app.domain.model.Account;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

  boolean existsByAccountNumber(String accountNumber);

  @Query("select a.id from Account a where a.accountNumber = :accountNumber")
  Optional<Long> findIdByAccountNumber(@Param("accountNumber") String accountNumber);

  @EntityGraph(attributePaths = "user")
  Optional<Account> findByAccountNumber(String accountNumber);

  @EntityGraph(attributePaths = "user")
  Optional<Account> findById(Long id);

  @EntityGraph(attributePaths = "user")
  Page<Account> findByUser_Id(Long userId, Pageable pageable);

  @Override
  @EntityGraph(attributePaths = "user")
  Page<Account> findAll(@NonNull Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Account a where a.id in :ids order by a.id")
  List<Account> lockAllByIdIn(@Param("ids") Collection<Long> ids);
}
