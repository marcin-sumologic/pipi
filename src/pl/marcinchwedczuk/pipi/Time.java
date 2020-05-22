package pl.marcinchwedczuk.pipi;


import java.util.function.Supplier;

@FunctionalInterface
interface ExceptionFriendlySupplier<T> {
    T get() throws Exception;
}

public class Time {
    public static <T> T measure(ExceptionFriendlySupplier<T> producer) throws Exception {
        long startTime = System.currentTimeMillis();
        T result = producer.get();
        long stopTime = System.currentTimeMillis();

        long totalSeconds = (stopTime - startTime) / 1000;
        System.out.println("Operation took " + totalSeconds + " secs.");

        return result;
    }
}
