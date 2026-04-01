package com.movierental.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class StorageCodec {

    private StorageCodec() {
    }

    public static String toCsv(Object... values) {
        StringJoiner joiner = new StringJoiner(",");
        for (Object value : values) {
            joiner.add(escape(value == null ? "" : String.valueOf(value)));
        }
        return joiner.toString();
    }

    public static List<String> parseCsv(String line) {
        List<String> values = new ArrayList<>();
        if (line == null || line.isEmpty()) {
            return values;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int index = 0; index < line.length(); index++) {
            char currentCharacter = line.charAt(index);
            if (currentCharacter == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentCharacter == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentCharacter);
            }
        }
        values.add(current.toString());
        return values;
    }

    public static boolean isCompleteCsvRecord(String line) {
        if (line == null || line.isEmpty()) {
            return true;
        }

        boolean inQuotes = false;
        for (int index = 0; index < line.length(); index++) {
            char currentCharacter = line.charAt(index);
            if (currentCharacter == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            }
        }
        return !inQuotes;
    }

    private static String escape(String value) {
        String escapedValue = value.replace("\"", "\"\"");
        if (escapedValue.contains(",") || escapedValue.contains("\"")
                || escapedValue.contains("\n") || escapedValue.contains("\r")) {
            return "\"" + escapedValue + "\"";
        }
        return escapedValue;
    }
}
