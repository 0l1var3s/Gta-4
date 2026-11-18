# Liberty Recompiled ES: edición en español para Galaxy S24 Ultra

**APK listo para instalar:** [`LibertyRecompiled-ES-S24Ultra-v0.5.9.3.apk`](LibertyRecompiled-ES-S24Ultra-v0.5.9.3.apk)

| | |
|---|---|
| Paquete | `com.libertyrecomp.es` (la original es `com.libertyrecomp`, así que **se instalan las dos a la vez**) |
| Nombre en el cajón de apps | **Liberty Recompiled ES** |
| Versión | 0.5.9.3-es-s24u.3 (versionCode 20) |
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

### 5. Audio: distorsión y radio que se traba (versiones 2 y 3)
El sonido de GTA IV (XMA) se decodifica en la CPU, y las emisoras de radio se leen del almacenamiento mientras se conduce. Ahí es justo cuando el juego también carga más ciudad. El modo de audio **«Estable + prioridad»** (por defecto desde la v3) combina:
* `--android_perf_hint=false`: la CPU no baja de frecuencia a mitad de partida.
* `--audio_maxqframes=64`: búfer de audio del juego al máximo (el original era 32).
* `--audio_low_latency=false`, `SDL_ANDROID_LOW_LATENCY_AUDIO=0` y `SDL_AUDIO_DEVICE_SAMPLE_FRAMES=1024`: AAudio en modo normal con un búfer de ~21 ms.
* `--ignore_thread_priorities=false` (v3): el motor respeta las prioridades que el juego da a sus hilos de sonido y radio. El original las ignoraba.
* `--host_file_io_workers=8` (v3): 8 canales de lectura en lugar de 4, para que la música de la radio no espere detrás de la carga de la ciudad.

Si notas algo raro con la prioridad, en Gráficos del juego → Audio elige «Estable» (como la v2) o «Ahorro de batería» (como el original).

### 6. Menú «Controles y mando» (versión 3)
Es un botón nuevo en el lanzador, debajo de «Gráficos del juego».
* **Mandos Bluetooth/USB:** muestra el mando conectado y se actualiza solo al conectarlo. Incluye un botón para abrir los ajustes Bluetooth y emparejarlo, y una prueba en vivo que muestra el último botón pulsado.
* **Controles en pantalla:** «Ocultar con mando» (desaparecen al conectar un mando), «Siempre visibles» o «Quitar siempre».
* **Estilo de los controles táctiles:** «Mando Xbox» (fijo, el original) o «Contextuales» (solo los botones útiles en cada momento).
* **Giroscopio del mando** (DualSense, DualShock 4, Switch Pro): activado o desactivado.
* **Reasignar botones del mando:** A, B, X, Y, LB, RB, L3, R3, Back y Start. Tocas la acción, pulsas el botón del mando y se intercambian, así nunca quedan dos iguales. Se guarda en `files/controles.txt` y se aplica dentro del juego antes de que SDL reciba la pulsación. Los gatillos, sticks y cruceta van por ejes y no se reasignan.
* **Mover/redimensionar los botones táctiles:** con el editor que ya trae el juego, en el menú de pausa → «Touch Controls» → «Edit Touch Layout».

## Instalación
1. Descarga el APK en el teléfono e instálalo. Si Android lo pide, permite "instalar apps desconocidas". **No hace falta desinstalar la versión original.**
2. Abre **Liberty Recompiled ES**. Como es otra app, tiene su propia carpeta de datos (`Android/data/com.libertyrecomp.es/`), así que hay que volver a elegir la imagen `.iso` USA y el Title Update 8 y pulsar **Instalar**.
   * **Si ya tienes una versión anterior de Liberty Recompiled ES**, la nueva se instala encima (misma firma) y conserva el juego instalado. Al abrir el lanzador, tu `args.txt` se actualiza solo una vez: la v1 recibe el perfil recomendado, y la v2 solo el nuevo modo de audio.
   * Si quieres, también puedes copiar los archivos en `Android/data/com.libertyrecomp.es/files/install/`.
3. Pulsa **JUGAR**.

### Volver al inglés
Gráficos del juego → Idioma del juego → English → Guardar.

## Reconstruir
Los scripts de `tools/` aplican todos los cambios a partir del APK original:
```
tools/construir.sh <apk_original> <carpeta_herramientas> <carpeta_trabajo> <salida.apk>
```
`<carpeta_herramientas>` debe contener `apktool_2.10.0.jar`, `dx.jar` (dalvik-dx 16.0.1) y `android-4.1.1.4.jar`. Además hacen falta `zipalign`, `apksigner`, Java 17+ y Python 3 con Pillow y numpy. Los menús y la reasignación están en `tools/java/` y se compilan en `classes2.dex`. `tools/prueba/` comprueba en la JVM la lógica de `args.txt`, las migraciones desde v1/v2 y la reasignación de botones.
