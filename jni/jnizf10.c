#include <stdio.h>
#include <string.h>

#include "./headers/pl_marcinchwedczuk_pipi_arith_JniZF10.h"
#include "./headers/pl_marcinchwedczuk_pipi_arith_JniZF10_DigitsExponentStruct.h"

char loDigit(signed char b)
{
  return (b & 0x0F);
}

char hiDigit(signed char b)
{
  return ((b & 0xF0) >> 4);
}

// WARN: This will not throw exception in C code, only set
// the exception flag for JVM!
jint throwRuntimeExcpeption(JNIEnv *env, char *message)
{
  jclass exClass = (*env)->FindClass(env, "java/lang/RuntimeException");
  if (exClass == NULL)
    return throwRuntimeExcpeption(env, message);

  return (*env)->ThrowNew(env, exClass, message);
}

// NOTE: Carry can be probagated using addOne array of bcd numbers
// addOne(byte b) -> return b+1 BCD number!!!
// carryOne(b) -> returns if carry is neccessary!!!
// 2x256  bytes mem space!!!

JNIEXPORT jint JNICALL Java_pl_marcinchwedczuk_pipi_arith_JniZF10_cmpAbs(JNIEnv *env, jclass thisObj,
                                                                         jbyteArray aDigits, jint aExponent,
                                                                         jbyteArray bDigits, jint bExponent)
{

  jbyte *aa = (*env)->GetByteArrayElements(env, aDigits, NULL);
  jbyte *bb = (*env)->GetByteArrayElements(env, bDigits, NULL);
  // Arrays should have the same size
  jsize arrSizeBytes = (*env)->GetArrayLength(env, aDigits);

  // Fast track: compare exponents
  if (aExponent != bExponent)
  {
    // number is either in form 0.dddd x 10^exp
    // where first d != 0, or in form
    // 0.00000 x 10^0 (for zero).

    int aNonZero = hiDigit(*aa) != 0;
    int bNonZero = hiDigit(*bb) != 0;

    if (aNonZero && bNonZero)
    {
      return (aExponent - bExponent);
    }
    else if (aNonZero)
    {
      return 1;
    }
    else if (bNonZero)
    {
      return (-1);
    }
    else
    {
      throwRuntimeExcpeption(env, "cannot happen");
      return 0;
    }
  }

  int cmp = strncmp((const char *)aa, (const char *)bb, arrSizeBytes);

  (*env)->ReleaseByteArrayElements(env, aDigits, aa, 0);
  (*env)->ReleaseByteArrayElements(env, bDigits, bb, 0);

  return cmp;
}

JNIEXPORT void JNICALL Java_pl_marcinchwedczuk_pipi_arith_JniZF10_addAbs(JNIEnv *env, jclass thisObj,
                                                                         jbyteArray aDigits, jint aExp,
                                                                         jbyteArray bDigits, jint bExp,
                                                                         jobject result)
{
  return;
}

JNIEXPORT void JNICALL Java_pl_marcinchwedczuk_pipi_arith_JniZF10_subtractAbs(JNIEnv *env, jclass thisObj,
                                                                              jbyteArray aDigits, jint aExp,
                                                                              jbyteArray bDigits, jint bExp,
                                                                              jobject result)
{
  return;
}
