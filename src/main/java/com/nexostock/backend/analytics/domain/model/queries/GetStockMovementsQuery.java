package com.nexostock.backend.analytics.domain.model.queries;

import java.time.LocalDate;

/**
 * Stock movements of every product of the business in a period. Null dates leave the range open.
 */
public record GetStockMovementsQuery(Long businessId, LocalDate from, LocalDate to) {
}
