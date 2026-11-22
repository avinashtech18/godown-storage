package com.godown.storage.service;

import com.godown.storage.entity.Item;
import com.godown.storage.entity.ItemSize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class InvoicePriceCalculator {

    private static final BigDecimal RATE_PER_KG = new BigDecimal("5.00");

    public BigDecimal calculate(List<Item> items) {
        // Keep money and fractional kilograms exact throughout the calculation.
        BigDecimal total = new BigDecimal("0.00");

        for (Item item : items) {
            BigDecimal weightCharge = item.getWeightKg().multiply(RATE_PER_KG);
            total = total.add(sizeRate(item.getSize())).add(weightCharge);
        }

        // Stored weights have at most two decimal places, so no rounding is needed.
        return total.setScale(2);
    }

    private BigDecimal sizeRate(ItemSize size) {
        return switch (size) {
            case SMALL -> new BigDecimal("100.00");
            case MEDIUM -> new BigDecimal("250.00");
            case LARGE -> new BigDecimal("500.00");
        };
    }
}