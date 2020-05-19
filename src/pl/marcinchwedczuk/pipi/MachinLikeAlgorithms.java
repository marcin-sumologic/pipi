package pl.marcinchwedczuk.pipi;

import pl.marcinchwedczuk.pipi.arith.Q10;
import pl.marcinchwedczuk.pipi.arith.Z10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MachinLikeAlgorithms {
    public static void main(String[] args) throws Exception {
        // takano(1000) <- overflow int - number of digits.
        // NWD Q to reduce number of digits.

        // takanoFormula(10_000); <- not finished
        String pi = chienLihFormula(10_000);
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

    // TODO: Hwang Chien-Lih formula
    // https://en.wikipedia.org/wiki/Machin-like_formula
    /*
    {\displaystyle {\begin{aligned}{\frac {\pi }{4}}=&36462\arctan {\frac {1}{390112}}+135908\arctan {\frac {1}{485298}}+274509\arctan {\frac {1}{683982}}\\&-39581\arctan {\frac {1}{1984933}}+178477\arctan {\frac {1}{2478328}}-114569\arctan {\frac {1}{3449051}}\\&-146571\arctan {\frac {1}{18975991}}+61914\arctan {\frac {1}{22709274}}-69044\arctan {\frac {1}{24208144}}\\&-89431\arctan {\frac {1}{201229582}}-43938\arctan {\frac {1}{2189376182}}\\\end{aligned}}}
     */

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
                        arctan(Q10.of(1, parts[index]), nterms[index], reportProgress));

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

    private static Q10 arctan(Q10 x, long nterms, boolean reportProgress) {
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

           if (reportProgress && ((i % 100) == 0)) {
               System.out.printf("PROGRESS: %.2f%n", ((double)i) / nterms);
               System.out.flush();
           }
        }

        return sum;
    }
}
