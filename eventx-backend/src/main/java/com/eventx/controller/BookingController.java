package com.eventx.controller;

import com.eventx.dto.request.LockSeatsRequest;
import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.BookingResponse;
import com.eventx.dto.response.LockSeatsResponse;
import com.eventx.service.BookingService;
import com.eventx.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/lock-seats")
    public ResponseEntity<ApiResponse<LockSeatsResponse>> lockSeats(@Valid @RequestBody LockSeatsRequest request) {
        LockSeatsResponse response = bookingService.lockSeats(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getUserBookings(Pageable pageable) {
        Page<BookingResponse> response = bookingService.getUserBookings(SecurityUtils.getCurrentUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.getBooking(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.cancelBooking(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
