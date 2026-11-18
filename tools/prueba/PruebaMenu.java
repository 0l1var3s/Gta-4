package com.libertyrecomp;
import java.nio.file.*; import java.util.*;
public class PruebaMenu {
  static void check(boolean c, String m){ if(!c) throw new AssertionError(m); System.out.println("OK  "+m); }
  public static void main(String[] a) throws Exception {
    List<String> old = Files.readAllLines(Paths.get(a[0]));
    List<String> def = Files.readAllLines(Paths.get(a[1]));
    // 1. Migración de la versión anterior
    List<String> up = GraphicsMenu.upgrade(old);
    check(up != null, "args.txt antiguo se migra");
    Map<String,String> m = GraphicsMenu.parse(up);
    check("1080".equals(m.get("gta4_output_height_cap")), "1080p aplicado");
    check("64".equals(m.get("audio_maxqframes")) && "false".equals(m.get("android_perf_hint")), "audio estable aplicado");
    check("5".equals(m.get("user_language")), "idioma español conservado");
    check("0.5".equals(m.get("gta4_ped_density_scale")), "densidad 0.5");
    check("72".equals(m.get("android_thermal_guard_hot_c")), "ajustes no gestionados conservados");
    long n = up.stream().filter(l->l.trim().startsWith("--gta4_anisotropic_filtering")).count();
    check(n == 1, "sin flags duplicados (AF aparece "+n+" vez)");
    check(GraphicsMenu.upgrade(up) == null, "la migración solo ocurre una vez");
    // 2. default_args nuevo == lo que generaría el menú con el perfil recomendado
    List<GraphicsMenu.Setting> s = GraphicsMenu.buildSettings();
    GraphicsMenu.select(s, GraphicsMenu.parse(def));
    for (GraphicsMenu.Setting x : s) check(x.sel >= 0, "default_args: '"+x.title+"' reconocido -> "+(x.sel>=0?x.labels[x.sel]:"?"));
    List<String> re = GraphicsMenu.merge(def, s);
    check(re.equals(def), "guardar sin cambios no altera default_args.txt");
    // 3. Cada perfil: todos los labels existen y se pueden releer
    for (int p=0;p<GraphicsMenu.PRESET_NAMES.length;p++){
      List<GraphicsMenu.Setting> t = GraphicsMenu.buildSettings();
      GraphicsMenu.select(t, GraphicsMenu.parse(def));
      GraphicsMenu.applyPreset(t,p);
      List<String> out = GraphicsMenu.merge(def,t);
      List<GraphicsMenu.Setting> r = GraphicsMenu.buildSettings();
      GraphicsMenu.select(r, GraphicsMenu.parse(out));
      for (int i=0;i<t.size();i++) check(t.get(i).sel==r.get(i).sel, "perfil "+GraphicsMenu.PRESET_NAMES[p]+": "+t.get(i).title);
      if (p==4) { Map<String,String> mm=GraphicsMenu.parse(out); check(!mm.containsKey("gta4_frame_limit") && "5".equals(mm.get("user_language")), "Todo Auto quita líneas y mantiene idioma"); }
    }
    // 4. Inglés
    List<GraphicsMenu.Setting> e = GraphicsMenu.buildSettings();
    GraphicsMenu.select(e, GraphicsMenu.parse(def));
    e.get(e.size()-1).sel = e.get(e.size()-1).find("English");
    check("1".equals(GraphicsMenu.parse(GraphicsMenu.merge(def,e)).get("user_language")), "cambiar a inglés");
    
  }
}
