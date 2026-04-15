package com.savchenko.bankapp.repository;

import com.savchenko.bankapp.model.WalletModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<WalletModel, UUID> {
    @Query("SELECT w FROM WalletModel w WHERE w.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletModel> findByIdWithLock(UUID id);
}
