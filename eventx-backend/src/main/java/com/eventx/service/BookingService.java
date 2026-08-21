package com.eventx.service;

import com.eventx.dto.request.LockSeatsRequest;
import com.eventx.dto.response.BookingResponse;
import com.eventx.dto.response.LockSeatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    LockSeatsResponse lockSeats(LockSeatsRequest request, Long userId);
    BookingResponse getBooking(Long bookingId, Long userId);
    Page<BookingResponse> getUserBookings(Long userId, Pageable pageable);
    Page<BookingResponse> getAllBookings(Pageable pageable);
    BookingResponse confirmBooking(Long bookingId);
    BookingResponse cancelBooking(Long bookingId, Long userId);
}
