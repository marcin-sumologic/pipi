package pl.marcinchwedczuk.pipi;

import pl.marcinchwedczuk.pipi.arith.JniZF10;
import pl.marcinchwedczuk.pipi.arith.Q10;
import pl.marcinchwedczuk.pipi.arith.Z10;
import pl.marcinchwedczuk.pipi.arith.ZF10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MachinLikeAlgorithms {
    // https://web.archive.org/web/20100619233448/http://turner.faculty.swau.edu/mathematics/materialslibrary/pi/machin.html
    // https://en.wikipedia.org/wiki/Machin-like_formula

    public static void main(String[] args) throws Exception {
        // TODO: Euler accelerated formula for arctan

        System.setProperty("java.library.path",
                System.getProperty("user.dir") + "/out/production/pipi/");
        System.out.println("Java Lib Path: " + System.getProperty("java.library.path"));
        System.out.println(JniZF10.cmpAbs(null, 0, null, 0));

        // takano(1000) <- overflow int - number of digits.
        // NWD Q to reduce number of digits.

        // takanoFormula(10_000); <- not finished

        int ndigits = 5_000;
        ZF10.setPrecision(ndigits + 50);
        String pi = Time
                .measure(() -> machinFormulaZF(ndigits))
                .substring(0, ndigits);
        PiChecker.checkValid(pi);
        System.out.println(pi);

        // 3.1415926535 8979323846 2643383279 5028841971 6939937510 5820974944 5923078164 0628620899 8628034825 3421170679
        // 3.1415926535 8979323846 2643383279 5028841971 6939937510 5820974944 5923078164 0628620899 8628034825 3421170679
        // 3.1415926535 8979323846 2643383279 5028841971 6939937510 5820974944 5923078164 0628620899 8628034825 3421170679
    }

    private static String machinFormula(int npidigits) {
        // aprox number of needed terms in series:
        int nterms5 =  arctanNterms(npidigits, 1, 5);
        int nterms239 = arctanNterms(npidigits, 1, 239);

        // https://en.wikipedia.org/wiki/Machin-like_formula
        Q10 _4arctan1$5 = Q10.multiply(arctan(Q10.of(1, 5), nterms5), Q10.of(4));
        Q10 arctan1$239 = arctan(Q10.of(1, 239), nterms239);
        Q10 delta = Q10.subtract(_4arctan1$5, arctan1$239);

        Q10 pi = Q10.multiply(Q10.of(4), delta);

        return pi.toDecimalString(npidigits);
    }

    private static String machinFormulaZF(int npidigits) {
        // aprox number of needed terms in series:
        int nterms5 =  arctanNterms(npidigits, 1, 5);
        int nterms239 = arctanNterms(npidigits, 1, 239);

        // https://en.wikipedia.org/wiki/Machin-like_formula
        ZF10 arctan1$239 = arctanZF(ZF10.frac(1, 239), nterms239, true);
        ZF10 _4arctan1$5 = arctanZF(ZF10.frac(1, 5), nterms5, true).multiply(ZF10.of(4));
        ZF10 delta = _4arctan1$5.subtract(arctan1$239);

        ZF10 pi = delta.multiply(ZF10.of(4));

        return pi.toString();
    }

    private static String takanoFormula(int npidigits) throws Exception {
        // aprox number of needed terms in series:
        int nterms49 = arctanNterms(npidigits, 1, 49);
        int nterms57 = arctanNterms(npidigits, 1, 57);
        int nterms239 = arctanNterms(npidigits, 1, 239);
        int nterms110443 = arctanNterms(npidigits, 1, 110443);

        int[] nterms = { nterms49, nterms57, nterms239, nterms110443 };
        int[] multiplicators = { 4*12, 4*32, 4*-5, 4*12 };
        int[] parts = { 49, 57, 239, 110443 };

        // run in parallel
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Q10>> results = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int index = i;
            Future<Q10> f = executor.submit(() -> {
                Q10 term = Q10.multiply(
                        Q10.of(multiplicators[index]),
                        arctan(Q10.of(1, parts[index]), nterms[index]));

                return term;
            });

            results.add(f);
        }

        Q10 pi = Q10.of(0);
        for (int i = 0; i < 4; i++) {
            pi = Q10.add(pi, results.get(i).get());
        }

        executor.shutdown();
        return pi.toDecimalString(npidigits);
    }


    private static String chienLihFormula(int npidigits) throws Exception {
        long[] multipliers =
                { 36462, 135908, 274509, -39581, 178477, -114569, -146571, 61914, -69044, -89431, -43938 };
        long[] parts =
                { 390112, 485298, 683982, 1984933, 2478328, 3449051, 18975991, 22709274, 24208144, 201229582L, 2189376182L };

        long[] nterms = Arrays.stream(parts)
                .map(p -> arctanNterms(npidigits, 1, p))
                .toArray();

        ExecutorService executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
        List<Future<Q10>> results = new ArrayList<>();

        for (int i = 0; i < multipliers.length; i++) {
            int index = i;
            Future<Q10> f = executor.submit(() -> {
                boolean reportProgress = (index == 0);
                Q10 term = Q10.multiply(
                        Q10.of(multipliers[index]),
                        arctanAccelerated(Q10.of(1, parts[index]), nterms[index], reportProgress));

                return term;
            });

            results.add(f);
        }

        Q10 pi = Q10.of(0);
        for (Future<Q10> r : results) {
            pi = Q10.add(pi, r.get());
        }
        pi = Q10.multiply(pi, Q10.of(4));

        executor.shutdown();
        return pi.toDecimalString(npidigits);
    }

    // how many terms of arctan series do I need to sum to get
    // valid ndigits of the result digits.
    private static int arctanNterms(int requiredNdigits, long a, long b) {
        return (int)Math.ceil(
            requiredNdigits * Math.log(10) / (2*Math.log(((double)b) / a))
        );
    }

    private static Q10 arctan(Q10 x, long nterms) {
        return arctan(x, nterms, false);
    }

    private static ZF10 arctanZF(ZF10 x, long nterms) {
        return arctanZF(x, nterms, false);
    }

    private static Q10 arctan(Q10 x, long nterms, boolean reportProgress) {
        final int REDUCE_EVERY_NTERMS = 1 + (int)Math.min(9, nterms / 10);

        Q10 sum = Q10.of(0);

        int k = 1;
        Q10 xk = Q10.copy(x);
        Q10 x2 = Q10.multiply(x, x);

        for (long i = 0; i < nterms; i++) {
            Z10 termNumerator = xk.numeratorCopy();
            Z10 termDenominator = Z10.multiply(
                    xk.denominatorCopy(),
                    Z10.of(k));

            if (((k/2) & 1) == 1) termNumerator.negate$();

           Q10 term = new Q10(termNumerator, termDenominator);
           sum = Q10.add(sum, term);

           k += 2;
           xk = Q10.multiply(xk, x2);

           if ((i % REDUCE_EVERY_NTERMS) == 0) {
               sum = sum.reduce();
           }

           if (reportProgress && ((i % 100) == 0)) {
               System.out.printf("PROGRESS: %.2f%%%n", (100.0f * i) / nterms);
               System.out.flush();
           }
        }

        return sum;
    }

    private static ZF10 arctanZF(ZF10 x, long nterms, boolean reportProgress) {
        ZF10 sum = ZF10.zero();

        int k = 1;
        ZF10 xk = x;
        ZF10 mx2 = x.multiply(x).multiply(ZF10.of(-1));

        for (long i = 0; i < nterms; i++) {
            sum = sum.add(xk.divide(ZF10.of(k)));

            k += 2;
            xk = xk.multiply(mx2);

            if (reportProgress && ((i % 100) == 0)) {
                System.out.printf("PROGRESS: %.2f%%%n", (100.0f * i) / nterms);
                System.out.flush();
            }
        }

        return sum;
    }

    private static Q10 arctanAccelerated(Q10 oneOverX, long nterms, boolean reportProgress) {
        // TODO: Check if nterms calculation for arctan can be used here safely
        final int REDUCE_EVERY_NTERMS = 1 + (int)Math.min(9, nterms / 10);

        // Euler's accelerated formula for tan-1 (arctan)
        if (Z10.cmp(oneOverX.numeratorCopy(), Z10.of(1)) != 0)
            throw new IllegalArgumentException("oneOverX must be a fraction in form 1/x");

        Z10 x = oneOverX.denominatorCopy();
        Q10 term = new Q10(Z10.of(1), Z10.add(Z10.of(1), Z10.multiply(x, x)));
        Q10 termPart = new Q10(Z10.of(1), Z10.add(Z10.of(1), Z10.multiply(x, x)));
        Q10 sum = Q10.copy(term);

        long k = 2;
        for (int i = 0; i < nterms; i++) {
            term = Q10.multiply(term, Q10.multiply(termPart, Q10.of(k, k+1)));
            sum = Q10.add(sum, term);
            k += 2;

            if ((i % REDUCE_EVERY_NTERMS) == 0) {
                sum = sum.reduce();
                term = term.reduce();
            }
        }

        sum = Q10.multiply(sum, new Q10(x, Z10.of(1)));
        return sum;
    }
}
