package com.libertyrecomp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.input.InputManager;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/** Menú "Controles y mando" del lanzador. */
public final class ControlsMenu {

    private static final int BG = 0xFF0B0D12;
    private static final int CARD = 0xFF161A22;
    private static final int TEXT = 0xFFF3F4F6;
    private static final int MUTED = 0xFF9AA3B2;
    private static final int ACCENT = 0xFFF5B335;
    private static final int OK = 0xFF5AE673;
    private static final int WARN = 0xFFFF7A6B;

    // Botones que se pueden reasignar (los gatillos, sticks y cruceta van por ejes).
    static final int[] CODES = {
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_START};
    static final String[] NAMES = {"A", "B", "X", "Y", "LB", "RB", "L3", "R3", "Back", "Start"};

    static List<GraphicsMenu.Setting> buildSettings() {
        List<GraphicsMenu.Setting> s = new ArrayList<GraphicsMenu.Setting>();
        s.add(GraphicsMenu.one("Controles en pantalla",
                "«Ocultar con mando»: desaparecen al conectar un mando (Bluetooth o USB). «Quitar siempre»: no se muestran nunca (necesitas un mando para jugar).",
                "touch_controls",
                GraphicsMenu.f("Ocultar con mando", "Siempre visibles", "Quitar siempre"),
                GraphicsMenu.v("auto", "on", "off")).absent("Ocultar con mando"));
        s.add(GraphicsMenu.one("Estilo de los controles táctiles",
                "«Mando Xbox»: un mando completo fijo en pantalla. «Contextuales»: solo aparecen los botones útiles en cada momento (a pie, en coche, apuntando).",
                "gta4_touch_layout",
                GraphicsMenu.f("Mando Xbox", "Contextuales"),
                GraphicsMenu.v("gamepad", "context")).absent("Mando Xbox"));
        s.add(GraphicsMenu.one("Giroscopio del mando",
                "Apuntar y conducir inclinando el mando, si tiene sensor de movimiento (DualSense, DualShock 4, Switch Pro).",
                "gta4_motion_enabled",
                GraphicsMenu.f("Activado", "Desactivado"),
                GraphicsMenu.v("true", "false")).absent("Activado"));
        return s;
    }

    // ------------------------------------------------------------------
    // Reasignación: assign[i] = código físico que produce el botón CODES[i]
    // ------------------------------------------------------------------

    static int[] loadAssign(File f) {
        int[] map = GamepadRemap.parse(f); // físico -> en juego
        int[] assign = new int[CODES.length];
        for (int i = 0; i < CODES.length; i++) assign[i] = CODES[i];
        for (int phys = 1; phys < map.length; phys++) {
            int to = map[phys];
            if (to <= 0) continue;
            int li = indexOf(to);
            if (li >= 0 && indexOf(phys) >= 0) assign[li] = phys;
        }
        return assign;
    }

    static int indexOf(int code) {
        for (int i = 0; i < CODES.length; i++) if (CODES[i] == code) return i;
        return -1;
    }

    /** El botón en juego {@code logical} pasa a dispararse con {@code phys}; se intercambia con el que lo tenía. */
    static void assign(int[] assign, int logical, int phys) {
        int old = assign[logical];
        for (int i = 0; i < assign.length; i++) {
            if (i != logical && assign[i] == phys) assign[i] = old;
        }
        assign[logical] = phys;
    }

    static List<String> remapLines(int[] assign) {
        List<String> out = new ArrayList<String>();
        out.add("# Reasignacion de botones del mando (la escribe el menu Controles y mando).");
        out.add("# boton_fisico=boton_en_juego (codigos KeyEvent de Android)");
        for (int i = 0; i < CODES.length; i++) {
            if (assign[i] != CODES[i]) {
                out.add(assign[i] + "=" + CODES[i] + "  # " + nameOf(assign[i]) + " -> " + NAMES[i]);
            }
        }
        return out;
    }

    static String nameOf(int code) {
        int i = indexOf(code);
        return i >= 0 ? NAMES[i] : ("tecla " + code);
    }

    private static void writeRemap(Context c, int[] assign) throws IOException {
        File f = GamepadRemap.file(c);
        File dir = f.getParentFile();
        if (dir != null && !dir.isDirectory()) dir.mkdirs();
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        try {
            for (String l : remapLines(assign)) {
                w.write(l);
                w.write('\n');
            }
        } finally {
            w.close();
        }
    }

    // ------------------------------------------------------------------
    // Diálogo
    // ------------------------------------------------------------------

    public static void show(final Activity a) {
        final List<GraphicsMenu.Setting> settings = buildSettings();
        try {
            GraphicsMenu.select(settings, GraphicsMenu.readFlags(GraphicsMenu.argsFile(a), a));
        } catch (IOException e) {
            GraphicsMenu.select(settings, new java.util.HashMap<String, String>());
        }
        final int[] assign = loadAssign(GamepadRemap.file(a));
        final int[] capture = {-1};

        final Dialog d = new Dialog(a, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Window win = d.getWindow();
        if (win != null) {
            win.setBackgroundDrawable(new ColorDrawable(BG));
            win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            try {
                WindowManager.LayoutParams wlp = win.getAttributes();
                wlp.getClass().getField("layoutInDisplayCutoutMode").setInt(wlp, 1);
                win.setAttributes(wlp);
            } catch (Throwable ignored) {
            }
        }

        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        int pad = GraphicsMenu.dp(a, 18);
        root.setPadding(pad, GraphicsMenu.dp(a, 12), pad, GraphicsMenu.dp(a, 12));

        // Cabecera
        LinearLayout header = new LinearLayout(a);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titles = new LinearLayout(a);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView title = GraphicsMenu.text(a, "Controles y mando", 22, TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titles.addView(title);
        final TextView lastKey = GraphicsMenu.text(a, "Pulsa cualquier botón del mando para probarlo.", 12, MUTED);
        titles.addView(lastKey);
        header.addView(titles, new LinearLayout.LayoutParams(0, -2, 1f));
        Button cancel = GraphicsMenu.button(a, "Cancelar", false);
        Button save = GraphicsMenu.button(a, "Guardar", true);
        header.addView(cancel, new LinearLayout.LayoutParams(GraphicsMenu.dp(a, 120), GraphicsMenu.dp(a, 42)));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(GraphicsMenu.dp(a, 140), GraphicsMenu.dp(a, 42));
        slp.leftMargin = GraphicsMenu.dp(a, 10);
        header.addView(save, slp);
        root.addView(header);

        ScrollView scroll = new ScrollView(a);
        LinearLayout list = new LinearLayout(a);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, GraphicsMenu.dp(a, 6), 0, GraphicsMenu.dp(a, 12));

        // --- Mandos conectados ---
        LinearLayout padCard = card(a);
        padCard.setOrientation(LinearLayout.VERTICAL);
        TextView pt = GraphicsMenu.text(a, "Mandos (Bluetooth / USB)", 15, TEXT);
        pt.setTypeface(Typeface.DEFAULT_BOLD);
        padCard.addView(pt);
        final TextView pads = GraphicsMenu.text(a, "", 13, MUTED);
        padCard.addView(pads);
        padCard.addView(GraphicsMenu.text(a,
                "Para emparejar: pon el mando en modo emparejamiento (Xbox: botón de emparejar; DualSense: PS + Create; Switch Pro: botón superior) "
                        + "y elígelo en los ajustes Bluetooth. El juego lo detecta solo al conectarlo, también en mitad de la partida.",
                11, MUTED));
        LinearLayout padButtons = new LinearLayout(a);
        padButtons.setOrientation(LinearLayout.HORIZONTAL);
        Button bt = GraphicsMenu.button(a, "Abrir ajustes Bluetooth", true);
        Button refresh = GraphicsMenu.button(a, "Actualizar", false);
        LinearLayout.LayoutParams b1 = new LinearLayout.LayoutParams(GraphicsMenu.dp(a, 220), GraphicsMenu.dp(a, 40));
        LinearLayout.LayoutParams b2 = new LinearLayout.LayoutParams(GraphicsMenu.dp(a, 130), GraphicsMenu.dp(a, 40));
        b2.leftMargin = GraphicsMenu.dp(a, 10);
        padButtons.addView(bt, b1);
        padButtons.addView(refresh, b2);
        LinearLayout.LayoutParams pbl = new LinearLayout.LayoutParams(-2, -2);
        pbl.topMargin = GraphicsMenu.dp(a, 8);
        padCard.addView(padButtons, pbl);
        list.addView(padCard, cardParams(a));
        final Runnable updatePads = new Runnable() {
            @Override
            public void run() {
                List<String> names = connectedPads();
                if (names.isEmpty()) {
                    pads.setText("No hay ningún mando conectado. Se usan los controles táctiles.");
                    pads.setTextColor(WARN);
                } else {
                    StringBuilder sb = new StringBuilder("Conectado: ");
                    for (int i = 0; i < names.size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(names.get(i));
                    }
                    pads.setText(sb.toString());
                    pads.setTextColor(OK);
                }
            }
        };
        updatePads.run();
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    a.startActivity(new Intent("android.settings.BLUETOOTH_SETTINGS"));
                } catch (Throwable t) {
                    Toast.makeText(a, "No se pudieron abrir los ajustes Bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePads.run();
            }
        });
        final InputManager im = (InputManager) a.getSystemService(Context.INPUT_SERVICE);
        final InputManager.InputDeviceListener listener = new InputManager.InputDeviceListener() {
            @Override
            public void onInputDeviceAdded(int id) { updatePads.run(); }

            @Override
            public void onInputDeviceRemoved(int id) { updatePads.run(); }

            @Override
            public void onInputDeviceChanged(int id) { updatePads.run(); }
        };
        if (im != null) im.registerInputDeviceListener(listener, null);

        // --- Ajustes (chips) ---
        for (final GraphicsMenu.Setting s : settings) {
            LinearLayout c = card(a);
            c.setOrientation(LinearLayout.HORIZONTAL);
            c.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout label = new LinearLayout(a);
            label.setOrientation(LinearLayout.VERTICAL);
            TextView tt = GraphicsMenu.text(a, s.title, 15, TEXT);
            tt.setTypeface(Typeface.DEFAULT_BOLD);
            label.addView(tt);
            label.addView(GraphicsMenu.text(a, s.desc, 11, MUTED));
            c.addView(label, new LinearLayout.LayoutParams(0, -2, 0.5f));
            HorizontalScrollView hs = new HorizontalScrollView(a);
            hs.setHorizontalScrollBarEnabled(false);
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            final List<TextView> chips = new ArrayList<TextView>();
            for (int o = 0; o < s.labels.length; o++) {
                final int oi = o;
                TextView ch = GraphicsMenu.chip(a, s.labels[o], false);
                ch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        s.sel = oi;
                        s.touched = true;
                        GraphicsMenu.refreshRow(a, chips, oi);
                    }
                });
                chips.add(ch);
                LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(-2, GraphicsMenu.dp(a, 38));
                cl.leftMargin = GraphicsMenu.dp(a, 6);
                row.addView(ch, cl);
            }
            GraphicsMenu.refreshRow(a, chips, s.sel);
            hs.addView(row);
            LinearLayout.LayoutParams hsl = new LinearLayout.LayoutParams(0, -2, 0.5f);
            hsl.leftMargin = GraphicsMenu.dp(a, 10);
            c.addView(hs, hsl);
            list.addView(c, cardParams(a));
        }

        // --- Editor táctil ---
        LinearLayout ed = card(a);
        ed.setOrientation(LinearLayout.VERTICAL);
        TextView et = GraphicsMenu.text(a, "Mover y cambiar el tamaño de los botones táctiles", 15, TEXT);
        et.setTypeface(Typeface.DEFAULT_BOLD);
        ed.addView(et);
        ed.addView(GraphicsMenu.text(a,
                "El editor está dentro del juego: pausa la partida y en los ajustes busca «Touch Controls» → «Edit Touch Layout» "
                        + "(esas opciones del port aparecen en inglés). Arrastra cada botón y guarda; queda en touch-controls.ini.",
                12, MUTED));
        list.addView(ed, cardParams(a));

        // --- Reasignar botones ---
        LinearLayout rc = card(a);
        rc.setOrientation(LinearLayout.VERTICAL);
        TextView rt = GraphicsMenu.text(a, "Reasignar botones del mando", 15, TEXT);
        rt.setTypeface(Typeface.DEFAULT_BOLD);
        rc.addView(rt);
        final TextView status = GraphicsMenu.text(a,
                "Toca una acción y luego pulsa en el mando el botón que quieres usar. Los botones se intercambian, así no queda ninguno repetido. "
                        + "Los gatillos (LT/RT), los sticks y la cruceta no se pueden reasignar.", 11, MUTED);
        rc.addView(status);
        final List<TextView> rowViews = new ArrayList<TextView>();
        LinearLayout grid = new LinearLayout(a);
        grid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout line = null;
        for (int i = 0; i < CODES.length; i++) {
            if (i % 2 == 0) {
                line = new LinearLayout(a);
                line.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(-1, -2);
                ll.topMargin = GraphicsMenu.dp(a, 6);
                grid.addView(line, ll);
            }
            final int li = i;
            TextView r = GraphicsMenu.chip(a, "", false);
            r.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            r.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    capture[0] = li;
                    refreshAssign(a, rowViews, assign, capture[0]);
                    status.setText("Pulsa en el mando el botón que hará de «" + NAMES[li] + "» en el juego…");
                    status.setTextColor(ACCENT);
                }
            });
            rowViews.add(r);
            LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(0, GraphicsMenu.dp(a, 44), 1f);
            if (i % 2 == 1) rl.leftMargin = GraphicsMenu.dp(a, 8);
            line.addView(r, rl);
        }
        refreshAssign(a, rowViews, assign, -1);
        rc.addView(grid);
        Button reset = GraphicsMenu.button(a, "Restablecer botones", false);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < CODES.length; i++) assign[i] = CODES[i];
                capture[0] = -1;
                refreshAssign(a, rowViews, assign, -1);
                status.setText("Botones restablecidos. Pulsa Guardar.");
                status.setTextColor(MUTED);
            }
        });
        LinearLayout.LayoutParams rsl = new LinearLayout.LayoutParams(GraphicsMenu.dp(a, 220), GraphicsMenu.dp(a, 40));
        rsl.topMargin = GraphicsMenu.dp(a, 10);
        rc.addView(reset, rsl);
        list.addView(rc, cardParams(a));

        scroll.addView(list);
        LinearLayout.LayoutParams scl = new LinearLayout.LayoutParams(-1, 0, 1f);
        scl.topMargin = GraphicsMenu.dp(a, 6);
        root.addView(scroll, scl);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    GraphicsMenu.save(a, settings);
                    writeRemap(a, assign);
                    Toast.makeText(a, "Controles guardados. Se aplican al pulsar Jugar.", Toast.LENGTH_SHORT).show();
                    d.dismiss();
                } catch (Throwable t) {
                    Toast.makeText(a, "No se pudo guardar: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface di, int keyCode, KeyEvent e) {
                InputDevice dev = e.getDevice();
                boolean fromPad = KeyEvent.isGamepadButton(keyCode)
                        || (dev != null && (dev.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD);
                if (capture[0] >= 0) {
                    if (!fromPad) return false;
                    if (e.getAction() != KeyEvent.ACTION_DOWN || e.getRepeatCount() > 0) return true;
                    if (indexOf(keyCode) < 0) {
                        status.setText("Ese botón no se puede reasignar (" + KeyEvent.keyCodeToString(keyCode)
                                + "). Prueba con A, B, X, Y, LB, RB, L3, R3, Back o Start.");
                        status.setTextColor(WARN);
                        return true;
                    }
                    int li = capture[0];
                    assign(assign, li, keyCode);
                    capture[0] = -1;
                    refreshAssign(a, rowViews, assign, -1);
                    status.setText("«" + NAMES[li] + "» ahora es el botón " + nameOf(keyCode) + " del mando. Pulsa Guardar.");
                    status.setTextColor(OK);
                    return true;
                }
                if (fromPad && e.getAction() == KeyEvent.ACTION_DOWN) {
                    String devName = dev != null ? dev.getName() : "mando";
                    int li = indexOf(keyCode);
                    String what = li >= 0 ? NAMES[li] : KeyEvent.keyCodeToString(keyCode).replace("KEYCODE_", "");
                    lastKey.setText("Último botón: " + what + "  (" + devName + ")");
                    lastKey.setTextColor(OK);
                }
                if (keyCode == KeyEvent.KEYCODE_BUTTON_B && e.getAction() == KeyEvent.ACTION_UP) {
                    d.dismiss();
                    return true;
                }
                return false;
            }
        });
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface di) {
                if (im != null) im.unregisterInputDeviceListener(listener);
            }
        });

        d.setContentView(root);
        d.show();
        if (win != null) {
            win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            win.getDecorView().setSystemUiVisibility(0x1706);
        }
        save.requestFocus();
    }

    private static void refreshAssign(Context c, List<TextView> rows, int[] assign, int capturing) {
        for (int i = 0; i < rows.size(); i++) {
            TextView r = rows.get(i);
            String phys = assign[i] == CODES[i] ? "mando " + NAMES[i] : "mando " + nameOf(assign[i]) + "  ✱";
            if (i == capturing) phys = "pulsa un botón…";
            r.setText(NAMES[i] + " del juego   ←   " + phys);
            GraphicsMenu.styleChip(c, r, i == capturing);
            r.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }
    }

    static List<String> connectedPads() {
        List<String> names = new ArrayList<String>();
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice dev = InputDevice.getDevice(id);
            if (dev == null) continue;
            int s = dev.getSources();
            boolean pad = (s & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (s & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
            if (!pad || dev.isVirtual()) continue;
            String n = dev.getName();
            if (n != null && !names.contains(n)) names.add(n);
        }
        return names;
    }

    private static LinearLayout card(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setBackgroundDrawable(GraphicsMenu.rounded(CARD, GraphicsMenu.dp(c, 12), 0, 0));
        l.setPadding(GraphicsMenu.dp(c, 14), GraphicsMenu.dp(c, 10), GraphicsMenu.dp(c, 14), GraphicsMenu.dp(c, 10));
        return l;
    }

    private static LinearLayout.LayoutParams cardParams(Context c) {
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(-1, -2);
        l.topMargin = GraphicsMenu.dp(c, 8);
        return l;
    }

    private ControlsMenu() {
    }
}
