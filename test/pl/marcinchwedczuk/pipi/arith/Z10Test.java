package pl.marcinchwedczuk.pipi.arith;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class Z10Test {
    @Test public void toString_zero() {
        Z10 zero = Z10.newWithCapacity(1);

        assertEquals(
            "0",
            zero.toString());

        Z10 zero2 = Z10.newWithCapacity(2);
        assertEquals(
                "0",
                zero2.toString());
    }

    @Test public void toString_negative() {
        Z10 m1 = Z10.newWithCapacity(8);
        m1.setMinus$().setDigitAt$(1, 1);

        assertEquals(
                "-10",
                m1.toString()
        );
    }

    @Test public void toString_digit() {
        Z10 pi = Z10.newWithCapacity(10);
        pi.setDigitAt$(4, 3)
                .setDigitAt$(3, 1)
                .setDigitAt$(2, 4)
                .setDigitAt$(1, 1)
                .setDigitAt$(0, 5);

        assertEquals(
                "31415",
                pi.toString()
        );
    }

    @Test public void load_works() {
        Z10 n = Z10.newZero();

        n.load(12345);
        assertEquals(
                "12345",
                n.toString());

        n.load(8);
        assertEquals(
                "8",
                n.toString());

        n.load(-15);
        assertEquals(
                "-15",
                n.toString());

        n.load(1);
        assertEquals(
                "1",
                n.toString());

        n.load(0);
        assertEquals(
                "0",
                n.toString());
    }

    @Test public void add_works() {
        assertAddWorks(0, 0);
        assertAddWorks(-5, 5);

        assertAddWorks(0, 1);
        assertAddWorks(1, 0);
        assertAddWorks(128, 0);
        assertAddWorks(-128, 0);
        assertAddWorks(0, 128);
        assertAddWorks(0, -128);

        assertAddWorks(99, 999);
        assertAddWorks(22, 22);
        assertAddWorks(-99, -999);
        assertAddWorks(-22, -222);

        assertAddWorks(12345, 2345678);
    }

    @Test public void subtract_works() {
        assertSubtractWorks(0, 0);
        assertSubtractWorks(7, 7);

        assertSubtractWorks(7, 0);
        assertSubtractWorks(-7, 0);
        assertSubtractWorks(0, 7);
        assertSubtractWorks(0, -7);

        assertSubtractWorks(1111, 77);
        assertSubtractWorks(77, 1111);
        assertSubtractWorks(-77, 1111);
        assertSubtractWorks(77, -1111);
        assertSubtractWorks(-77, -1111);
        assertSubtractWorks(-1100, -11);

        assertSubtractWorks(10000, 7329);
    }

    @Test public void add_stress_test() {
        Random r = ThreadLocalRandom.current();

        for (int i = 0; i < 1000; i++) {
            long a = r.nextLong();
            long b = r.nextLong();

            try {
                assertAddWorks(a, b);
            }
            catch (AssertionError ae) { throw ae; } // junit
            catch (Exception e) {
                e.printStackTrace();
                Assert.fail(String.format(
                        "Adding %d and %d resulted in exception %s.", a, b, e));
            }
        }
    }

    @Test public void multiplication_works() {
        assertMultiplyWorks(0, 0);
        assertMultiplyWorks(1, 1);

        assertMultiplyWorks(4, 0);
        assertMultiplyWorks(-4, 0);
        assertMultiplyWorks(0, 4);
        assertMultiplyWorks(0, -4);

        assertMultiplyWorks(4, 1);
        assertMultiplyWorks(-4, 1);
        assertMultiplyWorks(1, 4);
        assertMultiplyWorks(1, -4);

        assertMultiplyWorks(2, 3);
        assertMultiplyWorks(-2, 3);
        assertMultiplyWorks(2, -3);
        assertMultiplyWorks(-2, -3);
    }

    @Test public void multiply_stress_test() {
        Random r = ThreadLocalRandom.current();

        for (int i = 0; i < 1000; i++) {
            long a = r.nextLong();
            long b = r.nextLong();

            try {
                assertMultiplyWorks(a, b);
            }
            catch (AssertionError ae) { throw ae; } // junit
            catch (Exception e) {
                e.printStackTrace();
                Assert.fail(String.format(
                        "Multiplying %d and %d resulted in exception %s.", a, b, e));
            }
        }
    }

    @Test public void division_works() {
        assertDivisionWorks(1, 1);
        assertDivisionWorks(3, 3);
        assertDivisionWorks(13, 13);
        assertDivisionWorks(13, 1);

        assertDivisionWorks(13, -13);
        assertDivisionWorks(-13, 13);
        assertDivisionWorks(-13, -13);

        assertDivisionWorks(12, 3);
        assertDivisionWorks(3, 12);
    }

    @Test public void division_stress_test() {
        Random r = ThreadLocalRandom.current();

        for (int i = 0; i < 1000; i++) {
            long a = Math.abs(r.nextLong());
            long b = Math.abs(r.nextLong());
            if (b == 0) { i--; continue; }

            try {
                assertDivisionWorks(a, b);
            }
            catch (AssertionError ae) { throw ae; } // junit
            catch (Exception e) {
                e.printStackTrace();
                Assert.fail(String.format(
                        "Dividing %d and %d resulted in exception %s.", a, b, e));
            }
        }
    }

    private void assertDivisionWorks(long a, long b) {
        Z10 za = Z10.of(a);
        Z10 zb = Z10.of(b);
        Z10[] qr = Z10.divideSlowly(za, zb);

        BigDecimal da = BigDecimal.valueOf(a);
        BigDecimal db = BigDecimal.valueOf(b);
        BigDecimal expectedQ = da.divide(db, RoundingMode.FLOOR);
        BigDecimal expectedR = da.remainder(db);

        assertEquals(
                String.format("Division %d / %d = (q: %s, r: %s), " +
                        "actual (q: %s, r: %s).",
                        a, b, qr[0], qr[1], expectedQ, expectedR),
                expectedQ.toString(), qr[0].toString());

        assertEquals(
                String.format("Division %d / %d = (q: %s, r: %s), " +
                                "actual (q: %s, r: %s).",
                        a, b, qr[0], qr[1], expectedQ, expectedR),
                expectedR.toString(), qr[1].toString());
    }


    private void assertMultiplyWorks(long a, long b) {
        Z10 za = Z10.of(a);
        Z10 zb = Z10.of(b);
        Z10 result = Z10.multiply(za, zb);

        BigDecimal da = BigDecimal.valueOf(a);
        BigDecimal db = BigDecimal.valueOf(b);
        BigDecimal expected = da.multiply(db);

        assertEquals(
                String.format("Multiplying %d * %d = %s, actual %s", a, b, expected, result),
                expected.toString(), result.toString());
    }

    private void assertAddWorks(long a, long b) {
        Z10 za = Z10.of(a);
        Z10 zb = Z10.of(b);
        Z10 result = Z10.add(za, zb);

        BigDecimal da = BigDecimal.valueOf(a);
        BigDecimal db = BigDecimal.valueOf(b);
        BigDecimal expected = da.add(db);

        assertEquals(
                String.format("Adding %d + %d = %s, actual %s", a, b, expected, result),
                expected.toString(), result.toString());
    }

    private void assertSubtractWorks(long a, long b) {
        Z10 za = Z10.of(a);
        Z10 zb = Z10.of(b);
        Z10 result = Z10.subtract(za, zb);

        BigDecimal da = BigDecimal.valueOf(a);
        BigDecimal db = BigDecimal.valueOf(b);
        BigDecimal expected = da.subtract(db);

        assertEquals(
                String.format("Subtracting %d - %d = %s, actual %s", a, b, expected, result),
                expected.toString(), result.toString());
    }
}
