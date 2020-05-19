package pl.marcinchwedczuk.pipi;

import pl.marcinchwedczuk.pipi.arith.Q10;
import pl.marcinchwedczuk.pipi.arith.Z10;

public class Unknown1 {
    // Found on: http://ajennings.net/blog/a-million-digits-of-pi-in-9-lines-of-javascript.html

    public static void main(String[] args) {
        int npidigits = 1000;
        long nterms = (long)Math.ceil(1.661 * npidigits);

        Q10 term1 = Q10.of(1);
        Q10 t4mk = Q10.of(1, 4);

        Q10 pi = Q10.of(1);

        int k = 1;
        for (int i = 0; i < nterms; i++) {
            term1 = Q10.multiply(term1, Q10.of(k, k+1));

            Q10 tmp = Q10.multiply(
                    term1,
                    Q10.of(1, k+2));
            tmp = Q10.multiply(tmp, t4mk);

            pi = Q10.add(pi, tmp);

            t4mk = Q10.multiply(t4mk, Q10.of(1, 4));
            k += 2;

            if ((i % 10) == 0) {
                term1 = term1.reduce();
                System.out.printf("progress: %.2f%n", ((double)i) / nterms);
            }
        }

        pi = Q10.multiply(pi, Q10.of(3));

        String piS = pi.toDecimalString(npidigits);
        PiChecker.checkValid(piS);
        System.out.println(piS);
    }
}
