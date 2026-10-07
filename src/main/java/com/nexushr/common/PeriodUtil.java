package com.nexushr.common;

import com.nexushr.common.exception.BadRequestException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public final class PeriodUtil {

    private PeriodUtil() {
    }

    /** Parse a {@code yyyy-MM} string, defaulting to the current month when blank. */
    public static YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid month '" + month + "', expected format yyyy-MM");
        }
    }
}
