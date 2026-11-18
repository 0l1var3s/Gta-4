# Liberty Recompiled ES: edición en español para Galaxy S24 Ultra

**APK listo para instalar:** [`LibertyRecompiled-ES-S24Ultra-v0.5.9.3.apk`](LibertyRecompiled-ES-S24Ultra-v0.5.9.3.apk)

| | |
|---|---|
| Paquete | `com.libertyrecomp.es` (la original es `com.libertyrecomp`, así que **se instalan las dos a la vez**) |
| Nombre en el cajón de apps | **Liberty Recompiled ES** |
| Versión | 0.5.9.3-es-s24u.2 (versionCode 19) |
| Base | Liberty Recompiled v0.5.9.3 (build de apkvision) |
| Firma | v2 + v3, con clave propia |
| Requisitos | Android 10 o superior, arm64, Vulkan 1.1+ (el S24 Ultra cumple) |

## Validación del APK original

* El RAR multiparte contenía un único APK de 48 MB. Se extrajo sin errores y su firma v3 era válida (firmado por `APKVISION.ORG`, no por el autor original).
* **Código añadido por apkvision (eliminado en esta edición):**
  * `androidx.loader.app.services.*` (ofuscado): en cada inicio se conectaba a `https://apkvision.org/w/apiget.php?package=…&api_key=…`. Si el servidor decía que había una "versión nueva", mostraba un aviso para obligarte a descargarla desde su tienda.
  * `com.android.support.*` + `libAPKVISION.so` + `assets/AVConfig.json`: una plantilla de "mod menu" flotante con código nativo cerrado.
  * No se vio robo de datos (la librería nativa no tiene funciones de red), pero era código innecesario que se ejecutaba al abrir la app. Se borró entero.
* El resto coincide con el proyecto original [vaduur/LibertyRecompAndroid](https://github.com/vaduur/LibertyRecompAndroid): el lanzador, el instalador, el driver Turnip para Adreno y las librerías del juego.

## Cambios

### 1. Español
* **Lanzador traducido al completo** (108 textos): pantalla principal, instalación, driver Vulkan, informes de fallo y ajustes de rendimiento.
* **Juego en español**: se añadió `--user_language=5` (idioma de la consola = español). GTA IV carga entonces sus textos en español (menús, subtítulos y misiones).
* Botón "JUGAR" y banner "PARA ANDROID · ES" rehechos.

### 2. Interfaz
* Nueva paleta oscura con acento **ámbar** en lugar del gris/azul de GitHub.
* Botones principales (Jugar, Instalar, Guardar) en ámbar con texto oscuro para que destaquen. Los secundarios tienen más contraste.
* Esquinas más redondeadas (12 dp) y borde de foco ámbar, también al usar mando.

### 3. Menú «Gráficos del juego» (versión 2)
Debajo del botón JUGAR hay un botón nuevo, **Gráficos del juego**, y bajo él un resumen de la configuración actual (p. ej. `1080p · 30 FPS · SMAA · AF 4x · Audio estable`). Se maneja con el dedo o con el mando.

**Perfiles rápidos:** *S24 Ultra · Recomendado* (por defecto), *Calidad máxima*, *Fluidez 60 FPS*, *Batería* y *Todo Auto*.

**Ajustes individuales:** resolución de salida, reescalado FSR 1, límite de FPS (30/40/60/sin límite), VSync, antialiasing (FXAA/SMAA/MSAA), filtrado anisotrópico y de texturas, distancia de dibujado (fija y dinámica), calidad y distancia de sombras, reflejos, desenfoque de movimiento, tráfico y peatones, modo de audio e idioma del juego.

«Auto» en cualquier ajuste deja que decida el menú Pantalla del propio juego. Lo que no esté en Auto se aplica en cada partida y manda sobre lo que elijas en el menú del juego. Los cambios se guardan en `args.txt` y se aplican al pulsar Jugar.

### 4. Configuración del S24 Ultra (perfil recomendado, ya aplicado)
| Ajuste | Valor |
|---|---|
| Resolución de salida | 1080p (se escala a la pantalla de 1440p). Mucho menos calor que en nativa y casi igual de nítido. |
| FPS | 30 con VSync, que divide exacto los 120 Hz de la pantalla |
| Antialiasing | SMAA |
| Filtrado | anisotrópico 4x + trilineal |
| Distancia de dibujado | 1.0 con distancia dinámica (mínimo 0.80) |
| Tráfico/peatones | 0.5 |
| Protección térmica | activada; baja la distancia de dibujado a 72 °C y la restaura a 62 °C |
| Audio | modo «Estable» (ver abajo) |

Si tienes la versión 1 instalada, al abrir la nueva el perfil recomendado se aplica solo una vez sobre tu `args.txt`, sin tocar el idioma.

### 5. Corrección del audio distorsionado
El sonido de GTA IV (XMA) se decodifica en la CPU. El juego original usaba *performance hint*, que baja la frecuencia de la CPU para que vaya justa al ritmo de los fotogramas. Los hilos de audio no cuentan para ese cálculo. Cuando la CPU baja de frecuencia o el teléfono se calienta, el audio se queda sin datos y suena distorsionado. El modo **Estable**:
* `--android_perf_hint=false`: la CPU no se queda corta a mitad de partida (la protección térmica controla el calor).
* `--audio_maxqframes=64`: duplica el búfer de audio del juego (antes 32, el máximo es 64).
* `--audio_low_latency=false` y `SDL_ANDROID_LOW_LATENCY_AUDIO=0`: AAudio en modo normal, con un búfer de decenas de ms en vez de ~4 ms.

Si quieres el comportamiento original (menos consumo), elige Audio → «Ahorro de batería».

## Instalación
1. Descarga el APK en el teléfono e instálalo. Si Android lo pide, permite "instalar apps desconocidas". **No hace falta desinstalar la versión original.**
2. Abre **Liberty Recompiled ES**. Como es otra app, tiene su propia carpeta de datos (`Android/data/com.libertyrecomp.es/`), así que hay que volver a elegir la imagen `.iso` USA y el Title Update 8 y pulsar **Instalar**.
   * **Si ya tienes la versión 1 de Liberty Recompiled ES**, la versión 2 se instala encima (misma firma) y conserva el juego instalado.
   * Si quieres, también puedes copiar los archivos en `Android/data/com.libertyrecomp.es/files/install/`.
3. Pulsa **JUGAR**.

### Volver al inglés
Gráficos del juego → Idioma del juego → English → Guardar.

## Reconstruir
Los scripts de `tools/` aplican todos los cambios a partir del APK original:
```
tools/construir.sh <apk_original> <carpeta_herramientas> <carpeta_trabajo> <salida.apk>
```
`<carpeta_herramientas>` debe contener `apktool_2.10.0.jar`, `dx.jar` (dalvik-dx 16.0.1) y `android-4.1.1.4.jar`. Además hacen falta `zipalign`, `apksigner`, Java 17+ y Python 3 con Pillow y numpy. El menú está en `tools/java/` y se compila en `classes2.dex`. `tools/prueba/` comprueba la lógica de `args.txt` en la JVM.
