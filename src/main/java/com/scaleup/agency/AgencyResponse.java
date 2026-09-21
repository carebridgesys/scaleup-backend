package com.scaleup.agency;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgencyResponse(
        UUID publicId,
        String name,
        String slug,
        String highLevelLocationId,
        boolean highLevelSyncEnabled,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}