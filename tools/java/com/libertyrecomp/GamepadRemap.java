package com.libertyrecomp;

import android.content.Context;
import android.view.InputDevice;
import android.view.KeyEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Reasigna botones del mando dentro del juego (proceso :game).
 *
 * SDLActivity.dispatchKeyEvent llama a {@link #remap(Context, KeyEvent)} antes de que
 * SDL vea la pulsación. Las reglas están en Android/data/&lt;paquete&gt;/files/controles.txt
 * con el formato "codigo_fisico=codigo_en_juego" (códigos de KeyEvent).
 */
public final class GamepadRemap {
    static final String FILE = "controles.txt";
    private static final int MAX = 512;

    private static int[] sMap;
    private static long sCheckedAt;
    private static long sModified = -1;
    private static File sFile;

    public static KeyEvent remap(Context c, KeyEvent e) {
        try {
            if (e == null) return e;
            int src = e.getSource();
            boolean pad = (src & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (src & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
            if (!pad) return e;
            int[] m = load(c);
            int code = e.getKeyCode();
            if (m == null || code <= 0 || code >= MAX) return e;
            int to = m[code];
            if (to <= 0 || to == code) return e;
            return new KeyEvent(e.getDownTime(), e.getEventTime(), e.getAction(), to, e.getRepeatCount(),
                    e.getMetaState(), e.getDeviceId(), e.getScanCode(), e.getFlags(), e.getSource());
        } catch (Throwable t) {
            return e;
        }
    }

    private static int[] load(Context c) {
        long now = System.currentTimeMillis();
        if (sMap != null && now - sCheckedAt < 3000) return sMap;
        sCheckedAt = now;
        if (sFile == null) sFile = file(c);
        File f = sFile;
        if (f == null) return sMap;
        long mod = f.isFile() ? f.lastModified() : 0;
        if (sMap != null && mod == sModified) return sMap;
        sModified = mod;
        sMap = parse(f);
        return sMap;
    }

    static File file(Context c) {
        if (c == null) return null;
        File dir = c.getExternalFilesDir(null);
        if (dir == null) dir = c.getFilesDir();
        return new File(dir, FILE);
    }

    static int[] parse(File f) {
        int[] m = new int[MAX];
        if (f == null || !f.isFile()) return m;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            try {
                String l;
                while ((l = r.readLine()) != null) {
                    int hash = l.indexOf('#');
                    if (hash >= 0) l = l.substring(0, hash);
                    l = l.trim();
                    int eq = l.indexOf('=');
                    if (eq <= 0) continue;
                    try {
                        int from = Integer.parseInt(l.substring(0, eq).trim());
                        int to = Integer.parseInt(l.substring(eq + 1).trim());
                        if (from > 0 && from < MAX && to > 0 && to < MAX) m[from] = to;
                    } catch (NumberFormatException ignored) {
                    }
                }
            } finally {
                r.close();
            }
        } catch (Throwable ignored) {
        }
        return m;
    }

    private GamepadRemap() {
    }
}
