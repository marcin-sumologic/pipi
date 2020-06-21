#include "./headers/pl_marcinchwedczuk_pipi_arith_JniZF10.h"
#include "./headers/pl_marcinchwedczuk_pipi_arith_JniZF10_DigitsExponentStruct.h"

JNIEXPORT jint JNICALL Java_pl_marcinchwedczuk_pipi_arith_JniZF10_cmpAbs
  (JNIEnv* env, jclass thisObj,
  jbyteArray aDigits, jint aExp,
  jbyteArray bDigits, jint bExp)
{
    return 42;
}

JNIEXPORT void JNICALL Java_pl_marcinchwedczuk_pipi_arith_JniZF10_addAbs
  (JNIEnv* env, jclass thisObj,
  jbyteArray aDigits, jint aExp,
  jbyteArray bDigits, jint bExp,
  jobject result)
{
    return;
}

JNIEXPORT void JNICALL Java_pl_marcinchwedczuk_pipi_arith_JniZF10_subtractAbs
  (JNIEnv* env, jclass thisObj,
  jbyteArray aDigits, jint aExp,
  jbyteArray bDigits, jint bExp,
  jobject result)
{
    return;
}

