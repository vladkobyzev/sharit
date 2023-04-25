package ru.practicum.shareit.booking.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDate;
import ru.practicum.shareit.util.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND (:state = 'CURRENT' AND :now BETWEEN b.start AND b.end " +
            "OR :state = 'PAST' AND b.end < :now " +
            "OR :state = 'FUTURE' AND b.start > :now " +
            "OR :state = 'WAITING' AND b.status = 'WAITING' " +
            "OR :state = 'REJECTED' AND b.status = 'REJECTED' " +
            "OR :state = 'ALL') " +
            "ORDER BY b.start DESC")
    List<Booking> findAllUserBookingsByState(@Param("userId") Long userId, @Param("state") String state, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner = :ownerId " +
            "AND (:state = 'CURRENT' AND :now BETWEEN b.start AND b.end " +
            "OR :state = 'PAST' AND b.end < :now " +
            "OR :state = 'FUTURE' AND b.start > :now " +
            "OR :state = 'WAITING' AND b.status = 'WAITING' " +
            "OR :state = 'REJECTED' AND b.status = 'REJECTED' " +
            "OR :state = 'ALL') " +
            "ORDER BY b.start DESC")
    List<Booking> findAllOwnerBookingsByState(@Param("ownerId") Long ownerId, @Param("state") String state, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND (:state = 'CURRENT' AND :now BETWEEN b.start AND b.end " +
            "OR :state = 'PAST' AND b.end < :now " +
            "OR :state = 'FUTURE' AND b.start > :now " +
            "OR :state = 'WAITING' AND b.status = 'WAITING' " +
            "OR :state = 'REJECTED' AND b.status = 'REJECTED' " +
            "OR :state = 'ALL') " +
            "ORDER BY b.start DESC")
    Slice<Booking> findAllUserBookingsByState(@Param("userId") Long userId, @Param("state") String state, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner = :ownerId " +
            "AND (:state = 'CURRENT' AND :now  BETWEEN b.start AND b.end " +
            "OR :state = 'PAST' AND b.end < :now " +
            "OR :state = 'FUTURE' AND b.start > :now " +
            "OR :state = 'WAITING' AND b.status = 'WAITING' " +
            "OR :state = 'REJECTED' AND b.status = 'REJECTED' " +
            "OR :state = 'ALL') " +
            "ORDER BY b.start DESC")
    Slice<Booking> findAllOwnerBookingsByState(@Param("ownerId") Long ownerId, @Param("state") String state, @Param("now") LocalDateTime now, Pageable pageable);

    @Query(value = "SELECT b.id, b.start_date AS bookingDate, b.booker_id AS bookerId " +
            "FROM bookings b WHERE b.item_id = ?1 AND b.start_date < ?2 " +
            "ORDER BY b.start_date DESC LIMIT 1", nativeQuery = true)
    BookingDate findLastBooking(Long itemId, LocalDateTime currentTime);

    @Query(value = "SELECT b.id, b.start_date AS bookingDate, b.booker_id AS bookerId " +
            "FROM bookings b WHERE b.item_id = ?1 AND b.start_date > ?2 AND NOT b.status = 'REJECTED'" +
            "ORDER BY b.start_date LIMIT 1", nativeQuery = true)
    BookingDate findNextBooking(Long itemId, LocalDateTime currentTime);

    boolean existsBookingByBooker_IdAndItem_IdAndStatusAndStartBefore(Long userId, Long itemId, BookingStatus status, LocalDateTime startDate);

    @Query(value = "SELECT b.id, b.start_date AS bookingDate, b.booker_id AS bookerId " +
            "FROM bookings b WHERE b.item_id IN (?1) AND b.start_date > ?2 AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.start_date LIMIT 1", nativeQuery = true)
    List<BookingDate> findAllNextBooking(List<Long> itemsId, LocalDateTime currentTime);

    @Query(value = "SELECT b.id, b.start_date AS bookingDate, b.booker_id AS bookerId " +
            "FROM bookings b WHERE b.item_id IN (?1) AND b.start_date < ?2 " +
            "ORDER BY b.start_date DESC LIMIT 1", nativeQuery = true)
    List<BookingDate> findAllLastBooking(List<Long> itemsId, LocalDateTime currentTime);
}
