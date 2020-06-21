package pl.marcinchwedczuk.pipi.arith;

public class JniZF10 {
    static {
        // Remember to add to run config
        // -Djava.library.path="./out/production/pipi/"
        System.loadLibrary("jnizf10");
    }

    public static native int cmpAbs(byte[] aDigits, int aExponent,
                                     byte[] bDigits, int bExponent);

    public static native void addAbs(
            byte[] aDigits, int aExponent,
            byte[] bDigits, int bExponent,
            DigitsExponentStruct result);

    public static native void subtractAbs(
            byte[] aDigits, int aExponent,
            byte[] bDigits, int bExponent,
            DigitsExponentStruct result);

    public static class DigitsExponentStruct {
        public byte[] digits;
        public int exponent;
    }
}
