package com.godown.storage.controller;

import com.godown.storage.dto.InvoiceResponse;
import com.godown.storage.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/bookings/{bookingId}/invoices")
public class InvoiceController {

    private final InvoiceService invoices;

    public InvoiceController(InvoiceService invoices) {
        this.invoices = invoices;
    }

    @GetMapping
    public List<InvoiceResponse> listOwnInvoices(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("bookingId") Long bookingId) {
        return invoices.listOwnInvoices(Long.valueOf(jwt.getSubject()), bookingId);
    }

    @GetMapping("/{invoiceId}")
    public InvoiceResponse getOwnInvoice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("bookingId") Long bookingId,
            @PathVariable("invoiceId") Long invoiceId) {
        return invoices.getOwnInvoice(Long.valueOf(jwt.getSubject()), bookingId, invoiceId);
    }

    @PatchMapping("/{invoiceId}/pay")
    public InvoiceResponse recordPayment(
            @PathVariable("bookingId") Long bookingId,
            @PathVariable("invoiceId") Long invoiceId) {
        return invoices.recordPayment(bookingId, invoiceId);
    }

    @PostMapping
    public InvoiceResponse generate(@PathVariable("bookingId") Long bookingId) {
        return invoices.generate(bookingId);
    }
}