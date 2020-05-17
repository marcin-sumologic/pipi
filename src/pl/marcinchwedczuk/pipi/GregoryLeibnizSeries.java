package pl.marcinchwedczuk.pipi;

import pl.marcinchwedczuk.pipi.arith.Q10;
import pl.marcinchwedczuk.pipi.arith.Z10;

public class GregoryLeibnizSeries {
    public static void main(String[] args) {
        alg2();
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
}
