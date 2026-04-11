package com.nckh.genealogy.dto.response.address;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        String formattedAddress,
        String addressLine,
        String ward,
        String district,
        String city,
        String province,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        String addressType,
        String addressTypeDescription,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        boolean isPrimary,
        String description
) {}