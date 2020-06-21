#!/usr/bin/env bash
export JAVA_HOME=$(/usr/libexec/java_home)

javah -classpath "./out/production/pipi" \
 -d "./jni/headers" \
 -jni pl.marcinchwedczuk.pipi.arith.JniZF10

mkdir -p "./out/production/pipi/"

# Library name must start with lib, so library X becomes libX
gcc -I"$JAVA_HOME/include" \
  -I"$JAVA_HOME/include/darwin" \
  -I"./jni/headers" \
  -dynamiclib -o "./out/production/pipi/libjnizf10.dylib" \
  ./jni/*.c
