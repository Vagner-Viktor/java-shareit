package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    Collection<Booking> findAllByItemId(Long itemId);

    Collection<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long userId, String status);

    Collection<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime localDateTime);

    Collection<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime localDateTime1, LocalDateTime localDateTime2);

    Collection<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime localDateTime);

    Collection<Booking> findAllByItemOwnerIdOrderByStartDesc(Long userId);

    Collection<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long userId, String status);

    Collection<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime localDateTime);

    Collection<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime localDateTime);

    Collection<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime localDateTime1, LocalDateTime localDateTime2);

}
