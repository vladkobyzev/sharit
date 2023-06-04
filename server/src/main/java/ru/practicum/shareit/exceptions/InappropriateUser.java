package ru.practicum.shareit.exceptions;

public class InappropriateUser extends RuntimeException {
    public InappropriateUser(String message) {
        super(message);
    }
}
