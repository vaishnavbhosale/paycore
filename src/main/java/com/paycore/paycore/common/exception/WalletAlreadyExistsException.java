package com.paycore.paycore.common.exception;

public class WalletAlreadyExistsException extends RuntimeException {
    public WalletAlreadyExistsException(Long userId) {
        super("Wallet already exists" + userId );
    }
}

