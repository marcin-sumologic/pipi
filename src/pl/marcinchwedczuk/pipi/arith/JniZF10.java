package pl.marcinchwedczuk.pipi.arith;

public class JniZF10 {
    static {
        // Relative to program current working directory
        String libPath = System.getProperty("user.dir") + "/out/production/pipi/libjnizf10.dylib";
        System.load(libPath);
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
