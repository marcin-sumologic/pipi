package pl.marcinchwedczuk.pipi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PiChecker {
    public static void checkValid(String pi) {
        pi = pi.replaceAll(" ", "");

        InputStream inputStream = PiChecker.class
                .getClassLoader()
                .getResourceAsStream("pi1000000.txt");

        try(InputStream is = inputStream;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            char[] needed = new char[pi.length()];
            reader.read(needed);

            char[] actual = pi.toCharArray();
            for (int i = 0; i < actual.length; i++) {
                if (actual[i] != needed[i]) {
                    System.err.printf(
                            "ERROR: pi digit at place %d: %s != %s",
                            i,
                            actual[i],
                            needed[i]);
                    return;
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("See cause.", e);
        }
    }
}
