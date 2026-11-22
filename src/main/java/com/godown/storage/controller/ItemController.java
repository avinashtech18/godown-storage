package com.godown.storage.controller;

import com.godown.storage.dto.CreateItemRequest;
import com.godown.storage.dto.ItemResponse;
import com.godown.storage.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/bookings/{bookingId}/items")
public class ItemController {

    private final ItemService items;

    public ItemController(ItemService items) {
        this.items = items;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("bookingId") Long bookingId,
            @Valid @RequestBody CreateItemRequest request) {
        return items.create(Long.valueOf(jwt.getSubject()), bookingId, request);
    }

    @GetMapping
    public List<ItemResponse> listOwnItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("bookingId") Long bookingId) {
        return items.listOwnItems(Long.valueOf(jwt.getSubject()), bookingId);
    }
}