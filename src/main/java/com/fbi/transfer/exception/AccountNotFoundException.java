package com.fbi.transfer.exception;

public class AccountNotFoundException extends TransferException {
    public AccountNotFoundException() {
        super("Account not found");
    }
}

