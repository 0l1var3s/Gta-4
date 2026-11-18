# Traducciones al español del lanzador de Liberty Recompiled.
# Clave: cadena original en inglés tal como aparece en el smali (con escapes smali).
# Valor: texto en español (se convierte a escapes \uXXXX al aplicar el parche).

TRADUCCIONES = {
    # --- Pantalla principal ---
    "Play": "Jugar",
    "Play (Debug Log)": "Jugar (con registro de depuración)",
    "Performance test settings": "Ajustes de rendimiento (pruebas)",
    "A: select   B: back   Start: play": "A: elegir   B: volver   Start: jugar",
    "The debug log is saved to Android/data/": "El registro de depuración se guarda en Android/data/",
    "/files/debug_logs/. Attach the newest one when you open an issue on GitHub.":
        "/files/debug_logs/. Adjunta el más reciente si abres un reporte en GitHub.",
    "Liberty Recompiled for Android ": "Liberty Recompiled para Android (Español) ",
    "Starting...": "Iniciando...",
    "Last launch failed: ": "El último inicio falló: ",

    # --- Panel del juego / instalación ---
    "Game": "Juego",
    "Checking the installation...": "Comprobando la instalación...",
    "Checking the sources...": "Comprobando los archivos...",
    "Checking the disc image...": "Comprobando la imagen de disco...",
    "Base game (disc image .iso)": "Juego base (imagen de disco .iso)",
    "Title Update 8": "Title Update 8",
    "The Lost and Damned (optional)": "The Lost and Damned (opcional)",
    "The Ballad of Gay Tony (optional)": "The Ballad of Gay Tony (opcional)",
    "Not selected": "Sin seleccionar",
    "Choose file...": "Elegir archivo...",
    "Clear": "Quitar",
    "Install": "Instalar",
    "Install the game": "Instalar el juego",
    "Install episodes": "Instalar episodios",
    "Reinstall or add episodes": "Reinstalar o añadir episodios",
    "Installing...": "Instalando...",
    "Installing the game...": "Instalando el juego...",
    "Installing the game... %d%%": "Instalando el juego... %d%%",
    "Cancelling...": "Cancelando...",
    "Cancel": "Cancelar",
    "Keep the app open. This takes several minutes.":
        "Mantén la app abierta. Esto tarda varios minutos.",
    "Game installed": "Juego instalado",
    "Game installed. Press Play.": "Juego instalado. Pulsa Jugar.",
    "Installation complete. Press Play.": "Instalación completada. Pulsa Jugar.",
    "Installation failed": "La instalación falló",
    "The game is being installed.": "El juego se está instalando.",
    "An installation is already running.": "Ya hay una instalación en curso.",
    "Install the game first (Game panel on the right).":
        "Primero instala el juego (panel Juego a la derecha).",
    "Game not installed. Choose the disc image and Title Update 8 on the right and press Install.":
        "Juego no instalado. Elige la imagen de disco y el Title Update 8 a la derecha y pulsa Instalar.",
    "Game files found in install/. Press Install on the right.":
        "Se encontraron archivos del juego en install/. Pulsa Instalar a la derecha.",
    "GTA IV with Title Update 8 is installed and ready.":
        "GTA IV con el Title Update 8 está instalado y listo.",
    "You need your own USA disc image and Title Update 8 (0.0.8.5).":
        "Necesitas tu propia imagen de disco USA y el Title Update 8 (0.0.8.5).",
    "Files can be picked anywhere on the device storage or an SD card (the app asks for file access once). Alternatively copy them into Android/data/":
        "Puedes elegir los archivos desde cualquier parte del almacenamiento o de una tarjeta SD (la app pide acceso a archivos una vez). También puedes copiarlos en Android/data/",
    "/files/install/ - they are picked up automatically.":
        "/files/install/ y se detectan automáticamente.",
    "/files/install/ instead.": "/files/install/.",
    "). The game installs itself on Play from the install/ folder.":
        "). El juego se instalará al pulsar Jugar desde la carpeta install/.",
    "\\nCurrent installation: ": "\\nInstalación actual: ",
    "The installer is unavailable (": "El instalador no está disponible (",
    "The installer stopped: ": "El instalador se detuvo: ",
    "The source could not be checked: ": "No se pudo comprobar el archivo: ",
    "Cannot read ": "No se puede leer ",
    ". Check that file access is allowed.": ". Comprueba que el acceso a archivos esté permitido.",
    "This file has no path on the device (cloud storage?). Pick it from the device storage or an SD card.":
        "Este archivo no tiene ruta en el dispositivo (¿está en la nube?). Elígelo desde el almacenamiento interno o una tarjeta SD.",
    "No file picker is available on this device.":
        "No hay ningún selector de archivos disponible en este dispositivo.",
    "Allow file access": "Permitir acceso a archivos",
    "Open setting": "Abrir ajuste",
    "To read the game files from anywhere on this device, Liberty Recompiled needs access to all files. Android opens the setting next; enable it and come back.\\n\\nWithout it, copy the files into Android/data/":
        "Para leer los archivos del juego desde cualquier parte del dispositivo, Liberty Recompiled necesita acceso a todos los archivos. A continuación Android abrirá el ajuste; actívalo y vuelve.\\n\\nSin ese permiso, copia los archivos en Android/data/",

    # --- Driver Vulkan ---
    "Vulkan driver": "Driver Vulkan",
    "Applied the next time you press Play.": "Se aplica la próxima vez que pulses Jugar.",
    "Turnip 26.3.0-R6 (bundled)": "Turnip 26.3.0-R6 (incluido)",
    "Recommended for Adreno 6xx/7xx": "Recomendado para Adreno 6xx/7xx (S24 Ultra: Adreno 750)",
    "System driver": "Driver del sistema",
    "The device\\'s own Vulkan driver (needs Vulkan 1.2)":
        "El driver Vulkan propio del teléfono (requiere Vulkan 1.2)",
    "Imported driver": "Driver importado",
    "Import driver (.zip)": "Importar driver (.zip)",
    "Importing driver...": "Importando driver...",
    "Import failed: ": "Error al importar: ",
    "Driver imported and selected.": "Driver importado y seleccionado.",
    "Driver removed. Using the bundled Turnip.": "Driver eliminado. Se usará el Turnip incluido.",
    "Could not save the driver choice: ": "No se pudo guardar el driver elegido: ",
    "Remove": "Eliminar",
    "Turnip packages for AdrenoTools (a zip with a .so and meta.json) work here. The game needs Vulkan 1.2; if a driver cannot start, the game falls back to the device\\'s own driver.":
        "Aquí funcionan los paquetes Turnip para AdrenoTools (un zip con un .so y un meta.json). El juego necesita Vulkan 1.2; si un driver no arranca, el juego vuelve al driver propio del teléfono.",
    "No usable Vulkan driver was found on this device.\\n\\n":
        "No se encontró ningún driver Vulkan utilizable en este dispositivo.\\n\\n",
    "The Vulkan driver could not be initialized (unreadable status).":
        "No se pudo inicializar el driver Vulkan (estado ilegible).",
    "This device\\'s GPU (": "La GPU de este dispositivo (",
    ") only offers Vulkan ": ") solo ofrece Vulkan ",
    " through its own driver, and the game needs Vulkan 1.2.":
        " con su propio driver, y el juego necesita Vulkan 1.2.",
    "\\n\\nIf driver.txt says \\\"system\\\", delete it to use the bundled Turnip driver.":
        "\\n\\nSi driver.txt dice \\\"system\\\", bórralo para usar el driver Turnip incluido.",
    "\\n\\nSupported GPUs: Qualcomm Adreno 6xx and 7xx, ARM Mali (experimental).\\nA report was saved to Android/data/":
        "\\n\\nGPUs compatibles: Qualcomm Adreno 6xx y 7xx, ARM Mali (experimental).\\nSe guardó un informe en Android/data/",
    "\\n\\nThe selected custom driver (the bundled Turnip unless driver.txt names another) could not be used either: ":
        "\\n\\nTampoco se pudo usar el driver personalizado elegido (el Turnip incluido salvo que driver.txt indique otro): ",

    # --- Informes de fallos ---
    "The game crashed - a crash report was saved": "El juego se cerró inesperadamente: se guardó un informe",
    "Share crash report": "Compartir informe de fallo",
    "Share report": "Compartir informe",
    "Dismiss": "Descartar",
    "No app to share with. The report is in ": "No hay ninguna app para compartir. El informe está en ",
    "\\nPlease attach it to an issue on GitHub (vaduur/LibertyRecompAndroid), or press Share report to send it from here.":
        "\\nAdjúntalo en un reporte en GitHub (vaduur/LibertyRecompAndroid) o pulsa Compartir informe para enviarlo desde aquí.",
    "\\n\\nSaved to Android/data/": "\\n\\nGuardado en Android/data/",
    "Liberty Recompiled crash report: ": "Informe de fallo de Liberty Recompiled: ",

    # --- Ajustes de rendimiento ---
    "WARNING!!! For lower-end devices and endorsed ONLY for testing. SHIT WILL BREAK, and by shit I mean almost all visuals. If you have stable 30fps please don\\'t touch it, or your game will look like a potato, and a very ugly one a that with tons of other visual bugs and crashes. Issues made with any of these settings probably will be skipped on Github. You have been warned, have fun then:":
        "¡ATENCIÓN! Solo para equipos modestos y para pruebas. Varias opciones ROMPEN casi todo el apartado visual. Si ya tienes 30 FPS estables no las toques: el juego se verá mal y puede tener errores visuales y cierres. En un S24 Ultra se recomienda dejar todo apagado salvo «Protección térmica».",
    "Draw distance 0.5": "Distancia de dibujado 0.5",
    "Fixed half draw distance; the dynamic draw distance is turned off.":
        "Distancia de dibujado fija a la mitad; se desactiva la distancia dinámica.",
    "Sun shadows off": "Sin sombras del sol",
    "Nothing casts a sun shadow; the shadow map is not drawn.":
        "Nada proyecta sombra del sol; no se dibuja el mapa de sombras.",
    "GPU turbo": "Turbo de GPU",
    "Keeps the Adreno GPU at its highest clock. More heat and battery use.":
        "Mantiene la GPU Adreno a su frecuencia máxima. Más calor y consumo de batería.",
    "Sustained performance mode": "Modo de rendimiento sostenido",
    "Asks Android for steady clocks the device can hold without throttling (where supported). Cooler, but the peak speed may be lower.":
        "Pide a Android frecuencias estables que el teléfono pueda mantener sin estrangularse (si es compatible). Más fresco, pero con menor velocidad máxima.",
    "Thermal guard": "Protección térmica",
    "Lowers the draw distance before the device starts throttling and restores it after it has cooled down.":
        "Baja la distancia de dibujado antes de que el teléfono se sobrecaliente y la restaura al enfriarse. Recomendado en el S24 Ultra (activado por defecto).",
    "Safe mode (for bug reports)": "Modo seguro (para reportar errores)",
    "Turns off the port\\'s speed shortcuts and gameplay tweaks. Slower. If a bug is gone in safe mode, mention it in your issue.":
        "Desactiva los atajos de velocidad y ajustes de jugabilidad del port. Más lento. Si un error desaparece en modo seguro, menciónalo en tu reporte.",
    "Water reflections": "Reflejos del agua",
    "Other reflections (cars, windows)": "Otros reflejos (coches, ventanas)",
    "Every frame": "Cada fotograma",
    "Every 2nd frame": "Cada 2 fotogramas",
    "Every 3rd frame": "Cada 3 fotogramas",
    "Off": "Desactivado",
    "Off: the surface shows a plain colour instead of a reflection.":
        "Desactivado: la superficie muestra un color plano en lugar de un reflejo.",
    "Save": "Guardar",
}
