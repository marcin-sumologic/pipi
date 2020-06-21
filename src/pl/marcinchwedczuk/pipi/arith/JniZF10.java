package pl.marcinchwedczuk.pipi.arith;

// Docs: http://homepage.cs.uiowa.edu/~jones/bcd/bcd.html#packed
// https://en.wikipedia.org/wiki/Binary-coded_decimal#Other_computers_and_BCD
// http://homepage.divms.uiowa.edu/~jones/bcd/bcd.html
// https://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html
// https://www.baeldung.com/jni
public class JniZF10 {
    static {
        // Relative to program current working directory
        String libPath = System.getProperty("user.dir") + "/out/production/pipi/libjnizf10.dylib";
        System.load(libPath);

        initializeLibrary();
    }

    // TODO:  Representation that can be added  using alog
    // Substraction using 10  compelement
    // Number 123456 should be represented as long
    // [0|0] [1|2] [3|4] [5|6]
    // in little endian notation:
    // [5|6] [3|4] [1|2] [0|0] (order in byte[] array)
    //  TODO: Test using printf in native code
    // Byte array size always!!! multiply of 8

    // imported from native code:

    private static native void initializeLibrary();

    private static native int cmpAbs(byte[] aDigits, int aExponent,
                                     byte[] bDigits, int bExponent);

    private static native void addAbs(
            byte[] aDigits, int aExponent,
            byte[] bDigits, int bExponent,
            DigitsExponentStruct result);

    private static native void subtractAbs(
            byte[] aDigits, int aExponent,
            byte[] bDigits, int bExponent,
            DigitsExponentStruct result);

    private static class DigitsExponentStruct {
        public byte[] digits;
        public int exponent;
    }
}
