package pl.marcinchwedczuk.pipi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Sqrt2Checker {
    public static void checkValid(String sqrt) {
        sqrt = sqrt.replaceAll(" ", "");

        InputStream inputStream = Sqrt2Checker.class
                .getClassLoader()
                .getResourceAsStream("sqrt2.txt");

        try(InputStream is = inputStream;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            char[] needed = new char[sqrt.length()];
            reader.read(needed);

            char[] actual = sqrt.toCharArray();
            for (int i = 0; i < actual.length; i++) {
                if (actual[i] != needed[i]) {
                    System.err.printf(
                            "ERROR: sqrt(2) digit at place %d: %s != %s",
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
