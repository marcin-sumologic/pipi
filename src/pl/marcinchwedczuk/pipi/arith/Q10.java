package pl.marcinchwedczuk.pipi.arith;

public class Q10 {
    public static Q10 of(long number) {
        return of(number, 1);
    }

    public static Q10 of(long numerator, long denominator) {
        return new Q10(Z10.of(numerator), Z10.of(denominator));
    }

    public static Q10 copy(Q10 other) {
        return new Q10(other.numerator, other.denominator);
    }

    // The sign of numerator determines sign of the fraction.
    private final Z10 numerator;
    private final Z10 denominator;

    public Z10 numeratorCopy() { return Z10.copy(numerator); }
    public Z10 denominatorCopy() { return Z10.copy(denominator); }

    public Q10(Z10 numerator, Z10 denominator) {
        if (denominator.isZero()) throw new ArithmeticException("divide by zero!");

        this.numerator = numerator;
        this.denominator = denominator;

        if (denominator.sign() == Z10.SIGN_MINUS) {
            this.numerator.negate$();
            this.denominator.negate$();
        }
    }

    public Q10 reduce() {
        Z10 gcd = Z10.gcd(numerator, denominator);

        return new Q10(
            Z10.divideSlowly(numerator, gcd)[0],
            Z10.divideSlowly(denominator, gcd)[0]);
    }

    @Override
    public String toString() {
        return numerator.toString() + "/" + denominator.toString();
    }

    public String toDecimalString(int maxFractionDigits) {
        Z10[] qr = Z10.divideSlowly(numerator, denominator);

        StringBuilder s = new StringBuilder();
        s.append(qr[0].toString());

        // fraction digits
        if (!qr[1].isZero()) {
            s.append('.');
            Z10 rest = qr[1];
            for (int i = 0; i < maxFractionDigits; i++) {
                if ((i > 0) && ((i % 10) == 0)) s.append(' ');

                // TODO: x10 multiply can be speed up
                rest = Z10.multiply(rest, Z10.of(10));
                qr = Z10.divideSlowly(rest, denominator);
                s.append(qr[0].toString());
                rest = qr[1];

                if (rest.isZero()) break;
            }
        }

        return s.toString();
    }

    public static Q10 add(Q10 a, Q10 b) {
        Z10 commonDenom = Z10.multiply(a.denominator, b.denominator);
        Z10 numerator = Z10.add(
                Z10.multiply(a.numerator, b.denominator),
                Z10.multiply(b.numerator, a.denominator));

        return new Q10(numerator, commonDenom);
    }

    public static Q10 subtract(Q10 a, Q10 b) {
        Z10 commonDenom = Z10.multiply(a.denominator, b.denominator);
        Z10 numerator = Z10.subtract(
                Z10.multiply(a.numerator, b.denominator),
                Z10.multiply(b.numerator, a.denominator));

        return new Q10(numerator, commonDenom);
    }

    public static Q10 multiply(Q10 a, Q10 b) {
        Z10 numerator = Z10.multiply(a.numerator, b.numerator);
        Z10 denominator = Z10.multiply(a.denominator, b.denominator);
        return new Q10(numerator, denominator);
    }
}
