package io.github.speranskyartyom.rbac.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }


    // ==================== promptString ====================

    @ParameterizedTest
    @CsvSource({
            "hello, hello",
            "'\n', ''",
            "'   ', '   '",
            "'first\nsecond', first"
    })
    @DisplayName("Should return the entered string (non-required case)")
    void promptString_NotRequired_ReturnsInput(String inputSequence, String expected) {
        Scanner scanner = new Scanner(inputSequence.replace("\\n", "\n"));
        String actual = ConsoleUtils.promptString(scanner, "Test message", false);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should retry when required and input is blank, then return non-blank")
    void promptString_Required_RetriesOnBlank() {
        String input = "\n   \nvalid\n";
        Scanner scanner = new Scanner(input);
        String result = ConsoleUtils.promptString(scanner, "Enter value", true);
        assertEquals("valid", result);
    }

    @Test
    @DisplayName("Should return immediately when required and input is non-blank")
    void promptString_Required_NonBlank_ReturnsImmediately() {
        Scanner scanner = new Scanner("good\n");
        String result = ConsoleUtils.promptString(scanner, "Enter", true);
        assertEquals("good", result);
    }

    // ==================== promptInt ====================

    @ParameterizedTest
    @CsvSource({
            "5, 1, 10, 5",  // valid within range
            "1, 1, 10, 1",  // min boundary
            "10, 1, 10, 10" // max boundary
    })
    @DisplayName("Should return valid integer when input is correct")
    void promptInt_ValidInput_ReturnsNumber(String input, int min, int max, int expected) {
        Scanner scanner = new Scanner(input + "\n");
        int actual = ConsoleUtils.promptInt(scanner, "Enter number", min, max);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should retry on out-of-range and then accept valid number")
    void promptInt_OutOfRangeThenValid_ReturnsValid() {
        Scanner scanner = new Scanner("20\n5\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number", 1, 10);
        assertEquals(5, result);
    }

    @Test
    @DisplayName("Should retry on non-numeric input and then accept valid number")
    void promptInt_NonNumericThenValid_ReturnsValid() {
        Scanner scanner = new Scanner("abc\n7\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number", 1, 10);
        assertEquals(7, result);
    }

    @Test
    @DisplayName("Should handle multiple errors before valid input")
    void promptInt_MultipleErrorsThenValid_ReturnsValid() {
        Scanner scanner = new Scanner("99\nxyz\n3\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter", 1, 5);
        assertEquals(3, result);
    }

    // ==================== promptYesNo ====================

    @ParameterizedTest
    @ValueSource(strings = {"y", "yes", "Y", "YES", "yEs"})
    @DisplayName("Should return true for affirmative answers")
    void promptYesNo_Affirmative_ReturnsTrue(String affirmative) {
        Scanner scanner = new Scanner(affirmative + "\n");
        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"n", "no", "N", "NO", "", "maybe", "abc", " "})
    @DisplayName("Should return false for non-affirmative answers (including empty)")
    void promptYesNo_NonAffirmative_ReturnsFalse(String nonAffirmative) {
        Scanner scanner = new Scanner(nonAffirmative + "\n");
        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");
        assertFalse(result);
    }

    // ==================== promptChoice ====================

    @Test
    @DisplayName("Should throw IllegalArgumentException when options list is empty")
    void promptChoice_EmptyOptions_ThrowsException() {
        Scanner scanner = new Scanner("\n");
        assertThrows(IllegalArgumentException.class,
                () -> ConsoleUtils.promptChoice(scanner, "Choose", List.of()));
    }

    @Test
    @DisplayName("Should return the selected option when valid number is entered")
    void promptChoice_ValidChoice_ReturnsOption() {
        List<String> options = List.of("Apple", "Banana", "Cherry");
        // User chooses 2 -> "Banana"
        Scanner scanner = new Scanner("2\n");
        String result = ConsoleUtils.promptChoice(scanner, "Pick a fruit", options);
        assertEquals("Banana", result);
    }
}