#!/usr/bin/env bash
# Construye el APK en español / S24 Ultra a partir del APK original.
# Uso: tools/construir.sh <apk_original> <carpeta_herramientas> <carpeta_trabajo> <salida.apk>
#   <carpeta_herramientas> debe contener:
#     apktool_2.10.0.jar, dx.jar (com.jakewharton.android.repackaged:dalvik-dx:16.0.1)
#     y android-4.1.1.4.jar (com.google.android:android:4.1.1.4, para compilar el menú).
#   Si existe <carpeta_trabajo>/firma.keystore se reutiliza (mismas actualizaciones).
set -euo pipefail
ORIG=$1; TOOLS=$2; WORK=$3; OUT=$4
HERE=$(cd "$(dirname "$0")" && pwd)
APKTOOL="$TOOLS/apktool_2.10.0.jar"

rm -rf "$WORK/dec" "$WORK/sin_firmar.apk" "$WORK/alineado.apk" "$WORK/java"
mkdir -p "$WORK/java"
java -jar "$APKTOOL" d -f -o "$WORK/dec" "$ORIG"

python3 -I "$HERE/parchear.py" "$WORK/dec"
python3 -I "$HERE/recursos.py" "$WORK/dec"
cp "$HERE/default_args.txt" "$WORK/dec/assets/default_args.txt"

# Menús "Gráficos del juego" / "Controles y mando" y GamepadRemap -> classes2.dex
javac --release 8 -nowarn -encoding UTF-8 -cp "$TOOLS/android-4.1.1.4.jar" \
  -d "$WORK/java/classes" "$HERE"/java/com/libertyrecomp/*.java
java -cp "$TOOLS/dx.jar" com.android.dx.command.Main --dex --min-sdk-version=26 \
  --output="$WORK/java/classes2.dex" "$WORK/java/classes"

cp "$WORK/java/classes2.dex" "$WORK/dec/classes2.dex"   # apktool lo copia tal cual
java -jar "$APKTOOL" b -o "$WORK/sin_firmar.apk" "$WORK/dec"
[[ "$(unzip -Z1 "$WORK/sin_firmar.apk")" == *classes2.dex* ]] || { echo "falta classes2.dex"; exit 1; }
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
apksigner verify --min-sdk-version 24 -v "$OUT" | grep -E "Verifies|v2 scheme|v3 scheme"
