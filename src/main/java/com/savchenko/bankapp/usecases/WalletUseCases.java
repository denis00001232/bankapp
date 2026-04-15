package com.savchenko.bankapp.usecases;

import com.savchenko.bankapp.domain.Wallet;
import com.savchenko.bankapp.model.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletUseCases {

    void performWalletOperation(UUID walletId, BigDecimal amount, OperationType operationType);

    Wallet getWalletBalance(UUID walletId);
}
