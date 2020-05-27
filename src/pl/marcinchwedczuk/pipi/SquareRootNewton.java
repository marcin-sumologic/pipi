package pl.marcinchwedczuk.pipi;

import pl.marcinchwedczuk.pipi.arith.Q10;
import pl.marcinchwedczuk.pipi.arith.Z10;
import pl.marcinchwedczuk.pipi.arith.ZF10;

public class SquareRootNewton {

    public static void main(String[] args) {
        mainZF10();
    }

    public static void mainQ10() {
        int ndigits = 1_000;
        Q10 tmp = sqrt(2, ndigits);

        System.out.println("Converting to decimal...");
        String sqrt2 = tmp.toDecimalString(ndigits);

        System.out.println("Validating...");
        Sqrt2Checker.checkValid(sqrt2);

        System.out.println("SQRT2 = " + sqrt2);
    }

    public static Q10 sqrt(long value, int ndigits) {
        long estimate1M = (long)Math.sqrt(1000000L * value);
        Q10 estimate = Q10.of(estimate1M, 1000); // 1k because 1k*1k = 1M

        // Assume at least 1 correct digit.
        // If error < 1, every iteration
        // changes error -> error^2.
        //
        // We need error^N < 10^-ndigits * 0.5.
        // N log(error) < -ndigits log(10) + log(0.5)
        // N > (log(0.5) - ngitis log(10)) / log(error) [[ log(error) < 0 ]]
        //
        // notice that error^k -> iteration -> error^2k
        // We need actually ceil(log2(N)) to reach error^N.
        // log2(x) = log(x) / log(2)

        int niterations = (int)Math.ceil(log2(
                (Math.log(0.5) - ndigits) / Math.log(0.5)
        ));

        System.out.printf("SQRT ROOT ITERATIONS NEEDED FOR %d DIGITS ARE %d%n",
                ndigits, niterations);

        Q10 S = Q10.of(value);
        for (int i = 0; i < niterations; i++) {
            estimate = Q10.add(estimate, Q10.divide(S, estimate));
            estimate = Q10.divide(estimate, Q10.of(2));

            System.out.printf("SIZE " + estimate.sizeEstimate() + "%n");
            estimate = estimate.reduce();
        }

        return estimate;
    }

    public static void mainZF10() {
        int ndigits = 100_000;

        ZF10.setPrecision(ndigits);

        ZF10 tmp = sqrtZF(2, ndigits);
        String sqrt2 = tmp.toString();

        System.out.println("Validating...");
        Sqrt2Checker.checkValid(sqrt2);

        System.out.println("SQRT2 = " + sqrt2);
    }

    public static ZF10 sqrtZF(long value, int ndigits) {
        long estimate1M = (long)Math.sqrt(1000000L * value);
        ZF10 estimate = ZF10.of(estimate1M).exp10(-3); // 1k because 1k*1k = 1M

        // See comments in sqrt.
        int nIterations = (int)Math.ceil(log2(
                (Math.log(0.5) - ndigits) / Math.log(0.5)
        ));

        System.out.printf("SQRT ROOT ITERATIONS NEEDED FOR %d DIGITS ARE %d%n",
                ndigits, nIterations);

        final ZF10 HALF = ZF10.of(5).exp10(-1);
        ZF10 S = ZF10.of(value);
        for (int i = 0; i < nIterations; i++) {
            System.out.println("ITERATION " + i);

            long time = System.currentTimeMillis();
            ZF10 step1 = S.divide(estimate);
            long now = System.currentTimeMillis();
            System.out.println("divide took " + (now - time) + " ms.");

            time = System.currentTimeMillis();
            ZF10 step2 = estimate.add(step1);
            now = System.currentTimeMillis();
            System.out.println("addition took " + (now - time) + " ms.");

            time = System.currentTimeMillis();
            estimate = step2.multiply(HALF);
            now = System.currentTimeMillis();
            System.out.println("multiplication took " + (now - time) + " ms.");
        }

        return estimate;
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}
