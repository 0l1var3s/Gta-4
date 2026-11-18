# Liberty Recompiled ES: edición en español para Galaxy S24 Ultra

**APK listo para instalar:** [`LibertyRecompiled-ES-S24Ultra-v0.5.9.3.apk`](LibertyRecompiled-ES-S24Ultra-v0.5.9.3.apk)

| | |
|---|---|
| Paquete | `com.libertyrecomp.es` (la original es `com.libertyrecomp`, así que **se instalan las dos a la vez**) |
| Nombre en el cajón de apps | **Liberty Recompiled ES** |
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

### 3. Estabilidad y gráficos para el S24 Ultra (Snapdragon 8 Gen 3 / Adreno 750)
Estos ajustes están en `assets/default_args.txt`. Al primer inicio se copian a `Android/data/com.libertyrecomp.es/files/args.txt`, donde se pueden editar.

| Ajuste | Valor | Motivo |
|---|---|---|
| Protección térmica | **activada por defecto** | El S24 Ultra se calienta en sesiones largas. Esto baja la distancia de dibujado antes de que el teléfono reduzca su velocidad, en vez de dar tirones. |
| `android_thermal_guard_hot_c` / `cool_c` | 72 °C / 62 °C | Solo se usan si Android no informa del margen térmico. |
| `android_perf_hint` | true | La CPU sube su velocidad solo cuando el fotograma lo necesita: menos calor y batería. |
| `gta4_frame_wait_sleep` | true | La CPU descansa mientras espera el siguiente fotograma, en vez de seguir trabajando. |
| `gta4_anisotropic_filtering` | 4x | Texturas del suelo más nítidas, con un coste casi nulo en la Adreno 750. |
| Densidad de tráfico y peatones | 0.5 (antes 0.4) | La ciudad está más viva y el S24 Ultra va sobrado. |
| Distancia de dibujado dinámica | se mantiene (mín. 0.80) | Evita caídas de FPS. |

La resolución, el reescalado, los FPS y el antialiasing se siguen eligiendo en el menú **Pantalla** dentro del juego, como en la versión original.

## Instalación
1. Descarga el APK en el teléfono e instálalo. Si Android lo pide, permite "instalar apps desconocidas". **No hace falta desinstalar la versión original.**
2. Abre **Liberty Recompiled ES**. Como es otra app, tiene su propia carpeta de datos (`Android/data/com.libertyrecomp.es/`), así que hay que volver a elegir la imagen `.iso` USA y el Title Update 8 y pulsar **Instalar**.
   * Si quieres, también puedes copiar los archivos en `Android/data/com.libertyrecomp.es/files/install/`.
3. Pulsa **JUGAR**.

### Volver al inglés
Edita `Android/data/com.libertyrecomp.es/files/args.txt` y cambia `--user_language=5` por `--user_language=1`, o borra esa línea.

## Reconstruir
Los scripts de `tools/` aplican todos los cambios a partir del APK original:
```
tools/construir.sh <apk_original> <apktool.jar> <carpeta_trabajo> <salida.apk>
```
Necesitan `apktool` 2.10, `zipalign`, `apksigner`, Java y Python 3 con Pillow y numpy.
