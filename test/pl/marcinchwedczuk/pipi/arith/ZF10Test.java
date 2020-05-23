package pl.marcinchwedczuk.pipi.arith;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class ZF10Test {
    @Before public void beforeEach() {
        ZF10.setPrecision(7);
    }

    @Test public void of_works() {
        ZF10 zf;

        // 12345 = +0.12345 x 10^5
        zf = ZF10.of(12345);
        assertHasSign(zf, 1);
        assertHasDigits(zf, 1, 2, 3, 4, 5);
        assertHasExponent(zf, 5);

        // -12345 = -0.12345 x 10^5
        zf = ZF10.of(-12345);
        assertHasSign(zf, -1);
        assertHasDigits(zf, 1, 2, 3, 4, 5);
        assertHasExponent(zf, 5);

        // 1 = +0.1 x 10^1
        zf = ZF10.of(1);
        assertHasSign(zf, 1);
        assertHasDigits(zf, 1);
        assertHasExponent(zf, 1);

        // -1 = -0.1 x 10^1
        zf = ZF10.of(-1);
        assertHasSign(zf, -1);
        assertHasDigits(zf, 1);
        assertHasExponent(zf, 1);

        // +0 = +0.0 x 10^0
        zf = ZF10.of(0);
        assertHasSign(zf, 1);
        assertHasDigits(zf, 0);
        assertHasExponent(zf, 0);

        // Notice zero cannot be negative!
        // -0 = +0.0 x 10^0
        zf = ZF10.of(0);
        assertHasSign(zf, 1);
        assertHasDigits(zf, 0);
        assertHasExponent(zf, 0);

        // Trucation example (no rounding)
        zf = ZF10.of(123456789L);
        assertHasSign(zf, 1);
        assertHasDigits(zf, 1, 2, 3, 4, 5, 6, 7);
        assertHasExponent(zf, 9); // !!!
    }

    @Test public void toString_works() {
        assertEquals(
                "12345",
                ZF10.of(12345).toString()
        );

        assertEquals(
                "-12345",
                ZF10.of(-12345).toString()
        );

        assertEquals(
                "0",
                ZF10.of(0).toString()
        );

        assertEquals(
                "-2",
                ZF10.of(-2).toString()
        );
    }

    @Test public void shiftLeft_works() {
        byte[] digits = { (byte)0xAB, (byte)0xCD, (byte)0xEF, (byte)0x12 };

        byte[] actual = ZF10.shiftLeft(digits, 0);
        assertHasDigits(actual, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x1, 0x2);

        // odd width
        actual = ZF10.shiftLeft(digits, 1);
        assertHasDigits(actual, 0xB, 0xC, 0xD, 0xE, 0xF, 0x1, 0x2, 0);

        actual = ZF10.shiftLeft(digits, 3);
        assertHasDigits(actual, 0xD, 0xE, 0xF, 0x1, 0x2, 0, 0, 0);

        // even width
        actual = ZF10.shiftLeft(digits, 2);
        assertHasDigits(actual, 0xC, 0xD, 0xE, 0xF, 0x1, 0x2, 0, 0);

        actual = ZF10.shiftLeft(digits, 4);
        assertHasDigits(actual, 0xE, 0xF, 0x1, 0x2, 0, 0, 0, 0);

        // width == byte arr size
        actual = ZF10.shiftLeft(digits, 2*digits.length-1);
        assertHasDigits(actual, 0x2, 0, 0, 0, 0, 0, 0, 0);

        actual = ZF10.shiftLeft(digits, 2*digits.length);
        assertHasDigits(actual, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test public void shiftRight_works() {
        byte[] digits = { (byte)0xAB, (byte)0xCD, (byte)0xEF, (byte)0x12 };

        byte[] actual = ZF10.shiftRight(digits, 0);
        assertHasDigits(actual, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x1, 0x2);

        // odd width
        actual = ZF10.shiftRight(digits, 1);
        assertHasDigits(actual, 0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x1);

        actual = ZF10.shiftRight(digits, 3);
        assertHasDigits(actual, 0, 0, 0, 0xA, 0xB, 0xC, 0xD, 0xE);

        // even width
        actual = ZF10.shiftRight(digits, 2);
        assertHasDigits(actual, 0, 0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF);

        actual = ZF10.shiftRight(digits, 4);
        assertHasDigits(actual, 0, 0, 0, 0, 0xA, 0xB, 0xC, 0xD);

        // width == byte arr size
        actual = ZF10.shiftRight(digits, 2*digits.length-1);
        assertHasDigits(actual, 0, 0, 0, 0, 0, 0, 0, 0xA);

        actual = ZF10.shiftRight(digits, 2*digits.length);
        assertHasDigits(actual, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test public void countLeadingZeros_work() {
        int actual = ZF10.countLeadingZeros(new byte[] {
            0x11, 0x22, 0x33
        });
        assertEquals(0, actual);

        actual = ZF10.countLeadingZeros(new byte[] {
                0x01, 0x22, 0x33
        });
        assertEquals(1, actual);

        actual = ZF10.countLeadingZeros(new byte[] {
                0x00, 0x22, 0x33
        });
        assertEquals(2, actual);

        actual = ZF10.countLeadingZeros(new byte[] { 0, 0, 0 });
        assertEquals(6, actual);
    }

    @Test public void cmp_works() {
        assertCmpWorks(0, 0);
        assertCmpWorks(1, 1);
        assertCmpWorks(-1, -1);
        assertCmpWorks(223, 223);
        assertCmpWorks(-223, -223);

        assertCmpWorks(0, 123);
        assertCmpWorks(123, 0);
        assertCmpWorks(0, -123);
        assertCmpWorks(-123, 0);

        assertCmpWorks(1234, 3399);
        assertCmpWorks(3399, 1234);
        assertCmpWorks(-1234, -3399);
        assertCmpWorks(-3399, -1234);

        assertCmpWorks(130, 13);
        assertCmpWorks(13, 130);
        assertCmpWorks(-130, -13);
        assertCmpWorks(-13, -130);

        for (int i = 0; i < 1000; i++) {
            long a = ThreadLocalRandom.current().nextLong();
            long b = ThreadLocalRandom.current().nextLong();

            assertCmpWorks(a, b);
        }
    }

    @Test public void add_works() {
        assertAddWork(0, 0);
        assertAddWork(10, 0);
        assertAddWork(-10, 0);
        assertAddWork(0, 10);
        assertAddWork(0, -10);

        assertAddWork(1234, 4321);
        assertAddWork(-1234, -4321);
        assertAddWork(-1234, 4321);
        assertAddWork(1234, -4321);

        assertAddWork(9999, 7);
        assertAddWork(-9999, -7);
        assertAddWork(9999, 99999);
    }

    static void assertAddWork(long a, long b) {
        ZF10 expected = ZF10.of(a + b);
        ZF10 actual = ZF10.of(a).add(ZF10.of(b));

        String msg = String.format("%s is different than %s (sum of %d and %d).",
            actual, expected, a, b);
        assertTrue(msg, ZF10.cmp(expected, actual) == 0);
    }

    static void assertCmpWorks(long a, long b) {
        ZF10 zfa = ZF10.of(a);
        ZF10 zfb = ZF10.of(b);

        int cmp = ZF10.cmp(zfa, zfb);
        if (cmp > 0) cmp = 1;
        if (cmp < 0) cmp = -1;

        int expectedCmp = Long.compare(a, b);

        assertEquals(expectedCmp, cmp);
    }

    static void assertHasDigits(ZF10 zf, int... digits) {
        for (int i = 0; i < digits.length; i++) {
            int expected = digits[i];
            int actual =  zf.digitAt(i);

            assertEquals("Digit at position " + i + " should be " +
                    expected + " but  was " + actual + ".",
                    expected, actual);
        }

        for (int k = digits.length; k < zf.digits.length; k++) {
            int actual = zf.digitAt(k);

            assertEquals(
                    String.format("Digit at position %d should be zero but was %d.", k, actual),
                    0, actual);
        }
    }

    static void assertHasSign(ZF10 zf, int expected) {
        int actual = zf.sign;
        assertEquals(
                String.format("Expected sign %d but got %d.", expected, actual),
                expected, actual);
    }

    static void assertHasExponent(ZF10 zf, int expected) {
        int actual = zf.exponent;
        assertEquals(
                String.format("Expected sign %d but got %d.", expected, actual),
                expected, actual);
    }

    static void assertHasDigits(byte[] actualDigits, int... expectedDigits) {
        if ((2*actualDigits.length) != expectedDigits.length)
            fail(String.format(
                "different number of digits %d vs %d.", 2*actualDigits.length, expectedDigits.length));

        for (int i = 0; i < 2*actualDigits.length; i++) {
            int actualDigit = ZF10.getDigit(actualDigits, i);
            if (actualDigit != expectedDigits[i]) {
                fail(String.format(
                        "Digits at index %d are different: %d vs %d",
                        i, actualDigit, expectedDigits[i]));
            }
        }
    }
}
