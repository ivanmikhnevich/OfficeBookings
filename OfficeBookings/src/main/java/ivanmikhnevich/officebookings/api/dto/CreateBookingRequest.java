package ivanmikhnevich.officebookings.api.dto;

import org.antlr.v4.runtime.misc.NotNull;

import java.time.Instant;

/**
 * userId здесь СОЗНАТЕЛЬНО отсутствует.
 * @JsonIgnoreProperties(ignoreUnknown = false) заставит клиента получить 400,
 * если он пришлёт лишнее поле — это защита контракта, а не от подделки.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record CreateBookingRequest(
        @NotNull String roomId,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt
) {}