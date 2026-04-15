package com.savchenko.bankapp.usecases;

import com.savchenko.bankapp.domain.Wallet;
import com.savchenko.bankapp.exception.WalletNotFoundException;
import com.savchenko.bankapp.mapper.WalletMapper;
import com.savchenko.bankapp.model.OperationType;
import com.savchenko.bankapp.model.WalletModel;
import com.savchenko.bankapp.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletUseCaseImpl implements WalletUseCases {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void performWalletOperation(UUID walletId, BigDecimal amount, OperationType operationType) {
        WalletModel entity = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(WalletNotFoundException::new);
        
        Wallet wallet = WalletMapper.toDomain(entity);
        
        if (operationType == OperationType.DEPOSIT) {
            wallet.deposit(amount);
        } else if (operationType == OperationType.WITHDRAW) {
            wallet.withdraw(amount);
        }
        
        WalletMapper.updateEntityFromDomain(wallet, entity);
        walletRepository.save(entity);
    }

    @Override
    @Transactional
    public Wallet getWalletBalance(UUID walletId) {
        WalletModel entity = walletRepository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);
        
        return WalletMapper.toDomain(entity);
    }
}
