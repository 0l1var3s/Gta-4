#!/usr/bin/env python3
"""Aplica los cambios de la edición en español / S24 Ultra a un APK de
Liberty Recompiled ya decodificado con apktool.

Uso:  python3 -I parchear.py <carpeta_decodificada>
"""
import os
import re
import shutil
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from traducciones_es import TRADUCCIONES  # noqa: E402

D = sys.argv[1]
NUEVO_PAQUETE = "com.libertyrecomp.es"
NOMBRE_APP = "Liberty Recompiled ES"


def p(*a):
    return os.path.join(D, *a)


def leer(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def escribir(path, txt):
    with open(path, "w", encoding="utf-8") as f:
        f.write(txt)


def smali_escape(s):
    # Las claves/valores ya traen los escapes smali (\n, \', \"); solo hay
    # que convertir los caracteres no ASCII a \uXXXX.
    return "".join(c if ord(c) < 128 else "\\u%04x" % ord(c) for c in s)


def sustituir(path, viejo, nuevo, minimo=1):
    txt = leer(path)
    n = txt.count(viejo)
    if n < minimo:
        sys.exit(f"ERROR: no se encontró {viejo!r} en {path}")
    escribir(path, txt.replace(viejo, nuevo))
    return n


# ---------------------------------------------------------------------------
# 1. Quitar el código inyectado por APKVISION (menú flotante + comprobación
#    remota de "actualización" que contacta apkvision.org en cada inicio).
# ---------------------------------------------------------------------------
la = p("smali", "com", "libertyrecomp", "LauncherActivity.smali")
# En su lugar se engancha el menú "Gráficos del juego" (classes2.dex).
sustituir(la, "    invoke-static {p0}, Lcom/android/support/Main;->Start(Landroid/content/Context;)V\n",
          "    invoke-static {p0}, Lcom/libertyrecomp/GraphicsMenu;->attach(Landroid/app/Activity;)V\n")
sustituir(la, "    invoke-static/range {p0 .. p0}, Landroidx/loader/app/services/l;->j(Ljava/lang/Object;)V\n", "")
for carpeta in (p("smali", "com", "android", "support"), p("smali", "androidx", "loader")):
    shutil.rmtree(carpeta, ignore_errors=True)
for f in (p("lib", "arm64-v8a", "libAPKVISION.so"), p("assets", "AVConfig.json")):
    if os.path.exists(f):
        os.remove(f)
# Comprobar que no queda ninguna referencia.
for raiz, _, archivos in os.walk(p("smali")):
    for a in archivos:
        t = leer(os.path.join(raiz, a))
        if "Lcom/android/support/" in t or "Landroidx/loader/" in t or "apkvision" in t.lower():
            sys.exit(f"ERROR: queda una referencia a APKVISION en {a}")

# La "migración de argumentos" del original borra de args.txt líneas como
# --gta4_frame_limit=30 o --gta4_output_height_cap=720 en cada inicio, lo que
# anularía el menú de gráficos. En una instalación nueva no hace falta.
sustituir(p("smali", "com", "libertyrecomp", "LibertyActivity.smali"),
          "    invoke-static {v3}, Lcom/libertyrecomp/LibertyActivity;->migrateArguments(Ljava/io/File;)V\n", "")

# Audio: búfer del dispositivo más grande (AAudio en modo normal).
sustituir(p("AndroidManifest.xml"),
          '<meta-data android:name="SDL_ENV.SDL_JOYSTICK_HIDAPI" android:value="0"/>',
          '<meta-data android:name="SDL_ENV.SDL_JOYSTICK_HIDAPI" android:value="0"/>'
          '<meta-data android:name="SDL_ENV.SDL_ANDROID_LOW_LATENCY_AUDIO" android:value="0"/>')

# Reasignación de botones del mando (GamepadRemap en classes2.dex): se aplica
# a cada pulsación antes de que SDL la vea.
sustituir(p("smali", "org", "libsdl", "app", "SDLActivity.smali"),
          ".method public dispatchKeyEvent(Landroid/view/KeyEvent;)Z\n    .locals 3\n",
          ".method public dispatchKeyEvent(Landroid/view/KeyEvent;)Z\n    .locals 3\n\n"
          "    invoke-static {p0, p1}, Lcom/libertyrecomp/GamepadRemap;->remap(Landroid/content/Context;Landroid/view/KeyEvent;)Landroid/view/KeyEvent;\n\n"
          "    move-result-object p1\n")

# Audio: búfer del dispositivo de 1024 muestras (~21 ms) para absorber tirones.
sustituir(p("AndroidManifest.xml"),
          '<meta-data android:name="SDL_ENV.SDL_ANDROID_LOW_LATENCY_AUDIO" android:value="0"/>',
          '<meta-data android:name="SDL_ENV.SDL_ANDROID_LOW_LATENCY_AUDIO" android:value="0"/>'
          '<meta-data android:name="SDL_ENV.SDL_AUDIO_DEVICE_SAMPLE_FRAMES" android:value="1024"/>')

# ---------------------------------------------------------------------------
# 2. Paquete y nombre nuevos: se instala junto a la versión original.
# ---------------------------------------------------------------------------
yml = p("apktool.yml")
sustituir(p("AndroidManifest.xml"), 'package="com.libertyrecomp"', f'package="{NUEVO_PAQUETE}"')
sustituir(yml, "versionName: 0.5.9.3", "versionName: 0.5.9.3-es-s24u.3")
sustituir(yml, "versionCode: 18", "versionCode: 20")
sustituir(p("res", "values", "strings.xml"), ">Liberty Recompiled<", f">{NOMBRE_APP}<")

# ---------------------------------------------------------------------------
# 3. Traducción del lanzador al español.
# ---------------------------------------------------------------------------
smali_dir = p("smali", "com", "libertyrecomp")
total = 0
usadas = set()
for a in sorted(os.listdir(smali_dir)):
    ruta = os.path.join(smali_dir, a)
    txt = leer(ruta)
    original = txt
    for en, es in TRADUCCIONES.items():
        patron = re.compile(r'(const-string(?:/jumbo)? [vp]\d+, )"' + re.escape(en) + '"')
        txt, n = patron.subn(lambda m: m.group(1) + '"' + smali_escape(es) + '"', txt)
        if n:
            usadas.add(en)
            total += n
    if txt != original:
        escribir(ruta, txt)
faltan = [k for k in TRADUCCIONES if k not in usadas]
print(f"Traducciones aplicadas: {total} ({len(usadas)}/{len(TRADUCCIONES)} textos)")
for k in faltan:
    print("  (no encontrado, se ignora):", k[:70])

# ---------------------------------------------------------------------------
# 4. Interfaz: nueva paleta (oscuro + ámbar), esquinas más redondeadas y
#    botón Jugar destacado.
# ---------------------------------------------------------------------------
escribir(p("res", "values", "colors.xml"), """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="picker_accent">#fff5b335</color>
    <color name="picker_bg">#ff0b0d12</color>
    <color name="picker_btn_disabled">#ff252a34</color>
    <color name="picker_card">#ff161a22</color>
    <color name="picker_error">#ffff7a6b</color>
    <color name="picker_muted">#ff9aa3b2</color>
    <color name="picker_text">#fff3f4f6</color>
</resources>
""")
sustituir(p("res", "values", "styles.xml"), "#ff3fa9f5", "#fff5b335")

ACENTO = "-0xa4ccb"      # 0xFFF5B335 (ámbar) como int con signo
txt = leer(la)
# Borde de foco azul -> ámbar.
txt = txt.replace("const v5, -0xc0560b", f"const v5, {ACENTO}")
# Botones principales (Jugar, Instalar, Guardar): gris claro -> ámbar.
n_btn = txt.count("-0x19191a")
txt = txt.replace("-0x19191a", ACENTO)
# Botones secundarios: gris azulado algo más claro para mejor contraste.
txt = txt.replace("const p1, -0xcfc9c3", "const p1, -0xd8d2c7")  # 0xFF272D39
# Radio de las esquinas 8dp -> 12dp en focusBackground().
m = re.search(r"(\.method private focusBackground\(I\).*?\.end method)", txt, re.S)
cuerpo = m.group(1).replace("const/16 v0, 0x8", "const/16 v0, 0xc")
txt = txt.replace(m.group(1), cuerpo)
escribir(la, txt)
print(f"Botones recoloreados: {n_btn}")

# ---------------------------------------------------------------------------
# 5. Estabilidad en S24 Ultra: "Protección térmica" activada por defecto.
# ---------------------------------------------------------------------------
# El valor por defecto (false) vive en un registro compartido que vale 0; se
# pone a 1 solo durante la llamada getBoolean("thermal_guard", ...) y se
# devuelve a 0 justo después, así el resto del método no cambia.
txt = leer(la)
patron = re.compile(
    r'(const-string ([vp]\d+), "thermal_guard"\n(?:\s*\n|\s*\.line \d+\n)*)'
    r'(\s*invoke-interface \{[vp]\d+, \2, ([vp]\d+)\}, Landroid/content/SharedPreferences;->getBoolean\(Ljava/lang/String;Z\)Z\n'
    r'\s*move-result [vp]\d+\n)'
)
txt, n = patron.subn(
    lambda m: m.group(1) + f"    const/4 {m.group(4)}, 0x1\n" + m.group(3) + f"    const/4 {m.group(4)}, 0x0\n",
    txt,
)
if n != 2:
    sys.exit(f"ERROR: se esperaban 2 lecturas de thermal_guard, hay {n}")
escribir(la, txt)
print(f"Protección térmica activada por defecto ({n} lecturas)")

print("Parche aplicado correctamente.")
