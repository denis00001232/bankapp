package com.savchenko.bankapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "wallets")
public class WalletModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(
            columnDefinition = "DECIMAL(15, 2) NOT NULL CHECK (balance >= 0)"
    )
    private BigDecimal balance;   //BigDecimal используем потому что float в памяти представлен неточным числом
}
