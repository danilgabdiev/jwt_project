package ru.auth.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String msg) { super(msg); }
}
