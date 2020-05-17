package pl.marcinchwedczuk.pipi.arith;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class Q10Test {
    @Test public void toDecimalDigit_works() {
        Q10 q = Q10.of(4, 3);
        assertEquals("1.3333333", q.toDecimalString(7));

        q = Q10.of(1, 5);
        assertEquals("0.2", q.toDecimalString(7));

        q = Q10.of(120, 2);
        assertEquals("60", q.toDecimalString(7));

        q = Q10.of(31415, 10000);
        assertEquals("3.1415", q.toDecimalString(7));
        assertEquals("3.14", q.toDecimalString(2));
    }
}
