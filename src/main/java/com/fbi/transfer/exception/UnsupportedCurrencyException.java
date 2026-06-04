package com.fbi.transfer.exception;

public class UnsupportedCurrencyException extends TransferException {
    public UnsupportedCurrencyException() {
        super("Unsupported currency pair");
    }
}
