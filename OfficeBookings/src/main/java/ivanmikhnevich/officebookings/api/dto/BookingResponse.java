package ivanmikhnevich.officebookings.api.dto;

import java.time.Instant;

public record BookingResponse(
        String id,
        String roomId,
        String userId,
        Instant startsAt,
        Instant endsAt
) {}