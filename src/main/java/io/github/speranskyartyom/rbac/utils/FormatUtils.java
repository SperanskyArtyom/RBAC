package io.github.speranskyartyom.rbac.utils;

import java.util.List;
import java.util.stream.IntStream;

public class FormatUtils {
    private FormatUtils() {
        throw new AssertionError("Utility class");
    }

    public static String formatBox(String text) {
        return "┌" + "─".repeat(text.length()) + "┐\n" +
                "│" + text + "│\n" +
                "└" + "─".repeat(text.length()) + "┘";
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (maxLength <= 3) return ".".repeat(Math.max(0, maxLength));
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return truncate(text, length);
        return " ".repeat(length - text.length()) + text;
    }

    public static String padRight(String text, int length) {
        if (text.length() > length) return truncate(text, length);
        return text + " ".repeat(length - text.length());
    }

    public static String formatTable(String[] headers, List<String[]> rows) {
        boolean hasHeaders = headers != null;
        boolean hasRows = rows != null && !rows.isEmpty();

        if (!hasHeaders && !hasRows) {
            return "";
        }

        int columnCount = hasHeaders ? headers.length : rows.getFirst().length;

        if (hasRows) {
            for (String[] row : rows) {
                if (row.length != columnCount) {
                    throw new IllegalArgumentException("All rows must contain " + columnCount + " columns");
                }
            }
        }

        int[] maxLength = IntStream.range(0, columnCount)
                .map(i -> {
                    int headerLen = hasHeaders ? headers[i].length() : 0;

                    int rowsMaxLen = hasRows ? rows.stream()
                            .mapToInt(row -> row[i].length())
                            .max()
                            .orElse(0) : 0;

                    return Math.max(headerLen, rowsMaxLen);
                })
                .toArray();

        StringBuilder sb = new StringBuilder();

        sb.append('┌');

        for (int length : maxLength) {
            sb.append("─".repeat(length)).append('┬');
        }
        sb.setCharAt(sb.length() - 1, '┐');
        sb.append('\n');

        if (hasHeaders) {
            sb.append('│');

            int i = 0;
            for (String header : headers) {
                sb.append(padRight(header, maxLength[i])).append('│');
                i++;
            }
            sb.append('\n');

            if (hasRows) {
                sb.append('├');
                for (int length : maxLength) {
                    sb.append("─".repeat(length)).append('┼');
                }
                sb.setCharAt(sb.length() - 1, '┤');
                sb.append('\n');
            }
        }

        if (hasRows) {
            for (var row : rows) {
                sb.append('│');

                int i = 0;
                for (String cell : row) {
                    sb.append(padRight(cell, maxLength[i])).append('│');
                    i++;
                }
                sb.append('\n');
            }
        }

        sb.append('└');
        for (int length : maxLength) {
            sb.append("─".repeat(length)).append('┴');
        }
        sb.setCharAt(sb.length() - 1, '┘');

        return sb.toString();
    }

    public static String formatHeader(String text) {
        final String ANSI_BLUE = "\u001B[34m";
        final String ANSI_RESET = "\u001B[0m";

        return "\n===" + ANSI_BLUE + text + ANSI_RESET + "===\n";
    }
}
