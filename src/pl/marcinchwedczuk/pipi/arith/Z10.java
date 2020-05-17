package pl.marcinchwedczuk.pipi.arith;

import java.util.Arrays;

public class Z10 {
    private static final Z10 ZERO = Z10.of(0);

    public static Z10 newWithCapacity(int ndigits) {
        return new Z10(ndigits);
    }

    public static Z10 of(long number) {
        return newZero().load(number);
    }

    public static Z10 newZero() {
        return newWithCapacity(8);
    }

    public static Z10 copy(Z10 z) {
        Z10 copy = Z10.newWithCapacity(z.digitsCount());
        copy.setSign$(z.sign);
        System.arraycopy(z.digitPairs, 0, copy.digitPairs, 0, copy.digitPairs.length);
        return copy;
    }

    public static final byte SIGN_PLUS = 0;
    public static final byte SIGN_MINUS = 1;

    // Number 71234 will be represented as
    // [4|3] [2|1] [7|0] [0|0]
    // where [x|y] represents a byte with lower
    // nibble x (loDigit) and upper nibble y (hiDigit), that is (x | (y << 4)).
    private byte[] digitPairs;
    private byte sign;

    public Z10() {
        this(8);
    }

    public Z10(int ndigits) {
        if (ndigits < 1) throw new IllegalArgumentException();
        digitPairs = new byte[(ndigits + 1) / 2];
    }

    @Override
    public String toString() {
        int digitsCount = digitsCount();
        StringBuilder s = new StringBuilder(digitsCount + 1);

        if (sign == SIGN_MINUS) {
            s.append('-');
        }

        // Handle single digit at the beginning
        if ((digitsCount & 1) == 1) {
            int index = digitsCount / 2;
            char digit = (char) ('0' + loDigit(digitPairs[index]));
            s.append(digit);
        }

        for (int i = (digitsCount / 2) - 1; i >= 0; i--) {
            byte pair = digitPairs[i];
            char loDigit = (char) ('0' + loDigit(pair));
            char hiDigit = (char) ('0' + hiDigit(pair));
            s.append(hiDigit).append(loDigit);
        }

        return s.toString();
    }

    public int digitsCount() {
        int count = digitPairs.length * 2;

        int curr = digitPairs.length - 1;
        while ((curr >= 0) &&
                digitPairs[curr] == 0) {
            curr--;
            count -= 2;
        }

        // Check for a single digit in byte.
        if ((curr >= 0) && (hiDigit(digitPairs[curr]) == 0)) {
            count--;
        }

        // Zero has one digit.
        return Math.max(count, 1);
    }

    public Z10 load(long number) {
        if (number < 0) {
            setMinus$();
            number = -number; // F**k Long.MIN_VALUE
        } else {
            setPlus$();
        }

        Arrays.fill(this.digitPairs, (byte) 0);

        // 123 -> "123", in our representation  [3|2] [1|0] [lo|hi]
        char[] digits = Long.toString(number).toCharArray();
        for (int i = 0; i < digits.length; i++) {
            setDigitAt$(digits.length - 1 - i, digits[i] - '0');
        }

        return this;
    }

    public int digitAt(int index) {
        if (index < 0)
            throw new IllegalArgumentException();

        if (index >= (digitPairs.length * 2))
            return 0;

        return internalDigitAt(index);
    }

    private int internalDigitAt(int index) {
        int pairIndex = index / 2;
        int nibbleIndex = index & 1;

        byte pair = digitPairs[pairIndex];
        return (nibbleIndex == 0) ? loDigit(pair) : hiDigit(pair);
    }

    public Z10 setDigitAt$(int index, int digit) {
        if (index < 0)
            throw new IllegalArgumentException();

        if ((digit < 0) || (digit >= 10))
            throw new IllegalArgumentException();

        ensureHasCapacityForIndex(index);
        internalSetDigitAtNoResize$(index, digit);

        return this;
    }

    private void internalSetDigitAtNoResize$(int index, int digit) {
        int pairIndex = index / 2;
        int nibbleIndex = index & 1;

        if (nibbleIndex == 0) {
            setLoDigit(digitPairs, pairIndex, (byte) digit);
        } else {
            setHiDigit(digitPairs, pairIndex, (byte) digit);
        }
    }

    public boolean isZero() {
        for (int i = 0; i < digitPairs.length; i++) {
            if (digitPairs[i] != 0) return false;
        }

        return true;
    }

    public Z10 setMinus$() {
        return setSign$(SIGN_MINUS);
    }

    public Z10 setPlus$() {
        return setSign$(SIGN_PLUS);
    }

    public Z10 setSign$(int sign) {
        this.sign = (sign == 0)
                ? SIGN_PLUS
                : SIGN_MINUS;

        return this;
    }

    private void ensureHasCapacityForIndex(int digitIndex) {
        int pairIndex = (digitIndex + 1) / 2;
        if (pairIndex < digitPairs.length)
            return;

        int newSize = Math.max(pairIndex + 1, digitPairs.length * 2);
        byte[] newDigits = new byte[newSize];
        System.arraycopy(digitPairs, 0, newDigits, 0, digitPairs.length);

        this.digitPairs = newDigits;
    }

    private static byte loDigit(byte b) {
        return (byte) (b & 0x0F);
    }

    private static byte hiDigit(byte b) {
        return (byte) ((b & 0xF0) >> 4);
    }

    private static void setLoDigit(byte[] digits, int index, byte value) {
        digits[index] = (byte) ((digits[index] & 0xF0) | (value & 0x0F));
    }

    private static void setHiDigit(byte[] digits, int index, byte value) {
        digits[index] = (byte) ((digits[index] & 0x0F) | ((value << 4) & 0xF0));
    }

    // Z10 Math

    /**
     * @return 1 when a > b, -1 when a < b, 0 when a == b.
     */
    public static int cmp(Z10 a, Z10 b) {
        return cmpInternal(a, a.sign, b, b.sign);
    }

    public static int cmpAbs(Z10 a, Z10 b) {
        return cmpInternal(a, SIGN_PLUS, b, SIGN_PLUS);
    }

    public static int cmpInternal(Z10 a, int aSign, Z10 b, int bSign) {
        int signCmp = bSign - aSign;
        if (signCmp != 0) {
            if (a.isZero() && b.isZero()) return 0;
            return signCmp;
        }

        int s = 1 - 2 * aSign;
        if (a.digitPairs.length > b.digitPairs.length) {
            for (int i = b.digitPairs.length; i < a.digitPairs.length; i++) {
                if (a.digitPairs[i] != 0) return s;
            }
        } else if (b.digitPairs.length > a.digitPairs.length) {
            for (int i = a.digitPairs.length; i < b.digitPairs.length; i++) {
                if (b.digitPairs[i] != 0) return -s;
            }
        }

        int minLength = Math.min(a.digitPairs.length, b.digitPairs.length);
        for (int i = minLength - 1; i >= 0; i--) {
            byte aPair = a.digitPairs[i];
            byte bPair = b.digitPairs[i];

            int hiCmp = hiDigit(aPair) - hiDigit(bPair);
            if (hiCmp != 0) return (hiCmp > 0) ? 1 : -1;

            int loCmp = loDigit(aPair) - loDigit(bPair);
            if (loCmp != 0) return (loCmp > 0) ? 1 : -1;
        }

        return 0;
    }

    private static int negSign(int sign) {
        return (sign == SIGN_PLUS)
                ? SIGN_MINUS
                : SIGN_PLUS;
    }

    public static Z10 add(Z10 a, Z10 b) {
        if (a.sign == b.sign) {
            return addSameSign(a, b);
        } else {
            int cmp = cmpAbs(a, b);
            if (cmp == 0) return newZero();

            return (cmp > 0)
                    ? subtractIgnoreSign(a, b).setSign$(a.sign)
                    : subtractIgnoreSign(b, a).setSign$(b.sign);
        }
    }

    public static Z10 subtract(Z10 a, Z10 b) {
        if (a.sign != b.sign) {
            return addSameSign(a, b);
        } else {
            // +a - +b OR -a - -b
            int cmp = cmpAbs(a, b);
            if (cmp == 0) return newZero();

            return (cmp > 0)
                    ? subtractIgnoreSign(a, b).setSign$(a.sign)
                    : subtractIgnoreSign(b, a).setSign$(negSign(b.sign));
        }
    }

    private static Z10 addSameSign(Z10 a, Z10 b) {
        int aDigits = a.digitsCount();
        int bDigits = b.digitsCount();
        int maxDigits = Math.max(aDigits, bDigits);

        // +1 for potential carry value
        Z10 result = Z10.newWithCapacity(maxDigits + 1);

        int carry = 0;
        for (int i = 0; i < maxDigits; i++) {
            int sum = a.digitAt(i) + b.digitAt(i) + carry;
            carry = (sum >= 10) ? 1 : 0;
            sum -= carry * 10;

            result.internalSetDigitAtNoResize$(i, sum);
        }

        if (carry != 0) {
            result.internalSetDigitAtNoResize$(maxDigits, carry);
        }

        result.setSign$(a.sign);
        return result;
    }

    private static Z10 subtractIgnoreSign(Z10 greater, Z10 smaller) {
        Z10 result = Z10.copy(greater);
        subtractIgnoreSign(greater, smaller, result);
        return result;
    }

    private Z10 subtractIgnoreSign$(Z10 smaller) {
        subtractIgnoreSign(this, smaller, this);
        return this;
    }

    // greater can be used in dest position to perform subtraction in place
    private static void subtractIgnoreSign(Z10 greater, Z10 smaller, Z10 dest) {
        Z10 result = dest;

        int max = smaller.digitsCount();
        for (int i = 0; i < max; i++) {
            int d1 = result.internalDigitAt(i);
            int d2 = smaller.internalDigitAt(i);

            if (d1 < d2) {
                // borrow from next digit, result > small so this is possible
                int j = i + 1;
                while (result.internalDigitAt(j) == 0) j++;
                result.internalSetDigitAtNoResize$(
                        j, result.internalDigitAt(j) - 1);
                j--; // convert zeros -> nines
                while (j > i) {
                    result.internalSetDigitAtNoResize$(j, 9);
                    j--;
                }
                result.internalSetDigitAtNoResize$(i,
                        10 + d1 - d2);
            } else {
                result.internalSetDigitAtNoResize$(i, d1 - d2);
            }
        }
    }

    public static Z10 multiply(Z10 a, Z10 b) {
        int aDigits = a.digitsCount();
        int bDigits = b.digitsCount();

        Z10 result = Z10.newWithCapacity(aDigits * bDigits);

        for (int ai = 0; ai < aDigits; ai++) {
            // Multiply b by digit ai and add to result in place
            int ad = a.internalDigitAt(ai);
            int carry = 0;
            for (int bi = 0; bi < bDigits; bi++) {
                int r = result.internalDigitAt(ai + bi);
                r += carry + ad * b.internalDigitAt(bi);
                result.internalSetDigitAtNoResize$(ai + bi, r % 10);
                carry = r / 10;
            }
            // carry digit was not yet set by any computation so
            // we may replace `+=` with just `=`.
            result.internalSetDigitAtNoResize$(ai + bDigits, carry);
        }

        if ((a.sign != b.sign) && !result.isZero()) {
            result.setMinus$();
        }

        return result;
    }

    private void copyDigitsFromCount(Z10 src, int from, int count) {
        int j = 0;
        for (int i = from, end = from + count; i < end; i++) {
            setDigitAt$(j++, src.digitAt(i));
        }
    }

    private void shiftDropLeft(int newDigit) {
        int DC = digitsCount();

        // this is f**king slow!!!
        for (int i = DC; i >= 1; i--) {
            setDigitAt$(i, digitAt(i-1));
        }

        setDigitAt$(0, newDigit);
    }

    public static Z10[] divideSlowly(Z10 a, Z10 b) {
        if (b.isZero()) throw new ArithmeticException("divide by zero!");

        int aDC = a.digitsCount();
        int bDC = b.digitsCount();

        if (bDC > aDC) {
            return new Z10[] {
                    Z10.newZero(), Z10.copy(a)
            };
        }

        Z10 quotient = Z10.newWithCapacity(Math.max(aDC - bDC, 1));
        // +1 because we will use rest as a temp storage
        Z10 rest = Z10.newWithCapacity(bDC + 1);

        int curr = aDC - bDC;
        rest.copyDigitsFromCount(a, curr, bDC);

        while (true) {
            int cmp = cmpAbs(rest, b);
            if (cmp < 0) {
                if (curr == 0) break;
                rest.shiftDropLeft(a.digitAt(curr-1));
                curr--;
            }
            else {
                rest.subtractIgnoreSign$(b);
                quotient.setDigitAt$(curr,
                        quotient.digitAt(curr) + 1);
            }
        }

        if (a.sign != b.sign) {
            quotient.setMinus$();
        }

        return new Z10[]{quotient, rest};
    }

    public static Z10 gcd(Z10 a, Z10 b) {
        return (cmp(a, b) > 0)
                ? gcd0(a, b)
                : gcd0(b, a);
    }

    private static Z10 gcd0(Z10 bigger, Z10 smaller) {
        if (cmpAbs(smaller, ZERO) == 0) return bigger;
        return gcd0(smaller, divideSlowly(bigger, smaller)[1]);
    }
}
