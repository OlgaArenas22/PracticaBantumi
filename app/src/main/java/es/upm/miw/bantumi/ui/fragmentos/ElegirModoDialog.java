package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.actividades.MainActivity;

public class ElegirModoDialog extends DialogFragment {

    public ElegirModoDialog() {}
    private int semillas;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_game_mode, null);

        ImageButton btnClasico = view.findViewById(R.id.btnClasico);
        ImageButton btnRapido = view.findViewById(R.id.btnRapido);
        ImageButton btnFiebre = view.findViewById(R.id.btnFiebre);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        View.OnClickListener listener = v -> {

            if (v.getId() == R.id.btnClasico) {
                semillas = 4;
            } else if (v.getId() == R.id.btnRapido) {
                semillas = 2;
            } else if (v.getId() == R.id.btnFiebre) {
                semillas = 8;
            }

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onModoSeleccionado(semillas);
            }

            dialog.dismiss();
        };

        btnClasico.setOnClickListener(listener);
        btnRapido.setOnClickListener(listener);
        btnFiebre.setOnClickListener(listener);

        return dialog;
    }
}
