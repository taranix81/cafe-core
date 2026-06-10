package org.taranix.cafe.beans.converters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConvertersTest {

    // --- StringToIntegerConverter ---

    @Test
    @DisplayName("Integer: valid positive number")
    void integerConvertsPositive() {
        Assertions.assertEquals(42, new StringToIntegerConverter().convert("42"));
    }

    @Test
    @DisplayName("Integer: valid negative number")
    void integerConvertsNegative() {
        Assertions.assertEquals(-7, new StringToIntegerConverter().convert("-7"));
    }

    @Test
    @DisplayName("Integer: whitespace is trimmed")
    void integerTrimsWhitespace() {
        Assertions.assertEquals(10, new StringToIntegerConverter().convert(" 10 "));
    }

    @Test
    @DisplayName("Integer: null returns null")
    void integerNullReturnsNull() {
        Assertions.assertNull(new StringToIntegerConverter().convert(null));
    }

    @Test
    @DisplayName("Integer: empty string returns null")
    void integerEmptyReturnsNull() {
        Assertions.assertNull(new StringToIntegerConverter().convert(""));
    }

    @Test
    @DisplayName("Integer: non-numeric string returns null")
    void integerNonNumericReturnsNull() {
        Assertions.assertNull(new StringToIntegerConverter().convert("abc"));
    }

    @Test
    @DisplayName("Integer: overflow returns null")
    void integerOverflowReturnsNull() {
        Assertions.assertNull(new StringToIntegerConverter().convert("9999999999"));
    }

    // --- StringToLongConverter ---

    @Test
    @DisplayName("Long: valid positive number")
    void longConvertsPositive() {
        Assertions.assertEquals(42L, new StringToLongConverter().convert("42"));
    }

    @Test
    @DisplayName("Long: valid negative number")
    void longConvertsNegative() {
        Assertions.assertEquals(-7L, new StringToLongConverter().convert("-7"));
    }

    @Test
    @DisplayName("Long: whitespace is trimmed")
    void longTrimsWhitespace() {
        Assertions.assertEquals(10L, new StringToLongConverter().convert(" 10 "));
    }

    @Test
    @DisplayName("Long: null returns null")
    void longNullReturnsNull() {
        Assertions.assertNull(new StringToLongConverter().convert(null));
    }

    @Test
    @DisplayName("Long: empty string returns null")
    void longEmptyReturnsNull() {
        Assertions.assertNull(new StringToLongConverter().convert(""));
    }

    @Test
    @DisplayName("Long: non-numeric string returns null")
    void longNonNumericReturnsNull() {
        Assertions.assertNull(new StringToLongConverter().convert("abc"));
    }

    // --- StringToDoubleConverter ---

    @Test
    @DisplayName("Double: valid positive number")
    void doubleConvertsPositive() {
        Assertions.assertEquals(3.14, new StringToDoubleConverter().convert("3.14"));
    }

    @Test
    @DisplayName("Double: valid negative number")
    void doubleConvertsNegative() {
        Assertions.assertEquals(-2.5, new StringToDoubleConverter().convert("-2.5"));
    }

    @Test
    @DisplayName("Double: whitespace is trimmed")
    void doubleTrimsWhitespace() {
        Assertions.assertEquals(10.0, new StringToDoubleConverter().convert(" 10 "));
    }

    @Test
    @DisplayName("Double: null returns null")
    void doubleNullReturnsNull() {
        Assertions.assertNull(new StringToDoubleConverter().convert(null));
    }

    @Test
    @DisplayName("Double: empty string returns null")
    void doubleEmptyReturnsNull() {
        Assertions.assertNull(new StringToDoubleConverter().convert(""));
    }

    @Test
    @DisplayName("Double: non-numeric string returns null")
    void doubleNonNumericReturnsNull() {
        Assertions.assertNull(new StringToDoubleConverter().convert("abc"));
    }

    // --- StringToBooleanConverter ---

    @Test
    @DisplayName("Boolean: \"true\" returns true")
    void booleanTrueString() {
        Assertions.assertEquals(Boolean.TRUE, new StringToBooleanConverter().convert("true"));
    }

    @Test
    @DisplayName("Boolean: \"TRUE\" (uppercase) returns true")
    void booleanTrueUppercase() {
        Assertions.assertEquals(Boolean.TRUE, new StringToBooleanConverter().convert("TRUE"));
    }

    @Test
    @DisplayName("Boolean: \"false\" returns false")
    void booleanFalseString() {
        Assertions.assertEquals(Boolean.FALSE, new StringToBooleanConverter().convert("false"));
    }

    @Test
    @DisplayName("Boolean: \"yes\" (non-boolean) returns false")
    void booleanNonBooleanReturnsDefault() {
        Assertions.assertEquals(Boolean.FALSE, new StringToBooleanConverter().convert("yes"));
    }

    @Test
    @DisplayName("Boolean: null returns null")
    void booleanNullReturnsNull() {
        Assertions.assertNull(new StringToBooleanConverter().convert(null));
    }

    @Test
    @DisplayName("Boolean: whitespace is trimmed")
    void booleanTrimsWhitespace() {
        Assertions.assertEquals(Boolean.TRUE, new StringToBooleanConverter().convert(" true "));
    }
}
