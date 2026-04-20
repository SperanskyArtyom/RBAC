package io.github.speranskyartyom.rbac.utils;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    private ConsoleUtils() {
        throw new AssertionError("Utility class");
    }

    public static String promptString(Scanner scanner, String message, boolean required) {
        String s;

        while (true) {
            System.out.print(ANSI_BLUE + message + ": " + ANSI_RESET);
            s = scanner.nextLine();
            if (required && s.isBlank()) {
                System.out.println(ANSI_YELLOW + "String cannot be blank! Try again." + ANSI_RESET);
            } else {
                return s;
            }
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        int number;

        while (true) {
            System.out.print(ANSI_BLUE + message + ": " + ANSI_RESET);
            String s = scanner.nextLine();

            try {
                number = Integer.parseInt(s);
                if (number < min || number > max) {
                    System.out.printf(ANSI_YELLOW + "%d not in range [%d; %d]! Try again%n" + ANSI_RESET,
                            number, min, max
                    );
                } else {
                    return number;
                }
            } catch (NumberFormatException e) {
                System.out.println(ANSI_YELLOW + s + " is not a number! Try again" + ANSI_RESET);
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        System.out.print(ANSI_BLUE + message + " (" + ANSI_GREEN +
                "y" + ANSI_BLUE + "/" + ANSI_RED + "n" + ANSI_BLUE +
                ",  n - default): " + ANSI_RESET);

        String answer = scanner.nextLine();

        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {

        if (options.isEmpty()) {
            throw new IllegalArgumentException("Options must not be empty");
        }
        System.out.println(ANSI_BLUE + message + ANSI_RESET);

        int maxLength = 0;

        for (T option : options) {
            maxLength = Math.max(maxLength, option.toString().length());
        }

        int maxNumberLength = String.valueOf(options.size() + 1).length();

        String borderFormat = "%s" + "─".repeat(maxNumberLength) + "%s" + "─".repeat(maxLength) + "%s%n";
        String format = "│%-" + maxNumberLength + "s│%-" + maxLength + "s│%n";

        System.out.printf(borderFormat, "┌", "┬", "┐");
        for (int i = 0; i < options.size(); i++) {
            System.out.printf(format, i + 1, options.get(i).toString());

            if (i != options.size() - 1) {
                System.out.printf(borderFormat, "├", "┼", "┤");
            }
        }
        System.out.printf(borderFormat, "└", "┴", "┘");

        int choice = promptInt(scanner, "Enter number of option", 1, options.size());

        return options.get(choice - 1);
    }
}
