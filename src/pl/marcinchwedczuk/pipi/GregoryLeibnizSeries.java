package pl.marcinchwedczuk.pipi;

import pl.marcinchwedczuk.pipi.arith.Q10;
import pl.marcinchwedczuk.pipi.arith.Z10;

public class GregoryLeibnizSeries {
    public static void main(String[] args) {
        Q10 sum = Q10.of(0, 1);

        for (int i = 1; i <= 1000; i++) {
            Z10 num = Z10.of(4);
            if ((i & 1) == 0) num.setMinus$();

            Z10 denom = Z10.of(2*i - 1);

            sum = Q10.add(sum, new Q10(num, denom));
            System.out.println(sum.toDecimalString(20));
        }
    }
}
