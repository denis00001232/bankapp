package com.savchenko.bankapp.mapper;

import com.savchenko.bankapp.domain.Wallet;
import com.savchenko.bankapp.model.WalletModel;

public class WalletMapper {

    public static Wallet toDomain(WalletModel entity) {
        return new Wallet(entity.getId(), entity.getBalance());
    }

    public static WalletModel toEntity(Wallet domain) {
        WalletModel entity = new WalletModel();
        entity.setId(domain.getId());
        entity.setBalance(domain.getBalance());
        return entity;
    }

    public static void updateEntityFromDomain(Wallet domain, WalletModel entity) {
        entity.setId(domain.getId());
        entity.setBalance(domain.getBalance());
    }
}
