package pl.marcinchwedczuk.pipi.arith;

public class Q10 {
    public static Q10 of(long numerator, long denominator) {
        return new Q10(Z10.of(numerator), Z10.of(denominator));
    }

    // The sign of numerator determines sign of the fraction.
    private final Z10 numerator;
    private final Z10 denominator;

    public Q10(Z10 numerator, Z10 denominator) {
        if (denominator.isZero()) throw new ArithmeticException("divide by zero!");

        this.numerator = numerator;
        this.denominator = denominator;

        if (denominator.sign() == Z10.SIGN_MINUS) {
            this.numerator.negate$();
            this.denominator.negate$();
        }
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
}
