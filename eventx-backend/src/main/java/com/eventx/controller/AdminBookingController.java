package com.eventx.controller;

import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.BookingResponse;
import com.eventx.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Bookings - Admin")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(Pageable pageable) {
        Page<BookingResponse> response = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.getBooking(id, null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
