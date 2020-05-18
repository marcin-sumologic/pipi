package pl.marcinchwedczuk.pipi;

import pl.marcinchwedczuk.pipi.arith.Q10;
import pl.marcinchwedczuk.pipi.arith.Z10;

public class ClassicalAlgorithms {
    public static void main(String[] args) {
        machinFormula(100);

        // 3.1415926535 8979323846 2643383279 5028841971 6939937510 5820974944 5923078164 0628620899 8628034825 3421170679
        // 3.1415926535 8979323846 2643383279 5028841971 6939937510 5820974944 5923078164 0628620899 8628034825 3421170679
    }

    private static void machinFormula(int npidigits) {
        // aprox number of needed terms in series:
        double part1$5 = machinNterms(npidigits, 1, 5);
        double part1$239 = machinNterms(npidigits, 1, 239);

        int nterms = (int)Math.ceil(part1$5 + part1$239);

        // https://en.wikipedia.org/wiki/Machin-like_formula
        Q10 _4arctan1$5 = Q10.multiply(arctan(Q10.of(1, 5), nterms), Q10.of(4));
        Q10 arctan1$239 = arctan(Q10.of(1, 239), nterms);
        Q10 delta = Q10.subtract(_4arctan1$5, arctan1$239);

        Q10 pi = Q10.multiply(Q10.of(4), delta);

        System.out.println(pi.toDecimalString(npidigits));
    }

    // returns number of terms required for arctan(a, b)
    private static double machinNterms(int npidigits, int a, int b) {
        return npidigits * Math.log(10) / (2*Math.log(((double)b) / a));
    }

    private static void alg1() {
        Q10 sum = Q10.of(0, 1);

        for (int i = 1; i <= 1000; i++) {
            Z10 num = Z10.of(4);
            if ((i & 1) == 0) num.setMinus$();

            Z10 denom = Z10.of(2*i - 1);

            sum = Q10.add(sum, new Q10(num, denom));
            System.out.println(sum.toDecimalString(20));
        }
    }

    public static void alg2() {
        Q10 sum = Q10.of(0, 1);

        Z10 twoK = Z10.of(2);
        Z10 threeK = Z10.of(3);

        for (int i = 1; i <= 100; i++) {
            int k = 2*i - 1;

            {
                // 4 * (-1)^(k+1)/{k*2^k} part
                Z10 num2 = Z10.of(4);
                if ((i & 1) == 0) num2.setMinus$();

                Z10 denom2 = Z10.multiply(Z10.of(k), twoK);

                Q10 s2 = new Q10(num2, denom2);
                twoK = Z10.multiply(twoK, Z10.of(2 * 2));

                sum = Q10.add(sum, s2);
            }

            {
                // 4 * (-1)^(k+1)/{k*3^k} part
                Z10 num3 = Z10.of(4);
                if ((i & 1) == 0) num3.setMinus$();

                Z10 denom3 = Z10.multiply(Z10.of(k), threeK);

                Q10 s3 = new Q10(num3, denom3);
                threeK = Z10.multiply(threeK, Z10.of(3 * 3));

                sum = Q10.add(sum, s3);
            }

            System.out.println(sum.toDecimalString(20));
        }
    }

    private static Q10 arctan(Q10 x, int nterms) {
        Q10 sum = Q10.of(0);

        int k = 1;
        Q10 xk = Q10.copy(x);
        Q10 x2 = Q10.multiply(x, x);

        for (int i = 0; i < nterms; i++) {
            Z10 termNumerator = xk.numeratorCopy();
            Z10 termDenominator = Z10.multiply(
                    xk.denominatorCopy(),
                    Z10.of(k));

            if (((k/2) & 1) == 1) termNumerator.negate$();

           Q10 term = new Q10(termNumerator, termDenominator);
           sum = Q10.add(sum, term);

           k += 2;
           xk = Q10.multiply(xk, x2);
        }

        return sum;
    }
}
