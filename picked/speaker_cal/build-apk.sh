#!/bin/bash
# Builds a debug APK of Speaker Calibrator without the ROM tree (Android SDK build-tools + a JDK).
# usage: build-apk.sh <out.apk>
set -e
HERE=$(cd "$(dirname "$0")" && pwd)
OUT=${1:-$HERE/out/SpeakerCal.apk}
SDK=${ANDROID_SDK:-$HOME/Android/Sdk}
BT=$SDK/build-tools/36.0.0
JDK=${JDK:-/home/a441/rom/.picked-tools/jdk}
JAR=${ANDROID_JAR:-/home/a441/rom/.picked-tools/sdk36/android-36/android.jar}
export PATH=$JDK/bin:$PATH
W=$(mktemp -d)
$BT/aapt2 compile --dir "$HERE/res" -o $W/res.zip
$BT/aapt2 link -o $W/base.apk -I $JAR --manifest "$HERE/AndroidManifest.xml" --java $W/gen $W/res.zip
javac --release 11 -nowarn -cp $JAR -d $W/cls $(find "$HERE/src" $W/gen -name '*.java')
$BT/d8 --min-api 30 --lib $JAR --output $W $(find $W/cls -name '*.class')
cp $W/base.apk $W/unsigned.apk
(cd $W && zip -q unsigned.apk classes.dex)
mkdir -p "$(dirname "$OUT")"
$BT/zipalign -f -p 4 $W/unsigned.apk $W/aligned.apk
$BT/apksigner sign --ks $HOME/.android/debug.keystore --ks-pass pass:android --key-pass pass:android \
    --out "$OUT" $W/aligned.apk
rm -rf $W
echo "built $OUT"
