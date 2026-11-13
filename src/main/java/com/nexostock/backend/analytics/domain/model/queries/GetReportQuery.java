package com.nexostock.backend.analytics.domain.model.queries;

import java.time.LocalDate;

/**
 * Totals and stock movements of a period (US25, M18). Null dates leave the range open.
 */
public record GetReportQuery(Long businessId, LocalDate from, LocalDate to) {
}
