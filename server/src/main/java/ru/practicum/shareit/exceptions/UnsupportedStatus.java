package ru.practicum.shareit.exceptions;

public class UnsupportedStatus extends RuntimeException {
    private final String reasonPhrase = "Unknown state: UNSUPPORTED_STATUS";

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public UnsupportedStatus(String message) {
        super(message);
    }
}
