package org.lpc.logfileanalysisx.utils;

import io.micrometer.common.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    public static Long getDateDiff(String startDate, String endDate, String format) {
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StringUtils.isEmpty(format) ? "yyyy-MM-dd HH:mm:ss" : format);
        LocalDateTime startTime = LocalDateTime.parse(startDate, formatter);
        LocalDateTime endTime = LocalDateTime.parse(endDate, formatter);

        return ChronoUnit.MILLIS.between(startTime, endTime);
    }
}
