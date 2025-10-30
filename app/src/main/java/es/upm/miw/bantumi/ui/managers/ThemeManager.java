package es.upm.miw.bantumi.ui.temas;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import es.upm.miw.bantumi.R;

public final class ThemeManager {

    public enum ThemeId {
        CLASICO, VERDE, AZUL, ROJO, MORADO, NEGRO, NEON, DORADO
    }

    private static final String PREFS_FILE = "bantumi_prefs";
    private static final String KEY_THEME = "selected_theme";

    private ThemeManager() {}

    public static ThemeId getSelectedTheme(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_THEME, ThemeId.CLASICO.name());
        try {
            return ThemeId.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return ThemeId.CLASICO;
        }
    }

    public static void setSelectedTheme(Context ctx, ThemeId id) {
        ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, id.name())
                .apply();
    }

    public static void applyThemeToRoot(Activity activity) {
        if (activity == null) return;
        View root = activity.findViewById(R.id.main);
        if (root == null) return;
        int res = mapToDrawable(getSelectedTheme(activity));
        root.setBackgroundResource(res);
    }

    public static int mapToDrawable(ThemeId id) {
        switch (id) {
            case VERDE:  return R.drawable.fondo_bantumi_verde;
            case AZUL:   return R.drawable.fondo_bantumi_azul;
            case ROJO:   return R.drawable.fondo_bantumi_rojo;
            case MORADO: return R.drawable.fondo_bantumi_morado;
            case NEGRO:  return R.drawable.fondo_bantumi_negro;
            case NEON:   return R.drawable.fondo_bantumi_neon;
            case DORADO: return R.drawable.fondo_bantumi_dorado;
            case CLASICO:
            default:     return R.drawable.fondo_bantumi_clasico;
        }
    }
}
