package com.eventx.service;

import com.eventx.dto.request.RefundRequest;
import com.eventx.dto.response.RefundResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RefundService {
    RefundResponse processRefund(Long bookingId, RefundRequest request, Long userId);
    List<RefundResponse> getRefundsForBooking(Long bookingId);
    Page<RefundResponse> getAllRefunds(Pageable pageable);
}
