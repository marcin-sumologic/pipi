package pl.marcinchwedczuk.pipi.arith;

import java.util.Arrays;

public class ZF10 {
    private static int DIGITS_ARR_SIZE = 10;

    public static void setPrecision(int significantDigits) {
        DIGITS_ARR_SIZE = (significantDigits + 1) / 2;
    }

    public static ZF10 zero() { return of(0); }
    public static ZF10 one() { return of(1); }

    public static ZF10 of(long n) {
        return of(Long.toString(n));
    }

    public static ZF10 of(int... digits) {
        byte[] digitPerByte = new byte[digits.length];

        for (int i = 0; i < digits.length; i++) {
            int d = digits[i];
            if (d < 0 || d > 9) throw new IllegalArgumentException("digit outside range 0-9!");

            digitPerByte[i] = (byte)d;
        }

        return new ZF10(+1, digitPerByte);
    }

    public static ZF10 of(String s) {
        int start = 0;
        int sign = 1;

        if (s.charAt(0) == '-') {
            start++;
            sign = -1;
        }

        byte[] digitsPerByte = new  byte[s.length() - start];

        for (int i = start, k = 0; i < s.length(); i++) {
            digitsPerByte[k++] = (byte)(s.charAt(i) - '0');
        }

        return new ZF10(sign, digitsPerByte);
    }

    public static ZF10 frac(long numerator, long denominator) {
        return ZF10.of(numerator).divide(ZF10.of(denominator));
    }

    // Number 71234 will be represented as
    // [7|1] [2|3] [4|0] - assuming precision = 3
    // Logically this is a number 0.712340 x 10^5.
    // Nibble names: [hiDigit|loDigit].
    public byte[] digits;

    // Exponent is always >= 0
    public int exponent;
    public int sign;

    public int digitsCount() { return DIGITS_ARR_SIZE; }

    // 0 for Most significan digit (MSD)
    public byte digitAt(int index) {
        int byteIndex = index / 2;
        int nibbleIndex = index & 1;
        return (nibbleIndex == 0)
                ? hiDigit(digits[byteIndex])
                : loDigit(digits[byteIndex]);
    }

    public ZF10(int sign, byte[] digitPerByte) {
        this.digits = new byte[DIGITS_ARR_SIZE];
        this.sign = 0;
        this.exponent = 0;

        loadDigits(sign, digitPerByte);
    }

    private ZF10(int sign, byte[] digits, int exponent) {
        this.sign = sign;
        this.exponent = exponent;
        this.digits = digits;
    }

    private void loadDigits(int sign, byte[] digitPerByte) {
        this.exponent = digitPerByte.length;
        int in = 0;

        // Skip leading zeros
        while (in < digitPerByte.length) {
            if (digitPerByte[in] == 0) {
                this.exponent--;
                in++;
            }
            else {
                break;
            }
        }

        this.sign = (in == digitPerByte.length)
                ? 1 // number zero
                : (sign >= 0) ? 1 : -1;

        for (int k = 0; k < this.digits.length; k++) {
            if (in >= digitPerByte.length) break;
            checkValidDigit(digitPerByte[in]);
            setHiDigit(this.digits, k, digitPerByte[in++]);

            if (in >= digitPerByte.length) break;
            checkValidDigit(digitPerByte[in]);
            setLoDigit(this.digits, k, digitPerByte[in++]);
        }
    }

    public ZF10 negate() {
        return new ZF10(-sign, digits, exponent);
    }

    public ZF10 abs() {
        return new ZF10(1, digits, exponent);
    }

    public ZF10 exp10(int exp) {
        return new ZF10(sign, digits, exponent + exp);
    }

    public static int cmp(ZF10 a, ZF10 b) {
        if (a.sign != b.sign) {
            return a.sign - b.sign;
        }

        int sign = a.sign;
        return sign * cmpAbs(
                a.digits, a.exponent,
                b.digits, b.exponent);
    }

    private static int cmpAbs(byte[] aDigits, int aExponent,
                              byte[] bDigits, int bExponent) {

        // Fast track cmp(exp) when first digit is non zero
        if (aExponent != bExponent) {
            // number is either in form 0.dddd x 10^exp
            // where first d != 0, or in form
            // 0.00000 x 10^0 (for zero).

            boolean aNonZero = hiDigit(aDigits[0]) != 0;
            boolean bNonZero = hiDigit(bDigits[0]) != 0;

            if (aNonZero && bNonZero) {
                return (aExponent - bExponent);
            }
            else if (aNonZero) {
                return 1;
            }
            else if(bNonZero) {
                return (-1);
            }
            else {
                throw new AssertionError("cannot happen");
            }
        }

        for (int i = 0; i < DIGITS_ARR_SIZE; i++) {
            int cmp = hiDigit(aDigits[i]) - hiDigit(bDigits[i]);
            if (cmp != 0) return cmp;

            cmp = loDigit(aDigits[i]) - loDigit(bDigits[i]);
            if (cmp != 0) return cmp;
        }

        return 0;
    }

    public boolean isZero() {
        return (this.exponent == 0) && isZero(this.digits);
    }

    static boolean isZero(byte[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) return false;
        }

        return true;
    }

    public ZF10 subtract(ZF10 other) {
        return this.add(other.negate());
    }

    public ZF10 add(ZF10 other) {
        if (this.sign == other.sign) {
            DigitsExponent sum = addAbs(
                    this.digits, this.exponent,
                    other.digits, other.exponent);

            return new ZF10(this.sign, sum.digits, sum.exponent);
        }
        else {
            // subtract
            DigitsExponent diff;
            int diffSign;

            if (cmpAbs(this.digits, this.exponent, other.digits, other.exponent) >= 0) {
                diff = subtractAbs(
                        this.digits, this.exponent,
                        other.digits, other.exponent);
                diffSign = this.sign;
            }
            else {
                diff = subtractAbs(
                        other.digits, other.exponent,
                        this.digits, this.exponent);
                diffSign = other.sign;
            }

            return new ZF10(diffSign, diff.digits, diff.exponent);
        }
    }

    // computes a - b, a >= b
    private static DigitsExponent subtractAbs(
            byte[] aDigits, int aExponent,
            byte[] bDigits, int bExponent)
    {
        int exp;

        if (aExponent > bExponent) {
            exp = aExponent;
            bDigits = shiftRight(bDigits, aExponent - bExponent);
        }
        else if(aExponent < bExponent) {
            exp = bExponent;
            aDigits = shiftRight(aDigits, bExponent - aExponent);
        }
        else {
            exp = aExponent;
        }

        byte[] result = new byte[DIGITS_ARR_SIZE];
        int borrow = 0;

        for (int i = DIGITS_ARR_SIZE-1; i >= 0; i--) {
            int rlo = (int)loDigit(aDigits[i]) - loDigit(bDigits[i]) - borrow;
            borrow = 0;
            if (rlo < 0) {
                rlo += 10;
                borrow = 1;
            }

            int rhi = (int)hiDigit(aDigits[i]) - hiDigit(bDigits[i]) - borrow;
            borrow = 0;
            if (rhi < 0) {
                rhi += 10;
                borrow = 1;
            }

            result[i] = (byte)((rhi << 4) | rlo);
        }

        if (borrow != 0) {
            throw new AssertionError("a was not <= b");
        }

        int zeros = countLeadingZeros(result);
        if (zeros == (DIGITS_ARR_SIZE*2)) {
            // zero value
            exp = 0;
        }
        else {
            result = shiftLeft(result, zeros);
            exp -= zeros;
        }

        return new DigitsExponent(result, exp);
    }

    // computes a - b*m, a >= b*m
    private static DigitsExponent divSubtractAbs(
            byte[] aDigits, int aExponent,
            byte[] bDigits, int bExponent, int m)
    {
        int exp;

        if (aExponent > bExponent) {
            exp = aExponent;
            bDigits = shiftRight(bDigits, aExponent - bExponent);
        }
        else if(aExponent < bExponent) {
            exp = bExponent;
            aDigits = shiftRight(aDigits, bExponent - aExponent);
        }
        else {
            exp = aExponent;
        }

        byte[] result = new byte[DIGITS_ARR_SIZE];
        int borrow = 0;

        for (int i = DIGITS_ARR_SIZE-1; i >= 0; i--) {
            int rlo = (int)loDigit(aDigits[i]) - m*loDigit(bDigits[i]) - borrow;
            borrow = 0;
            while (rlo < 0) {
                rlo += 10;
                borrow++;
            }

            int rhi = (int)hiDigit(aDigits[i]) - m*hiDigit(bDigits[i]) - borrow;
            borrow = 0;
            while (rhi < 0) {
                rhi += 10;
                borrow++;
            }

            result[i] = (byte)((rhi << 4) | rlo);
        }

        if (borrow != 0) {
            throw new AssertionError("a was not <= m*b");
        }

        int zeros = countLeadingZeros(result);
        if (zeros == (DIGITS_ARR_SIZE*2)) {
            // zero value
            exp = 0;
        }
        else {
            result = shiftLeft(result, zeros);
            exp -= zeros;
        }

        return new DigitsExponent(result, exp);
    }

    static int countLeadingZeros(byte[] digits) {
        int leadingZeros = 0;

        for(int i = 0; i < digits.length; i++) {
            if (digits[i] == 0) {
                leadingZeros += 2;
            }
            else if (hiDigit(digits[i]) == 0) {
                leadingZeros++;
                break;
            }
            else {
                break;
            }
        }

        return leadingZeros;
    }

    static byte[] shiftLeft(byte[] digits, int ndigits) {
        if (ndigits == 0) return digits;

        if ((ndigits & 1) == 0) {
            // Fast path - moving entire bytes (digit pairs)
            byte[] tmp = new byte[DIGITS_ARR_SIZE];
            int bytes = ndigits / 2;

            System.arraycopy(
                    digits, bytes,
                    tmp, 0, DIGITS_ARR_SIZE - bytes);

            return tmp;
        }
        else {
            // Slow path moving nibbles (single digits)

            byte[] tmp = new byte[DIGITS_ARR_SIZE];

            int srcIndex = ndigits / 2;
            int i;
            for (i = 0; srcIndex < DIGITS_ARR_SIZE-1; i++, srcIndex++) {
                tmp[i] = (byte)(
                        (loDigit(digits[srcIndex]) << 4) | hiDigit(digits[srcIndex+1]));
            }

            // Last nibble
            tmp[i] = (byte)(loDigit(digits[srcIndex]) << 4);

            return tmp;
        }
    }

    static byte[] shiftRight(byte[] digits, int ndigits) {
        return shiftRight(digits, ndigits, new byte[DIGITS_ARR_SIZE], 0);
    }

    static byte[] shiftRight(byte[] digits, int ndigits, byte[] dest, int destStart) {
        if ((ndigits & 1) == 0) {
            // Fast path - moving entire bytes (digit pairs)
            int shiftBytes = ndigits / 2;

            System.arraycopy(
                    digits, 0,
                    dest, shiftBytes + destStart, digits.length - shiftBytes);

            return dest;
        }
        else {
            int bytesCount = ndigits / 2;
            // zero first bytesCount+1 of dest
            for (int i = 0; i < bytesCount; i++) {
                dest[i] = 0;
            }

            // Move first nibble
            int destByteIndex = destStart + ndigits / 2;
            dest[destByteIndex] = hiDigit(digits[0]);

            // Now destination is aligned to full bytes
            for (int i = 0, j = 1; ++destByteIndex < DIGITS_ARR_SIZE; i++, j++) {
                byte b = (byte)((loDigit(digits[i]) << 4) | hiDigit(digits[j]));
                dest[destByteIndex] = b;
            }

            return dest;
        }
    }

    public static byte[] U10(byte[] digits) {
        byte[] tmp = new byte[DIGITS_ARR_SIZE];

        byte U10 = (byte)((9 << 4) | 9);
        for (int i = 0; i < DIGITS_ARR_SIZE; i++) {
            tmp[i] = (byte)(U10 - digits[i]);
        }

        return tmp;
    }

    private static DigitsExponent addAbs(
            byte[] aDigits, int aExponent,
            byte[] bDigits, int bExponent)
    {

        byte[] max, min;
        int minE, maxE;
        if (aExponent >= bExponent) {
            max = aDigits; maxE = aExponent;
            min = bDigits; minE = bExponent;
        }
        else {
            max = bDigits; maxE = bExponent;
            min = aDigits; minE = aExponent;
        }

        byte[] result = new byte[DIGITS_ARR_SIZE];

        // Move min to result and shift it to maxE
        int shift = maxE - minE;
        shiftRight(min, shift, result, 0);

        // Perform addition result += max
        int c = 0;
        for (int i = DIGITS_ARR_SIZE-1; i >= 0; i--) {
            int rlo = loDigit(max[i]) + loDigit(result[i]) + c;
            c = 0;
            if (rlo >= 10) {
                rlo -= 10;
                c = 1;
            }

            int rhi = hiDigit(max[i]) + hiDigit(result[i]) + c;
            c = 0;
            if (rhi >= 10) {
                rhi -= 10;
                c = 1;
            }

            result[i] = (byte)(rlo | (rhi << 4));
        }

        if (c == 0) {
            // Normalize (case adding 0.001 + 0.000)
            int zeros = countLeadingZeros(result);
            if (zeros == (DIGITS_ARR_SIZE * 2)) {
                maxE = 0;
            }
            else {
                result = shiftLeft(result, zeros);
                maxE -= zeros;
            }
            return new DigitsExponent(result, maxE);
        }
        else {
            // TODO: Currently no rounding

            // Shift result 1 digit right
            for (int i = result.length*2 - 1; i > 0; i--) {
                setDigit(result, i, getDigit(result, i-1));
            }
            // please carry as the first digit
            result[0] = (byte)(loDigit(result[0]) | (c << 4));

            return new DigitsExponent(result, maxE + 1);
        }
    }

    public ZF10 multiply(ZF10 other) {
        return multiply(this, other);
    }

    private static ZF10 multiply(ZF10 a, ZF10 b) {
        if (a.isZero() || b.isZero()) return zero();

        int sign = a.sign * b.sign;
        int exponent = a.exponent + b.exponent;

        final int RESULT_BYTES = 2*DIGITS_ARR_SIZE;
        final int RESULT_DIGITS_COUNT = 2*RESULT_BYTES;
        byte[] result = new byte[RESULT_BYTES];

        final int DIGITS_COUNT = DIGITS_ARR_SIZE*2;
        for (int bi = DIGITS_COUNT - 1; bi >= 0; bi--) {
            int c = 0;
            int bDigit = getDigit(b.digits, bi);
            int bE = (DIGITS_COUNT - 1) - bi;

            for (int ai = DIGITS_COUNT-1; ai >= 0; ai--) {
                int aE  = (DIGITS_COUNT - 1) - ai;
                int rE = aE + bE;

                int ri = (RESULT_DIGITS_COUNT - 1) - rE;
                int r = getDigit(result, ri) + bDigit * getDigit(a.digits, ai) + c;
                c = r / 10;
                setDigit(result, ri, (byte)(r % 10));

                if (c >= 10) throw new AssertionError();
            }

            int rE = bE + (DIGITS_COUNT - 1) + 1;
            int ri = (RESULT_DIGITS_COUNT - 1) - rE;
            setDigit(result, ri, (byte)c);
        }

        byte[] resultClamped = new byte[DIGITS_ARR_SIZE];
        if (hiDigit(result[0]) != 0) {
            // carry present - just clamp the value
            System.arraycopy(result, 0, resultClamped, 0, resultClamped.length);
            return new ZF10(sign, resultClamped, exponent);
        }
        else {
            // Example: 0.1 x 0.3 = 0.03

            // (INLINED) Shift one digit left
            int srcIndex = 0;
            int i;
            for (i = 0; srcIndex < DIGITS_ARR_SIZE-1; i++, srcIndex++) {
                resultClamped[i] = (byte)(
                        (loDigit(result[srcIndex]) << 4) | hiDigit(result[srcIndex+1]));
            }

            // Last nibble
            resultClamped[i] = (byte)(loDigit(result[srcIndex]) << 4);


            // Reduce exponent by 1
            return new ZF10(sign, resultClamped, exponent-1);
        }
    }

    public ZF10 divide(ZF10 other) {
        return divide(this, other);
    }

    public static ZF10 divide(ZF10 a, ZF10 b) {
        if (b.isZero()) throw new ArithmeticException("divide by zero!");

        int sign = a.sign / b.sign;
        int exponent = a.exponent - b.exponent;

        byte[] aDigits = a.digits;
        byte[] bDigits = b.digits;

        byte[] quotient = new byte[DIGITS_ARR_SIZE];

        int divExp = 0;
        int exp = 0;
        int correction = -1;
        while ((divExp < DIGITS_ARR_SIZE*2) && !isZero(aDigits)) {
            // a*10^exp >= b
            while (cmpAbs(aDigits, exp + divExp, bDigits, 0) >= 0) {
                int estimate = estimateDiv(aDigits, bDigits);

                correction = (exp == 0) ? 1 : correction;
                DigitsExponent tmp = divSubtractAbs(aDigits, exp + divExp, bDigits, 0, estimate);
                aDigits = tmp.digits;
                exp = tmp.exponent - divExp;
                setDigit(quotient, divExp, (byte)(getDigit(quotient, divExp) + estimate));
            }

            divExp++;
        }

        // 125 / 100 -> 1.25 vs 125 / 250 -> 0.50
        exponent += correction;

        // normalize
        int zeros = countLeadingZeros(quotient);
        if (zeros == (DIGITS_ARR_SIZE*2)) {
            // zero value
            exponent = 0;
        }
        else {
            quotient = shiftLeft(quotient, zeros);
            exponent -= zeros;
        }

        return new ZF10(sign, quotient, exponent);
    }

    private static int estimateDiv(byte[] a, byte[] b) {
        long aa = 0, bb = 0;

        for (int i = 0; i < 2; i++) {
            aa = aa*100 + hiDigit(a[i])*10 + loDigit(a[i]);
            bb = bb*100 + hiDigit(b[i])*10 + loDigit(b[i]);
        }

        return (int) Math.max(1, aa / (bb + 1));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.digits.length + 1 + 1);
        if (sign < 0) sb.append('-');

        if ((exponent < 0) || exponent > DIGITS_ARR_SIZE*2) {
            // Use exponent notation
            int exp = exponent - 1; // 0.DDD = D.DDD * 10^-1
            for (int di = 0; di < DIGITS_ARR_SIZE*2; di++) {
                sb.append(getDigit(digits, di));
                if (di == 0) sb.append('.');
            }
            removeLastZeros(sb);
            sb.append('E').append(exp);
            return sb.toString();
        }

        if (exponent < 1) {
            sb.append("0.");
        }

        for (int di = 0; di < DIGITS_ARR_SIZE*2; di++) {
            sb.append(getDigit(digits, di));
            if ((di+1) == this.exponent) {
                sb.append('.');
            }
        }

        // there must be a '.' in sb
        removeLastZeros(sb);

        // get rid of trailing '.'
        if (sb.charAt(sb.length()-1) == '.')
            sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

    private static void removeLastZeros(StringBuilder sb) {
        while (sb.length() > 0) {
            char lastChar = sb.charAt(sb.length()-1);
            if (lastChar == '0') {
                sb.deleteCharAt(sb.length()-1);
            }
            else {
                break;
            }
        }
    }

    private static void checkValidDigit(byte b) {
        if (b < 0  || b > 9)
            throw new IllegalArgumentException("Illegal digit " + b);
    }

    static byte getDigit(byte[] digits, int digitIndex) {
        int byteIndex = digitIndex / 2;
        int nibbleIndex = digitIndex & 1;
        byte b = digits[byteIndex];
        return (nibbleIndex == 0) ? hiDigit(b) : loDigit(b);
    }

    private static void setDigit(byte[] digits, int digitIndex, byte digit) {
        int byteIndex = digitIndex / 2;
        int nibbleIndex = digitIndex & 1;

        if (nibbleIndex == 0) {
            setHiDigit(digits, byteIndex, digit);
        }
        else {
            setLoDigit(digits, byteIndex, digit);
        }
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

    private static class DigitsExponent {
        public final byte[] digits;
        public final int exponent;

        public DigitsExponent(byte[] digits, int exponent) {
            this.digits = digits;
            this.exponent = exponent;
        }
    }
}
