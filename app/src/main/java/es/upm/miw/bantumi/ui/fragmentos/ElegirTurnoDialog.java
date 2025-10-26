package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi;
import es.upm.miw.bantumi.ui.actividades.MainActivity;

public class ElegirTurnoDialog extends DialogFragment {

    private final String nombreBtn1;

    public ElegirTurnoDialog(String nombreBtn1) {
        this.nombreBtn1 = nombreBtn1;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_starting_player, null);

        Button btnJ1 = view.findViewById(R.id.btnPlayer1);
        Button btnJ2 = view.findViewById(R.id.btnPlayer2);

        btnJ1.setText(nombreBtn1);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnJ1.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onIniciarPartida(JuegoBantumi.Turno.turnoJ1);
            }
            dialog.dismiss();
        });

        btnJ2.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onIniciarPartida(JuegoBantumi.Turno.turnoJ2);
            }
            dialog.dismiss();
        });

        return dialog;
    }
}
