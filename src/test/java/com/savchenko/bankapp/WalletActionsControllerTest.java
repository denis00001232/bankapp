package com.savchenko.bankapp;

import com.savchenko.bankapp.dto.WalletBalanceDto;
import com.savchenko.bankapp.dto.WalletOperationRequestDto;
import com.savchenko.bankapp.model.OperationType;
import com.savchenko.bankapp.model.WalletModel;
import com.savchenko.bankapp.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.profiles.active=test"
})
@AutoConfigureWebTestClient
class WalletActionsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
    }

    @Test
    void performDeposit_shouldReturn200() {
        WalletModel wallet = new WalletModel();
        wallet.setBalance(BigDecimal.ZERO);
        WalletModel savedWallet = walletRepository.save(wallet);
        UUID walletId = savedWallet.getId();

        WalletOperationRequestDto request = new WalletOperationRequestDto();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("100.50"));

        webTestClient.post()
                .uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        WalletModel updatedWallet = walletRepository.findById(walletId).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo("100.50");
    }

    @Test
    void performWithdraw_shouldReturn200() {
        WalletModel wallet = new WalletModel();
        wallet.setBalance(new BigDecimal("200.00"));
        WalletModel walletModel = walletRepository.save(wallet);

        UUID walletId = walletModel.getId();
        WalletOperationRequestDto request = new WalletOperationRequestDto();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.WITHDRAW);
        request.setAmount(new BigDecimal("50.00"));

        webTestClient.post()
                .uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        WalletModel updatedWallet = walletRepository.findById(walletId).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void performWithdraw_insufficientFunds_shouldReturn400() {
        WalletModel wallet = new WalletModel();
        wallet.setBalance(new BigDecimal("10.00"));
        WalletModel walletModel = walletRepository.save(wallet);
        UUID walletId = walletModel.getId();

        WalletOperationRequestDto request = new WalletOperationRequestDto();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.WITHDRAW);
        request.setAmount(new BigDecimal("100.00"));

        webTestClient.post()
                .uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Insufficient funds");
    }

    @Test
    void performOperation_walletNotFound_shouldReturn404() {
        UUID walletId = UUID.randomUUID();

        WalletOperationRequestDto request = new WalletOperationRequestDto();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("100.00"));

        webTestClient.post()
                .uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .isEqualTo("Wallet not found");
    }

    @Test
    void performOperation_invalidRequest_shouldReturn400() {
        WalletOperationRequestDto request = new WalletOperationRequestDto();
        request.setWalletId(null);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("-10.00"));

        webTestClient.post()
                .uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void performOperation_missingFields_shouldReturn400() {
        String invalidJson = "{\"walletId\": \"" + UUID.randomUUID() + "\"}";

        webTestClient.post()
                .uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getWalletBalance_shouldReturnBalance() {

        WalletModel wallet = new WalletModel();

        wallet.setBalance(new BigDecimal("500.25"));
        WalletModel walletModel = walletRepository.save(wallet);

        UUID walletId = walletModel.getId();

        webTestClient.get()
                .uri("/api/v1/wallets/{walletId}", walletId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WalletBalanceDto.class)
                .value(dto -> {
                    assertThat(dto.getId()).isEqualTo(walletId);
                    assertThat(dto.getBalance()).isEqualByComparingTo("500.25");
                });
    }

    @Test
    void getWalletBalance_walletNotFound_shouldReturn404() {
        UUID walletId = UUID.randomUUID();

        webTestClient.get()
                .uri("/api/v1/wallets/{walletId}", walletId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .isEqualTo("Wallet not found");
    }

    @Test
    void getWalletBalance_invalidUUID_shouldReturn400() {
        webTestClient.get()
                .uri("/api/v1/wallets/{walletId}", "invalid-uuid")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
