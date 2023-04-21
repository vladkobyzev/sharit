package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;
	private static final String USER_ID = "X-Sharer-User-Id";

	@GetMapping
	public ResponseEntity<Object> getALLUserBookings(@RequestHeader(USER_ID) long userId,
			@RequestParam(name = "state", required = false) String stateParam,
			@PositiveOrZero @RequestParam(name = "from", required = false) Integer from,
			@Positive @RequestParam(name = "size", required = false) Integer size) {
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getAllUserBookings(userId, stateParam, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllOwnerBookings(@RequestHeader(value = USER_ID) long userId,
															@RequestParam(name = "from", required = false) Integer from,
															@RequestParam(name = "size", required = false) Integer size,
															@RequestParam(name = "state",
																	required = false, defaultValue = "ALL") String stateParam) {
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getOwnerBookings(userId, stateParam, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> createBooking(@RequestHeader(USER_ID) long userId,
												@RequestBody @Valid BookItemRequestDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.createBooking(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateBookingStatus(@PathVariable long bookingId,
													  @RequestParam(name = "approved") String approved,
													  @RequestHeader(value = USER_ID) long userId) {
		log.info("Update booking status {}, approved={}, userId={}", bookingId, approved, userId);
		return bookingClient.updateBookingStatus(bookingId, approved, userId);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader(USER_ID) long userId,
			@PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}
}
