package com.savchenko.bankapp.controller;

import com.savchenko.bankapp.domain.Wallet;
import com.savchenko.bankapp.dto.WalletBalanceDto;
import com.savchenko.bankapp.dto.WalletOperationRequestDto;
import com.savchenko.bankapp.usecases.WalletUseCases;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WalletActionsController {

    private final WalletUseCases walletUseCases;

    @PostMapping("/wallet")
    public ResponseEntity<?> performWalletOperation(@RequestBody @Valid WalletOperationRequestDto request) {
        walletUseCases.performWalletOperation(
                request.getWalletId(),
                request.getAmount(),
                request.getOperationType()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletBalanceDto> getWalletBalance(@PathVariable UUID walletId) {
        Wallet wallet = walletUseCases.getWalletBalance(walletId);
        return ResponseEntity.ok(WalletBalanceDto.fromDomain(wallet));
    }
}
