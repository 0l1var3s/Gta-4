#!/usr/bin/env bash
# Construye el APK en español / S24 Ultra a partir del APK original.
# Uso: tools/construir.sh <apk_original> <apktool.jar> <carpeta_trabajo> <salida.apk>
set -euo pipefail
ORIG=$1; APKTOOL=$2; WORK=$3; OUT=$4
HERE=$(cd "$(dirname "$0")" && pwd)

rm -rf "$WORK/dec" "$WORK/sin_firmar.apk" "$WORK/alineado.apk"
mkdir -p "$WORK"
java -jar "$APKTOOL" d -f -o "$WORK/dec" "$ORIG"

python3 -I "$HERE/parchear.py" "$WORK/dec"
python3 -I "$HERE/recursos.py" "$WORK/dec"
cp "$HERE/default_args.txt" "$WORK/dec/assets/default_args.txt"

java -jar "$APKTOOL" b -o "$WORK/sin_firmar.apk" "$WORK/dec"
zipalign -f -p 4 "$WORK/sin_firmar.apk" "$WORK/alineado.apk"

KS="$WORK/firma.keystore"
if [ ! -f "$KS" ]; then
  keytool -genkeypair -keystore "$KS" -storepass libertyes -keypass libertyes \
    -alias libertyes -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Liberty Recompiled ES" >/dev/null
fi
apksigner sign --ks "$KS" --ks-pass pass:libertyes --key-pass pass:libertyes \
  --ks-key-alias libertyes --v1-signing-enabled true --v2-signing-enabled true \
  --v3-signing-enabled true --min-sdk-version 24 --out "$OUT" "$WORK/alineado.apk"
apksigner verify -v "$OUT"
