package com.fbi.transfer.exception;

public class DailyLimitExceededException extends TransferException {
    public DailyLimitExceededException() {
        super("Daily transfer limit exceeded");
    }
}