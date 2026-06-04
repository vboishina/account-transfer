package com.fbi.transfer.exception;

public class InsufficientFundsException extends TransferException {
    public InsufficientFundsException() {
        super("Insufficient funds in source account");
    }
}
