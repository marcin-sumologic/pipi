package pl.marcinchwedczuk.pipi;

import pl.marcinchwedczuk.pipi.arith.Q10;
import pl.marcinchwedczuk.pipi.arith.Z10;

public class ChudnovskyAlgorithm {
    // see: https://www.craig-wood.com/nick/articles/pi-chudnovsky/

    public static void main(String[] args) throws Exception {
        String pi = Time.measure(() -> chudnovskypi(1000)); // Operation took 40 secs.
        PiChecker.checkValid(pi);
        System.out.println(pi);
    }

    private static String chudnovskypi(int ndigits) {
        // TODO: Verify citing actual whitepaper,
        // internet wisdom: 14 digits per iteration.
        int iterations = (ndigits + 14) / 14;

        long k = 0;
        Q10 ak = Q10.of(1, 1);
        Q10 asum = Q10.of(1, 1);

        Q10 bk = Q10.of(0, 1);
        Q10 bsum = Q10.of(0, 1);

        Z10 c_denom = Z10.multiply(
                Z10.of(640320),
                Z10.multiply(Z10.of(640320), Z10.of(640320)));
        Q10 c = new Q10(Z10.of(-24), c_denom);

        for (int i = 0; i < iterations; i++) {
            k++;

            Q10 tmp = new Q10(
                    Z10.of((6*k - 5)*(2*k - 1)*(6*k - 1)),
                    Z10.of(k*k*k));

            tmp = Q10.multiply(tmp, c);

            ak = Q10.multiply(ak, tmp);
            bk = Q10.multiply(Q10.of(k), ak);

            ak = ak.reduce();
            bk = bk.reduce();

            asum = Q10.add(asum, ak).reduce();
            bsum = Q10.add(bsum, bk).reduce();

            System.out.println("ITERATION...");
        }

        Q10 sqrt = SquareRootNewton.sqrt(10005, ndigits);
        sqrt = Q10.multiply(sqrt, Q10.of(426880));


        Q10 pi = Q10.divide(sqrt, Q10.add(
                Q10.multiply(Q10.of(13591409), asum),
                Q10.multiply(Q10.of(545140134L), bsum)
        ));

        return pi.toDecimalString(ndigits);
    }
}
