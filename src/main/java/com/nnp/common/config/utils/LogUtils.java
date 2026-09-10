package com.nnp.common.config.utils;
public final class LogUtils {

    private LogUtils() {
        // Utility class
    }

    public static String sanitizeForLog(String value) {
        if (value == null) {
            return null;
        }

        return value.replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
