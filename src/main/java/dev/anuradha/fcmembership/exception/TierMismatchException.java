package dev.anuradha.fcmembership.exception;

public class TierMismatchException extends RuntimeException{
    public TierMismatchException(String message){
        super(message);
    }
}
