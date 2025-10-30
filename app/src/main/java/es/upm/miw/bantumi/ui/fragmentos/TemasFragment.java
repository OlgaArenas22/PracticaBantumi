package es.upm.miw.bantumi.ui.fragmentos;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.material.button.MaterialButton;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.temas.ThemeManager;
import es.upm.miw.bantumi.ui.temas.ThemeManager.ThemeId;

public class TemasFragment extends Fragment {

    public TemasFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        if (requireActivity() instanceof es.upm.miw.bantumi.ui.actividades.MainActivity) {
            ((es.upm.miw.bantumi.ui.actividades.MainActivity) requireActivity()).stopCronometro();
        }

        // 👉 Fondo OPACO del fragmento (tapa la partida que está debajo)
        View root = v.findViewById(R.id.temasRoot);
        int bg = ThemeManager.mapToDrawable(ThemeManager.getSelectedTheme(requireContext()));
        root.setBackgroundResource(bg);

        // Cerrar (X)
        ImageButton btnClose = v.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Botones de tema -> diálogo confirmación
        bind(v, R.id.btnClasico, ThemeId.CLASICO);
        bind(v, R.id.btnVerde,   ThemeId.VERDE);
        bind(v, R.id.btnAzul,    ThemeId.AZUL);
        bind(v, R.id.btnRojo,    ThemeId.ROJO);
        bind(v, R.id.btnMorado,  ThemeId.MORADO);
        bind(v, R.id.btnNegro,   ThemeId.NEGRO);
        bind(v, R.id.btnNeon,    ThemeId.NEON);
        bind(v, R.id.btnDorado,  ThemeId.DORADO);
    }

    private void bind(View root, int btnId, ThemeId id) {
        MaterialButton b = root.findViewById(btnId);
        b.setOnClickListener(view ->
                TemasDialog.newInstance(id).show(getParentFragmentManager(), "CONFIRM_TEMA"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // ▶️ Reanudar cronómetro al salir de Temas (back o X)
        if (requireActivity() instanceof es.upm.miw.bantumi.ui.actividades.MainActivity) {
            ((es.upm.miw.bantumi.ui.actividades.MainActivity) requireActivity()).resumeCronometro();
        }
    }
}
