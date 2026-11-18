package com.libertyrecomp;

import java.io.File;
import java.nio.file.*;
import java.util.*;

/**
 * Prueba en la JVM de la lógica de args.txt y de la reasignación de botones.
 * Uso: java -cp clases:android-4.1.1.4.jar com.libertyrecomp.PruebaMenu args_version1.txt args_version2.txt default_args.txt
 */
public class PruebaMenu {
    static int n;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        n++;
    }

    static Map<String, String> p(List<String> l) { return GraphicsMenu.parse(l); }

    public static void main(String[] a) throws Exception {
        List<String> v1 = Files.readAllLines(Paths.get(a[0]));
        List<String> v2 = Files.readAllLines(Paths.get(a[1]));
        List<String> def = Files.readAllLines(Paths.get(a[2]));

        // 1. Migración desde la versión 1 (sin bloque)
        List<String> up1 = GraphicsMenu.upgrade(v1);
        check(up1 != null, "v1 se migra");
        Map<String, String> m = p(up1);
        check("1080".equals(m.get("gta4_output_height_cap")), "v1: 1080p");
        check("false".equals(m.get("ignore_thread_priorities")) && "8".equals(m.get("host_file_io_workers")), "v1: audio v3");
        check("5".equals(m.get("user_language")), "v1: idioma");
        check(GraphicsMenu.upgrade(up1) == null, "v1: solo una vez");

        // 2. Migración desde la versión 2 (bloque sin versión): solo cambia el audio
        List<String> up2 = GraphicsMenu.upgrade(v2);
        check(up2 != null, "v2 se migra");
        Map<String, String> m2 = p(up2);
        check("false".equals(m2.get("ignore_thread_priorities")) && "8".equals(m2.get("host_file_io_workers"))
                && "64".equals(m2.get("audio_maxqframes")), "v2: audio Estable -> Estable + prioridad");
        check("30".equals(m2.get("gta4_frame_limit")) && "smaa".equals(m2.get("gta4_native_anti_aliasing")), "v2: gráficos intactos");
        check(GraphicsMenu.upgrade(up2) == null, "v2: solo una vez");
        // v2 con audio "Ahorro" elegido por el usuario: se respeta
        List<String> v2b = new ArrayList<String>();
        for (String l : v2) v2b.add(l.replace("--audio_maxqframes=64", "--audio_maxqframes=32").replace("--android_perf_hint=false", "--android_perf_hint=true"));
        check(!p(GraphicsMenu.upgrade(v2b)).containsKey("ignore_thread_priorities"), "v2: Ahorro se respeta");
        check(GraphicsMenu.upgrade(def) == null, "default_args ya es v3");

        // 3. default_args == lo que escribe el menú
        List<GraphicsMenu.Setting> s = GraphicsMenu.buildSettings();
        GraphicsMenu.select(s, p(def));
        for (GraphicsMenu.Setting x : s) check(x.sel >= 0, "default reconocido: " + x.title);
        check(GraphicsMenu.merge(def, s).equals(def), "guardar sin cambios no altera default_args");

        // 4. Perfiles
        for (int k = 0; k < GraphicsMenu.PRESET_NAMES.length; k++) {
            List<GraphicsMenu.Setting> t = GraphicsMenu.buildSettings();
            GraphicsMenu.select(t, p(def));
            GraphicsMenu.applyPreset(t, k);
            List<String> out = GraphicsMenu.merge(def, t);
            List<GraphicsMenu.Setting> r = GraphicsMenu.buildSettings();
            GraphicsMenu.select(r, p(out));
            for (int i = 0; i < t.size(); i++) check(t.get(i).sel == r.get(i).sel, "perfil " + k + " " + t.get(i).title);
        }

        // 5. Menú de controles y de gráficos comparten el bloque
        List<GraphicsMenu.Setting> c = ControlsMenu.buildSettings();
        GraphicsMenu.select(c, p(def));
        check(c.get(0).sel == c.get(0).find("Ocultar con mando"), "touch_controls ausente = auto");
        check(c.get(1).sel == c.get(1).find("Mando Xbox"), "layout ausente = gamepad");
        c.get(0).sel = c.get(0).find("Quitar siempre");
        c.get(1).sel = c.get(1).find("Contextuales");
        List<String> withCtl = GraphicsMenu.merge(def, c);
        Map<String, String> mc = p(withCtl);
        check("off".equals(mc.get("touch_controls")) && "context".equals(mc.get("gta4_touch_layout")), "controles guardados");
        check("1080".equals(mc.get("gta4_output_height_cap")), "gráficos conservados al guardar controles");
        List<GraphicsMenu.Setting> g = GraphicsMenu.buildSettings();
        GraphicsMenu.select(g, mc);
        g.get(2).sel = g.get(2).find("60");
        List<String> both = GraphicsMenu.merge(withCtl, g);
        Map<String, String> mb = p(both);
        check("off".equals(mb.get("touch_controls")) && "60".equals(mb.get("gta4_frame_limit")), "controles conservados al guardar gráficos");
        int begin = both.indexOf(GraphicsMenu.BEGIN), end = both.indexOf(GraphicsMenu.END);
        int tc = -1;
        for (int i = 0; i < both.size(); i++) if (both.get(i).startsWith("--touch_controls")) tc = i;
        check(begin < tc && tc < end, "touch_controls queda dentro del bloque");
        int count = 0;
        for (String l : both) if (l.startsWith(GraphicsMenu.VERSION_PREFIX)) count++;
        check(count == 1, "una sola línea de versión");

        // 6. Reasignación
        int[] as = ControlsMenu.loadAssign(new File("/no/existe"));
        for (int i = 0; i < as.length; i++) check(as[i] == ControlsMenu.CODES[i], "identidad");
        int A = 0, B = 1;
        ControlsMenu.assign(as, A, ControlsMenu.CODES[B]); // A del juego <- botón B
        check(as[A] == ControlsMenu.CODES[B] && as[B] == ControlsMenu.CODES[A], "intercambio A/B");
        File tmp = File.createTempFile("controles", ".txt");
        Files.write(tmp.toPath(), ControlsMenu.remapLines(as));
        int[] map = GamepadRemap.parse(tmp);
        check(map[ControlsMenu.CODES[B]] == ControlsMenu.CODES[A] && map[ControlsMenu.CODES[A]] == ControlsMenu.CODES[B], "controles.txt: físico->juego");
        int[] back = ControlsMenu.loadAssign(tmp);
        check(Arrays.equals(back, as), "controles.txt se relee igual");
        tmp.delete();

        System.out.println("OK: " + n + " comprobaciones");
    }
}
