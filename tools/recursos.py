#!/usr/bin/env python3
"""Regenera las imágenes del lanzador en español.

Uso:  python3 -I recursos.py <carpeta_decodificada>
"""
import os
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageFont

D = sys.argv[1]
FUENTE = "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"
nodpi = os.path.join(D, "res", "drawable-nodpi")


def texto_ajustado(lienzo, texto, color, alto_max, ancho_max):
    """Dibuja `texto` centrado lo más grande posible dentro del lienzo."""
    draw = ImageDraw.Draw(lienzo)
    tam = 400
    while tam > 10:
        f = ImageFont.truetype(FUENTE, tam)
        x0, y0, x1, y1 = draw.textbbox((0, 0), texto, font=f)
        if x1 - x0 <= ancho_max and y1 - y0 <= alto_max:
            break
        tam -= 2
    W, H = lienzo.size
    draw.text(((W - (x1 - x0)) / 2 - x0, (H - (y1 - y0)) / 2 - y0), texto, font=f, fill=color)


# --- Botón "Jugar" (antes "Play"), mismo tamaño y color que el original ---
play = Image.new("RGBA", (488, 232), (0, 0, 0, 0))
texto_ajustado(play, "JUGAR", (17, 17, 17, 255), 150, 470)
play.save(os.path.join(nodpi, "play_label.png"))

# --- Banner: "FOR ANDROID" -> "PARA ANDROID · ES" ---
ruta = os.path.join(nodpi, "launcher_banner.png")
banner = Image.open(ruta).convert("RGBA")
arr = np.array(banner)
# Zona de "FOR ANDROID": debajo de la línea negra, a la derecha.
y0, y1, x0 = 330, banner.size[1], 900
zona = arr[y0:y1, x0:, 3]
filas = np.where(zona.max(axis=1) > 0)[0]
cols = np.where(zona.max(axis=0) > 0)[0]
top, bot = y0 + filas.min(), y0 + filas.max() + 1
right = x0 + cols.max() + 1
color = tuple(int(c) for c in arr[top:bot, x0:][arr[top:bot, x0:, 3] == 255][0][:3]) + (255,)
arr[top:bot, x0:, :] = 0
banner = Image.fromarray(arr)
alto = bot - top
f = ImageFont.truetype(FUENTE, 10)
tam = 10
texto = "PARA ANDROID · ES"
d = ImageDraw.Draw(banner)
while True:
    f2 = ImageFont.truetype(FUENTE, tam + 1)
    bx = d.textbbox((0, 0), texto, font=f2)
    if bx[3] - bx[1] > alto:
        break
    tam += 1
    f = f2
bx = d.textbbox((0, 0), texto, font=f)
d.text((right - (bx[2] - bx[0]) - bx[0], top - bx[1]), texto, font=f, fill=color)
banner.save(ruta)
print("Imágenes regeneradas:", f"color={color}", f"alto_texto={alto}px")
