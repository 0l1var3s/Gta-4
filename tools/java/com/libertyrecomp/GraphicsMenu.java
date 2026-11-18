package com.libertyrecomp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menú "Gráficos del juego" del lanzador (edición en español / S24 Ultra).
 *
 * Lee y escribe Android/data/<paquete>/files/args.txt: las opciones elegidas se
 * guardan en un bloque marcado; "Auto" quita la línea y deja que decida el
 * menú Pantalla del propio juego.
 */
public final class GraphicsMenu {

    // ---- Paleta (igual que el lanzador) ----
    private static final int BG = 0xFF0B0D12;
    private static final int CARD = 0xFF161A22;
    private static final int CHIP = 0xFF20252F;
    private static final int TEXT = 0xFFF3F4F6;
    private static final int MUTED = 0xFF9AA3B2;
    private static final int ACCENT = 0xFFF5B335;
    private static final int ON_ACCENT = 0xFF111111;

    static final String BEGIN = "# >>> MENU GRAFICOS (lo escribe la app: usa el boton Graficos del juego)";
    static final String END = "# <<< MENU GRAFICOS";
    /** Versión del bloque: sube cuando cambia el perfil recomendado. */
    static final int CONFIG_VERSION = 3;
    static final String VERSION_PREFIX = "# menu-version=";
    private static final String BUTTON_ANCHOR = "Ajustes de rendimiento (pruebas)";

    // ------------------------------------------------------------------
    // Definición de ajustes
    // ------------------------------------------------------------------
    static final class Setting {
        final String title;
        final String desc;
        final String[] flags;
        final String[] labels;
        final String[][] values; // values[opción][flag]; null = quitar la línea
        int sel = -1;            // -1 = valor personalizado que no está en la lista
        boolean touched;
        String absent;           // opción que equivale a "sin línea" (valor por defecto del juego)

        Setting absent(String label) {
            find(label);
            this.absent = label;
            return this;
        }

        Setting(String title, String desc, String[] flags, String[] labels, String[][] values) {
            this.title = title;
            this.desc = desc;
            this.flags = flags;
            this.labels = labels;
            this.values = values;
        }

        int find(String label) {
            for (int i = 0; i < labels.length; i++) if (labels[i].equals(label)) return i;
            throw new IllegalArgumentException(label);
        }
    }

    static String[] f(String... s) { return s; }
    static String[] v(String... s) { return s; }

    static Setting one(String title, String desc, String flag, String[] labels, String[] vals) {
        String[][] values = new String[vals.length][];
        for (int i = 0; i < vals.length; i++) values[i] = new String[]{vals[i]};
        return new Setting(title, desc, new String[]{flag}, labels, values);
    }

    static List<Setting> buildSettings() {
        List<Setting> s = new ArrayList<Setting>();
        s.add(one("Resolución de salida",
                "Altura máxima de la imagen. El S24 Ultra tiene una pantalla de 1440p; 1080p es casi igual de nítido y mucho más fresco.",
                "gta4_output_height_cap",
                f("Auto", "720p", "900p", "1080p", "1440p (nativa)"),
                v(null, "720", "900", "1080", "0")));
        s.add(new Setting("Reescalado FSR 1",
                "Renderiza por debajo de la resolución de salida y reescala con AMD FSR 1. Más FPS a cambio de algo de nitidez.",
                f("gta4_native_upscaler", "gta4_fsr1_quality"),
                f("Auto", "Desactivado", "Ultra calidad", "Calidad", "Equilibrado", "Rendimiento"),
                new String[][]{
                        {null, null}, {"native", null}, {"fsr1", "ultra_quality"},
                        {"fsr1", "quality"}, {"fsr1", "balanced"}, {"fsr1", "performance"}}));
        s.add(one("Límite de FPS",
                "30 es lo más estable (como en consola). 40 y 60 encajan con la pantalla de 120 Hz del S24 Ultra.",
                "gta4_frame_limit",
                f("Auto", "30", "40", "60", "Sin límite"),
                v(null, "30", "40", "60", "0")));
        s.add(one("Sincronización vertical (VSync)",
                "Activada evita el tearing (cortes en la imagen).",
                "gta4_present_mode",
                f("Auto", "Activada", "Desactivada"),
                v(null, "vsync", "immediate")));
        s.add(one("Antialiasing",
                "Suaviza los bordes dentados. SMAA es el mejor equilibrio; MSAA es más caro.",
                "gta4_native_anti_aliasing",
                f("Auto", "No", "FXAA", "SMAA", "MSAA 2x", "MSAA 4x"),
                v(null, "off", "fxaa", "smaa", "msaa2x", "msaa4x")));
        s.add(one("Filtrado anisotrópico",
                "Texturas del suelo nítidas a distancia. 4x-8x apenas cuesta en la Adreno 750.",
                "gta4_anisotropic_filtering",
                f("Auto", "1x", "2x", "4x", "8x", "16x"),
                v(null, "1x", "2x", "4x", "8x", "16x")));
        s.add(one("Filtrado de texturas", "Trilineal elimina las transiciones bruscas entre niveles de textura.",
                "gta4_texture_filtering",
                f("Auto", "Bilineal", "Trilineal"),
                v(null, "bilinear", "trilinear")));
        s.add(one("Distancia de dibujado", "Hasta dónde se ven edificios, coches y vegetación.",
                "gta4_draw_distance_scale",
                f("Auto", "Corta 0.75", "Normal 1.0", "Lejana 1.25"),
                v(null, "0.75", "1.0", "1.25")));
        s.add(one("Distancia dinámica",
                "Baja la distancia un poco cuando no se llega a los FPS y la recupera después. Evita tirones.",
                "gta4_dynamic_draw_distance",
                f("Auto", "Activada", "Desactivada"),
                v(null, "true", "false")));
        s.add(one("Calidad de sombras", "Tamaño del mapa de sombras.",
                "gta4_shadow_map_base_size",
                f("Auto", "Baja", "Alta"),
                v(null, "256", "512")));
        s.add(one("Distancia de sombras", "Hasta dónde se dibujan sombras.",
                "gta4_shadow_distance_scale",
                f("Auto", "Corta 0.75", "Normal 1.0", "Lejana 1.25"),
                v(null, "0.75", "1.0", "1.25")));
        s.add(one("Reflejos", "Resolución de los reflejos (agua, coches, ventanas).",
                "gta4_reflection_resolution",
                f("Auto", "Original", "1080p", "Completa"),
                v(null, "original", "1080p", "full")));
        s.add(one("Desenfoque de movimiento", "Efecto de motion blur al girar la cámara y conducir.",
                "gta4_motion_blur",
                f("Auto", "Sí", "No"),
                v(null, "true", "false")));
        s.add(new Setting("Tráfico y peatones",
                "Cantidad de coches y gente en la calle. Más alto = ciudad más viva pero más CPU y calor.",
                f("gta4_traffic_density_scale", "gta4_parked_car_density_scale",
                        "gta4_ped_density_scale", "gta4_scenario_ped_density_scale"),
                f("Auto", "Baja 0.3", "Normal 0.4", "Media 0.5", "Alta 0.7", "Consola 1.0"),
                new String[][]{
                        {null, null, null, null},
                        {"0.3", "0.3", "0.3", "0.3"}, {"0.4", "0.4", "0.4", "0.4"},
                        {"0.5", "0.5", "0.5", "0.5"}, {"0.7", "0.7", "0.7", "0.7"},
                        {"1.0", "1.0", "1.0", "1.0"}}));
        s.add(new Setting("Audio",
                "Estable + prioridad: búfer de audio doble, CPU sin bajar de frecuencia, más hilos de lectura para que la radio no se corte al conducir y prioridad alta para los hilos de sonido. "
                        + "Estable: igual pero sin prioridad ni lectura extra. Ahorro: como la versión original.",
                f("audio_maxqframes", "audio_low_latency", "android_perf_hint",
                        "ignore_thread_priorities", "host_file_io_workers"),
                f("Auto", "Estable + prioridad", "Estable", "Ahorro de batería"),
                new String[][]{
                        {null, null, null, null, null},
                        {"64", "false", "false", "false", "8"},
                        {"64", "false", "false", null, null},
                        {"32", "false", "true", null, null}}));
        s.add(one("Idioma del juego", "Idioma de menús, subtítulos y misiones (necesita que tu disco incluya ese idioma).",
                "user_language",
                f("Español", "English", "Français", "Deutsch", "Italiano"),
                v("5", "1", "4", "3", "6")).absent("English"));
        return s;
    }

    // Presets: etiqueta elegida en cada ajuste (en el mismo orden que buildSettings()).
    static final String[] PRESET_NAMES = {
            "S24 Ultra · Recomendado", "Calidad máxima", "Fluidez 60 FPS", "Batería", "Todo Auto"};
    static final String[][] PRESETS = {
            {"1080p", "Desactivado", "30", "Activada", "SMAA", "4x", "Trilineal", "Normal 1.0", "Activada",
                    "Auto", "Normal 1.0", "Auto", "Auto", "Media 0.5", "Estable + prioridad", null},
            {"1440p (nativa)", "Desactivado", "30", "Activada", "SMAA", "8x", "Trilineal", "Lejana 1.25", "Activada",
                    "Alta", "Lejana 1.25", "1080p", "Sí", "Media 0.5", "Estable + prioridad", null},
            {"1080p", "Calidad", "60", "Activada", "FXAA", "4x", "Trilineal", "Normal 1.0", "Activada",
                    "Baja", "Normal 1.0", "Original", "No", "Normal 0.4", "Estable + prioridad", null},
            {"720p", "Desactivado", "30", "Activada", "FXAA", "2x", "Bilineal", "Corta 0.75", "Activada",
                    "Baja", "Corta 0.75", "Original", "No", "Baja 0.3", "Ahorro de batería", null},
            {"Auto", "Auto", "Auto", "Auto", "Auto", "Auto", "Auto", "Auto", "Auto",
                    "Auto", "Auto", "Auto", "Auto", "Auto", "Auto", null},
    };

    // ------------------------------------------------------------------
    // Botón en el lanzador
    // ------------------------------------------------------------------

    /** Llamado desde LauncherActivity.onCreate: espera al layout y añade el botón. */
    public static void attach(final Activity activity) {
        try {
            ensurePreconfigured(activity);
        } catch (Throwable ignored) {
        }
        final Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            int tries;

            @Override
            public void run() {
                if (activity.isFinishing()) return;
                try {
                    if (insertButton(activity)) {
                        watch(activity);
                        return;
                    }
                } catch (Throwable t) {
                    return;
                }
                if (++tries < 50) h.postDelayed(this, 200);
            }
        }, 150);
    }

    private static TextView sSummary;

    /** Si el lanzador reconstruye su pantalla, vuelve a poner el botón. */
    private static void watch(final Activity a) {
        a.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        try {
                            insertButton(a);
                        } catch (Throwable ignored) {
                        }
                    }
                });
    }

    private static boolean insertButton(final Activity a) {
        View root = a.getWindow().getDecorView();
        View anchor = findByText(root, BUTTON_ANCHOR);
        if (anchor == null || !(anchor.getParent() instanceof LinearLayout)) return false;
        LinearLayout parent = (LinearLayout) anchor.getParent();
        if (parent.findViewWithTag("graficos") != null) return true;
        int index = parent.indexOfChild(anchor);

        Button b = new Button(a);
        b.setTag("graficos");
        b.setText("Gráficos del juego");
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(ACCENT);
        b.setBackgroundDrawable(focusable(a, CARD, ACCENT, 2));
        b.setFocusable(true);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show(a);
            }
        });
        ViewGroup.LayoutParams lp = anchor.getLayoutParams();
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(lp.width, lp.height);
        if (lp instanceof ViewGroup.MarginLayoutParams) nlp.topMargin = ((ViewGroup.MarginLayoutParams) lp).topMargin;
        parent.addView(b, index, nlp);

        TextView sum = new TextView(a);
        sum.setTextColor(MUTED);
        sum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        sum.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(nlp.width, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = dp(a, 4);
        parent.addView(sum, index + 1, slp);
        sSummary = sum;
        updateSummary(a);

        Button cb = new Button(a);
        cb.setTag("controles");
        cb.setText("Controles y mando");
        cb.setAllCaps(false);
        cb.setTypeface(Typeface.DEFAULT_BOLD);
        cb.setTextColor(ACCENT);
        cb.setBackgroundDrawable(focusable(a, CARD, ACCENT, 2));
        cb.setFocusable(true);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlsMenu.show(a);
            }
        });
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(nlp.width, nlp.height);
        clp.topMargin = nlp.topMargin;
        parent.addView(cb, index + 2, clp);
        return true;
    }

    private static View findByText(View v, String text) {
        if (v instanceof TextView && text.contentEquals(((TextView) v).getText())) return v;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View r = findByText(g.getChildAt(i), text);
                if (r != null) return r;
            }
        }
        return null;
    }

    private static void updateSummary(Context c) {
        if (sSummary == null) return;
        try {
            Map<String, String> cur = readFlags(argsFile(c), c);
            StringBuilder sb = new StringBuilder();
            String h = cur.get("gta4_output_height_cap");
            sb.append(h == null ? "Resolución auto" : ("0".equals(h) ? "1440p" : h + "p"));
            String up = cur.get("gta4_native_upscaler");
            if ("fsr1".equals(up)) sb.append(" + FSR 1");
            String fps = cur.get("gta4_frame_limit");
            sb.append(" · ").append(fps == null ? "FPS auto" : ("0".equals(fps) ? "FPS sin límite" : fps + " FPS"));
            String aa = cur.get("gta4_native_anti_aliasing");
            if (aa != null) sb.append(" · ").append(aa.toUpperCase());
            String af = cur.get("gta4_anisotropic_filtering");
            if (af != null) sb.append(" · AF ").append(af);
            String audio = cur.get("audio_maxqframes");
            if ("64".equals(audio)) sb.append(" · Audio estable");
            sSummary.setText(sb.toString());
        } catch (Throwable t) {
            sSummary.setText("");
        }
    }

    // ------------------------------------------------------------------
    // Archivo args.txt
    // ------------------------------------------------------------------

    static File argsFile(Context c) {
        File dir = c.getExternalFilesDir(null);
        if (dir == null) dir = c.getFilesDir();
        return new File(dir, "args.txt");
    }

    static List<String> readLines(File file, Context c) throws IOException {
        InputStream in;
        if (file.isFile()) {
            in = new FileInputStream(file);
        } else {
            in = c.getAssets().open("default_args.txt");
        }
        List<String> lines = new ArrayList<String>();
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        try {
            String l;
            while ((l = r.readLine()) != null) lines.add(l);
        } finally {
            r.close();
        }
        return lines;
    }

    static String flagName(String line) {
        String t = line.trim();
        if (!t.startsWith("--")) return null;
        int eq = t.indexOf('=');
        return eq < 0 ? t.substring(2) : t.substring(2, eq);
    }

    static Map<String, String> readFlags(File file, Context c) throws IOException {
        return parse(readLines(file, c));
    }

    static Map<String, String> parse(List<String> lines) {
        Map<String, String> m = new HashMap<String, String>();
        for (String l : lines) {
            String n = flagName(l);
            if (n == null) continue;
            String t = l.trim();
            int eq = t.indexOf('=');
            m.put(n, eq < 0 ? "true" : t.substring(eq + 1).trim());
        }
        return m;
    }

    static void save(Context c, List<Setting> settings) throws IOException {
        File file = argsFile(c);
        writeLines(file, merge(readLines(file, c), settings));
    }

    static List<String> merge(List<String> lines, List<Setting> settings) {
        // Flags que este guardado va a fijar o quitar.
        List<String> managed = new ArrayList<String>();
        for (Setting s : settings) {
            if (s.sel < 0) continue;
            for (String fl : s.flags) managed.add(fl);
        }
        List<String> out = new ArrayList<String>();
        List<String> kept = new ArrayList<String>();
        boolean inBlock = false;
        for (String l : lines) {
            String t = l.trim();
            if (t.equals(BEGIN)) { inBlock = true; continue; }
            if (t.equals(END)) { inBlock = false; continue; }
            String n = flagName(l);
            if (inBlock) {
                // Conserva valores del bloque que gestiona el otro menú (p. ej. Controles).
                if (n != null && !managed.contains(n)) kept.add(l);
                continue;
            }
            if (n != null && managed.contains(n)) continue;
            out.add(l);
        }
        while (!out.isEmpty() && out.get(out.size() - 1).trim().isEmpty()) out.remove(out.size() - 1);
        out.add("");
        out.add(BEGIN);
        out.add(VERSION_PREFIX + CONFIG_VERSION);
        out.addAll(kept);
        for (Setting s : settings) {
            if (s.sel < 0) continue;
            String[] vals = s.values[s.sel];
            for (int i = 0; i < s.flags.length; i++) {
                if (vals[i] != null) out.add("--" + s.flags[i] + "=" + vals[i]);
            }
        }
        out.add(END);
        return out;
    }

    static void writeLines(File file, List<String> out) throws IOException {
        File dir = file.getParentFile();
        if (dir != null && !dir.isDirectory()) dir.mkdirs();
        File tmp = new File(file.getPath() + ".tmp");
        OutputStream os = new FileOutputStream(tmp);
        OutputStreamWriter w = new OutputStreamWriter(os, "UTF-8");
        try {
            for (String l : out) {
                w.write(l);
                w.write('\n');
            }
        } finally {
            w.close();
        }
        if (!tmp.renameTo(file)) {
            file.delete();
            if (!tmp.renameTo(file)) throw new IOException("no se pudo escribir " + file);
        }
    }

    private static void loadSelection(Context c, List<Setting> settings) {
        Map<String, String> cur;
        try {
            cur = readFlags(argsFile(c), c);
        } catch (IOException e) {
            cur = new HashMap<String, String>();
        }
        select(settings, cur);
    }

    static void select(List<Setting> settings, Map<String, String> cur) {
        for (Setting s : settings) {
            s.sel = -1;
            s.touched = false;
            for (int o = 0; o < s.values.length; o++) {
                boolean match = true;
                for (int i = 0; i < s.flags.length; i++) {
                    String want = s.values[o][i];
                    String have = cur.get(s.flags[i]);
                    if (want == null ? have != null : !want.equalsIgnoreCase(have == null ? "" : have)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    s.sel = o;
                    break;
                }
            }
            // Sin ninguna línea = valor por defecto del juego.
            if (s.sel < 0 && s.absent != null) {
                boolean none = true;
                for (String fl : s.flags) if (cur.containsKey(fl)) none = false;
                if (none) s.sel = s.find(s.absent);
            }
        }
    }

    // ------------------------------------------------------------------
    // Diálogo
    // ------------------------------------------------------------------

    public static void show(final Activity a) {
        final List<Setting> settings = buildSettings();
        loadSelection(a, settings);

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
        int pad = dp(a, 18);
        root.setPadding(pad, dp(a, 12), pad, dp(a, 12));

        // Cabecera
        LinearLayout header = new LinearLayout(a);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titles = new LinearLayout(a);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView title = text(a, "Gráficos del juego", 22, TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titles.addView(title);
        titles.addView(text(a, deviceLine(), 12, MUTED));
        header.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button cancel = button(a, "Cancelar", false);
        Button saveBtn = button(a, "Guardar", true);
        header.addView(cancel, new LinearLayout.LayoutParams(dp(a, 120), dp(a, 42)));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(a, 140), dp(a, 42));
        slp.leftMargin = dp(a, 10);
        header.addView(saveBtn, slp);
        root.addView(header);

        // Presets
        TextView pt = text(a, "Perfiles rápidos", 13, ACCENT);
        pt.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams ptl = new LinearLayout.LayoutParams(-1, -2);
        ptl.topMargin = dp(a, 12);
        root.addView(pt, ptl);

        final List<List<TextView>> chipRows = new ArrayList<List<TextView>>();
        HorizontalScrollView presetScroll = new HorizontalScrollView(a);
        presetScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout presetRow = new LinearLayout(a);
        presetRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int p = 0; p < PRESET_NAMES.length; p++) {
            final int pi = p;
            TextView chip = chip(a, PRESET_NAMES[p], p == 0);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyPreset(settings, pi);
                    for (int i = 0; i < settings.size(); i++) refreshRow(a, chipRows.get(i), settings.get(i).sel);
                    Toast.makeText(a, "Perfil «" + PRESET_NAMES[pi] + "» aplicado. Pulsa Guardar.", Toast.LENGTH_SHORT).show();
                }
            });
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-2, dp(a, 40));
            clp.rightMargin = dp(a, 8);
            presetRow.addView(chip, clp);
        }
        presetScroll.addView(presetRow);
        LinearLayout.LayoutParams psl = new LinearLayout.LayoutParams(-1, -2);
        psl.topMargin = dp(a, 6);
        root.addView(presetScroll, psl);

        // Lista de ajustes
        ScrollView scroll = new ScrollView(a);
        LinearLayout list = new LinearLayout(a);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dp(a, 6), 0, dp(a, 12));
        for (int i = 0; i < settings.size(); i++) {
            final Setting s = settings.get(i);
            LinearLayout card = new LinearLayout(a);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setBackgroundDrawable(rounded(CARD, dp(a, 12), 0, 0));
            card.setPadding(dp(a, 14), dp(a, 10), dp(a, 14), dp(a, 10));

            LinearLayout label = new LinearLayout(a);
            label.setOrientation(LinearLayout.VERTICAL);
            TextView tt = text(a, s.title, 15, TEXT);
            tt.setTypeface(Typeface.DEFAULT_BOLD);
            label.addView(tt);
            label.addView(text(a, s.desc, 11, MUTED));
            card.addView(label, new LinearLayout.LayoutParams(0, -2, 0.42f));

            HorizontalScrollView hs = new HorizontalScrollView(a);
            hs.setHorizontalScrollBarEnabled(false);
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            final List<TextView> chips = new ArrayList<TextView>();
            for (int o = 0; o < s.labels.length; o++) {
                final int oi = o;
                TextView c = chip(a, s.labels[o], false);
                c.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        s.sel = oi;
                        s.touched = true;
                        refreshRow(a, chips, oi);
                    }
                });
                chips.add(c);
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-2, dp(a, 38));
                clp.leftMargin = dp(a, 6);
                row.addView(c, clp);
            }
            refreshRow(a, chips, s.sel);
            chipRows.add(chips);
            hs.addView(row);
            LinearLayout.LayoutParams hsl = new LinearLayout.LayoutParams(0, -2, 0.58f);
            hsl.leftMargin = dp(a, 10);
            card.addView(hs, hsl);

            LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(-1, -2);
            cl.topMargin = dp(a, 8);
            list.addView(card, cl);
        }
        TextView note = text(a,
                "«Auto» deja que decida el menú Pantalla del juego. Los cambios se aplican la próxima vez que pulses Jugar. "
                        + "Se guardan en Android/data/" + a.getPackageName() + "/files/args.txt.", 11, MUTED);
        LinearLayout.LayoutParams nl = new LinearLayout.LayoutParams(-1, -2);
        nl.topMargin = dp(a, 12);
        list.addView(note, nl);
        scroll.addView(list);
        LinearLayout.LayoutParams scl = new LinearLayout.LayoutParams(-1, 0, 1f);
        scl.topMargin = dp(a, 6);
        root.addView(scroll, scl);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    save(a, settings);
                    Toast.makeText(a, "Gráficos guardados. Se aplican al pulsar Jugar.", Toast.LENGTH_SHORT).show();
                    updateSummary(a);
                    d.dismiss();
                } catch (Throwable t) {
                    Toast.makeText(a, "No se pudo guardar: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        d.setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface di, int keyCode, KeyEvent e) {
                if (keyCode == KeyEvent.KEYCODE_BUTTON_B && e.getAction() == KeyEvent.ACTION_UP) {
                    d.dismiss();
                    return true;
                }
                return false;
            }
        });

        d.setContentView(root);
        d.show();
        if (win != null) {
            win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // Inmersivo: oculta barras de estado y navegación.
            win.getDecorView().setSystemUiVisibility(0x1706);
        }
        saveBtn.requestFocus();
    }

    /**
     * Un args.txt de una versión anterior (sin bloque del menú) recibe una vez
     * el perfil "S24 Ultra · Recomendado"; el idioma elegido se respeta.
     */
    static void ensurePreconfigured(Context c) throws IOException {
        File file = argsFile(c);
        if (!file.isFile()) return; // el juego copiará default_args.txt, que ya viene configurado
        List<String> lines = readLines(file, c);
        List<String> out = upgrade(lines);
        if (out != null) writeLines(file, out);
    }

    /** Devuelve las líneas con el perfil recomendado aplicado, o null si no hace falta. */
    static List<String> upgrade(List<String> lines) {
        boolean block = false;
        int version = 0;
        for (String l : lines) {
            String t = l.trim();
            if (t.equals(BEGIN)) block = true;
            if (t.startsWith(VERSION_PREFIX)) {
                try {
                    version = Integer.parseInt(t.substring(VERSION_PREFIX.length()).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (block && version >= CONFIG_VERSION) return null;
        List<Setting> settings = buildSettings();
        select(settings, parse(lines));
        if (!block) {
            applyPreset(settings, 0);
        } else {
            // v2 -> v3: el audio "Estable" pasa a "Estable + prioridad" (corrige la radio).
            for (Setting s : settings) {
                if (s.flags[0].equals("audio_maxqframes") && s.sel == s.find("Estable")) {
                    s.sel = s.find("Estable + prioridad");
                }
            }
        }
        return merge(lines, settings);
    }

    static void applyPreset(List<Setting> settings, int p) {
        String[] labels = PRESETS[p];
        for (int i = 0; i < settings.size() && i < labels.length; i++) {
            if (labels[i] == null) continue; // el idioma no cambia con los perfiles
            Setting s = settings.get(i);
            s.sel = s.find(labels[i]);
            s.touched = true;
        }
    }

    static String deviceLine() {
        String gpu = null;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/sys/class/kgsl/kgsl-3d0/gpu_model"), "UTF-8"));
            try {
                gpu = r.readLine();
            } finally {
                r.close();
            }
        } catch (Throwable ignored) {
        }
        String model = Build.MODEL == null ? "" : Build.MODEL;
        String name = model;
        if (model.startsWith("SM-S928")) name = "Galaxy S24 Ultra";
        StringBuilder sb = new StringBuilder();
        sb.append(Build.MANUFACTURER == null ? "" : capitalize(Build.MANUFACTURER)).append(' ').append(name);
        if (gpu != null && gpu.trim().length() > 0) sb.append(" · ").append(gpu.trim());
        return sb.toString().trim();
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ------------------------------------------------------------------
    // Estilo
    // ------------------------------------------------------------------

    static void refreshRow(Context c, List<TextView> chips, int sel) {
        for (int i = 0; i < chips.size(); i++) styleChip(c, chips.get(i), i == sel);
    }

    static TextView chip(Context c, String label, boolean highlight) {
        TextView t = new TextView(c);
        t.setText(label);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        t.setPadding(dp(c, 14), 0, dp(c, 14), 0);
        t.setFocusable(true);
        t.setClickable(true);
        t.setSingleLine(true);
        styleChip(c, t, false);
        if (highlight) t.setBackgroundDrawable(focusable(c, CHIP, ACCENT, 1));
        return t;
    }

    static void styleChip(Context c, TextView t, boolean selected) {
        if (selected) {
            t.setTextColor(ON_ACCENT);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setBackgroundDrawable(focusable(c, ACCENT, 0, 0));
        } else {
            t.setTextColor(TEXT);
            t.setTypeface(Typeface.DEFAULT);
            t.setBackgroundDrawable(focusable(c, CHIP, 0x33FFFFFF, 1));
        }
    }

    static Button button(Context c, String label, boolean primary) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(primary ? ON_ACCENT : TEXT);
        b.setBackgroundDrawable(focusable(c, primary ? ACCENT : 0xFF272D39, 0, 0));
        b.setFocusable(true);
        return b;
    }

    static TextView text(Context c, String s, int sp, int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        return t;
    }

    static GradientDrawable rounded(int color, int radius, int strokeColor, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        if (strokeDp > 0) g.setStroke(strokeDp, strokeColor);
        return g;
    }

    static StateListDrawable focusable(Context c, int color, int strokeColor, int strokeDp) {
        int r = dp(c, 12);
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, rounded(color, r, Color.WHITE, dp(c, 2)));
        s.addState(new int[]{android.R.attr.state_focused}, rounded(color, r, Color.WHITE, dp(c, 3)));
        s.addState(new int[0], rounded(color, r, strokeColor, strokeDp > 0 ? dp(c, strokeDp) : 0));
        return s;
    }

    static int dp(Context c, int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics()));
    }

    private GraphicsMenu() {
    }
}
