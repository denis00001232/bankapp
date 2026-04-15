package com.savchenko.bankapp.dto;

import com.savchenko.bankapp.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class WalletBalanceDto {
    private UUID id;
    private BigDecimal balance;

    public static WalletBalanceDto fromDomain(Wallet wallet) {
        return new WalletBalanceDto(wallet.getId(), wallet.getBalance());
    }
}
