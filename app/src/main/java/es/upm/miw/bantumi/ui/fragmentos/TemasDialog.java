package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.managers.ThemeManager;
import es.upm.miw.bantumi.ui.managers.ThemeManager.ThemeId;

public class TemasDialog extends DialogFragment {

    private static final String ARG_THEME = "arg_theme";

    public static TemasDialog newInstance(ThemeId id) {
        TemasDialog d = new TemasDialog();
        Bundle b = new Bundle();
        b.putString(ARG_THEME, id.name());
        d.setArguments(b);
        return d;
    }

    public TemasDialog() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ThemeId id = ThemeId.CLASICO;
        if (getArguments() != null) {
            String raw = getArguments().getString(ARG_THEME, ThemeId.CLASICO.name());
            try { id = ThemeId.valueOf(raw); } catch (IllegalArgumentException ignore) {}
        }
        final ThemeId selectedId = id;

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_tema_confirm, null);
        TextView txt = view.findViewById(R.id.txtMensaje);
        txt.setText(getString(R.string.temas_confirmar_cambio, readableName(selectedId)));

        Button btnAplicar = view.findViewById(R.id.btnAplicarTema);
        Button btnCancelar = view.findViewById(R.id.btnCancelarTema);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnAplicar.setOnClickListener(v -> {
            // 1) Guardar preferencia
            ThemeManager.setSelectedTheme(requireContext(), selectedId);
            // 2) Aplicar en la Activity (cuando vuelvas a la partida estará ya cambiado)
            ThemeManager.applyThemeToRoot(requireActivity());
            // 3) Aplicar también en el propio fragmento de Temas (preview vivo)
            View temasRoot = requireActivity().findViewById(R.id.temasRoot);
            if (temasRoot != null) {
                int bg = ThemeManager.mapToDrawable(ThemeManager.getSelectedTheme(requireContext()));
                temasRoot.setBackgroundResource(bg);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private String readableName(ThemeId id) {
        switch (id) {
            case VERDE:  return getString(R.string.tema_verde);
            case AZUL:   return getString(R.string.tema_azul);
            case ROJO:   return getString(R.string.tema_rojo);
            case MORADO: return getString(R.string.tema_morado);
            case NEGRO:  return getString(R.string.tema_negro);
            case NEON:   return getString(R.string.tema_neon);
            case DORADO: return getString(R.string.tema_dorado);
            case CLASICO:
            default:     return getString(R.string.tema_clasico);
        }
    }
}
