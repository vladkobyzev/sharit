package ru.practicum.shareit.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
@Slf4j
public class ExceptionApiHandler {
    @ExceptionHandler(AlreadyUsedEmail.class)
    public ResponseEntity<ErrorMessage> alreadyUsedEmail(AlreadyUsedEmail e) {
        log.error("already used email: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorMessage("Already used email:" + e.getMessage()));
    }

    @ExceptionHandler({InappropriateUser.class, EntityNotFound.class})
    public ResponseEntity<ErrorMessage> notFound(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler({BadRequest.class, ItemIsUnavailable.class, BookingStatusAlreadySet.class})
    public ResponseEntity<ErrorMessage> badRequest(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(UnsupportedStatus.class)
    public ResponseEntity<ErrorResponse> unsupportedStatus(UnsupportedStatus e, HttpServletRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String timestamp = formatter.format(ZonedDateTime.now());
        String path = request.getRequestURI();
        String error = e.getReasonPhrase();
        String message = e.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse errorResponse = new ErrorResponse(timestamp, status.value(), error, message, path);
        return new ResponseEntity<>(errorResponse, status);
    }
}
