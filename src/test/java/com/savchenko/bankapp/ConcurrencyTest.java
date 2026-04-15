package com.savchenko.bankapp;

import com.savchenko.bankapp.dto.WalletOperationRequestDto;
import com.savchenko.bankapp.model.OperationType;
import com.savchenko.bankapp.model.WalletModel;
import com.savchenko.bankapp.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.profiles.active=test"
})
@Testcontainers
class ConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
    }

    /**
     * Тестирует способность системы обрабатывать 100 запросов за раз(положить деньги на кошелек), с акцентом на корректность запроса
     * @throws InterruptedException
     */
    @Test
    void concurrentDeposits_shouldHandleConcurrency() throws InterruptedException {
        WalletModel wallet = new WalletModel();
        wallet.setBalance(BigDecimal.ZERO);
        WalletModel walletModel = walletRepository.save(wallet);
        UUID walletId = walletModel.getId();

        int threadCount = 1000;
        BigDecimal depositAmount = new BigDecimal("10.00");
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    WalletOperationRequestDto request = new WalletOperationRequestDto();
                    request.setWalletId(walletId);
                    request.setOperationType(OperationType.DEPOSIT);
                    request.setAmount(depositAmount);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<WalletOperationRequestDto> entity = new HttpEntity<>(request, headers);

                    ResponseEntity<String> response = restTemplate.postForEntity(
                            "/api/v1/wallet",
                            entity,
                            String.class
                    );

                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        WalletModel finalWallet = walletRepository.findById(walletId).orElseThrow();
        BigDecimal expectedBalance = depositAmount.multiply(new BigDecimal(threadCount));

        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(finalWallet.getBalance()).isEqualByComparingTo(expectedBalance);
    }

    /**
     * Тестирует способность системы обрабатывать 100 запросов за раз(снять деньги с кошелька), с акцентом на корректность запроса
     */
    @Test
    void concurrentWithdraws_shouldHandleConcurrency() throws InterruptedException {
        String bigDecimalBalance = "100000.00";
        WalletModel wallet = new WalletModel();
        wallet.setBalance(new BigDecimal(bigDecimalBalance));
        WalletModel walletModel = walletRepository.save(wallet);
        UUID walletId = walletModel.getId();

        int threadCount = 1000;
        BigDecimal withdrawAmount = new BigDecimal("5.00");
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    WalletOperationRequestDto request = new WalletOperationRequestDto();
                    request.setWalletId(walletId);
                    request.setOperationType(OperationType.WITHDRAW);
                    request.setAmount(withdrawAmount);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<WalletOperationRequestDto> entity = new HttpEntity<>(request, headers);

                    ResponseEntity<String> response = restTemplate.postForEntity(
                            "/api/v1/wallet",
                            entity,
                            String.class
                    );

                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        WalletModel finalWallet = walletRepository.findById(walletId).orElseThrow();
        BigDecimal expectedBalance = new BigDecimal(bigDecimalBalance)
                .subtract(withdrawAmount.multiply(new BigDecimal(successCount.get())));

        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);
        assertThat(finalWallet.getBalance()).isEqualByComparingTo(expectedBalance);
    }

    /**
     * Тестирует способность системы обрабатывать 100 запросов за раз(Обе операции), с акцентом на корректность запроса
     */
    @Test
    void concurrentMixedOperations_shouldHandleConcurrency() throws InterruptedException {
        WalletModel wallet = new WalletModel();
        wallet.setBalance(new BigDecimal("500.00"));
        WalletModel walletModel = walletRepository.save(wallet);
        UUID walletId = walletModel.getId();



        int threadCount = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    WalletOperationRequestDto request = new WalletOperationRequestDto();
                    request.setWalletId(walletId);

                    if (index % 2 == 0) {
                        request.setOperationType(OperationType.DEPOSIT);
                        request.setAmount(new BigDecimal("10.00"));
                    } else {
                        request.setOperationType(OperationType.WITHDRAW);
                        request.setAmount(new BigDecimal("5.00"));
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<WalletOperationRequestDto> entity = new HttpEntity<>(request, headers);

                    ResponseEntity<String> response = restTemplate.postForEntity(
                            "/api/v1/wallet",
                            entity,
                            String.class
                    );

                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(threadCount);

        WalletModel finalWallet = walletRepository.findById(walletId).orElseThrow();
        int deposits = threadCount / 2;
        int withdraws = threadCount / 2;
        BigDecimal expectedBalance = new BigDecimal("500.00")
                .add(new BigDecimal("10.00").multiply(new BigDecimal(deposits)))
                .subtract(new BigDecimal("5.00").multiply(new BigDecimal(withdraws)));

        assertThat(finalWallet.getBalance()).isEqualByComparingTo(expectedBalance);
    }
}
