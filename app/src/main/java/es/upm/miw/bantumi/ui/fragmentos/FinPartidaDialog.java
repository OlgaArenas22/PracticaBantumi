package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi;
import es.upm.miw.bantumi.ui.actividades.MainActivity;

public class FinPartidaDialog extends DialogFragment {

    private String player1name;
    private boolean player1won;

    public FinPartidaDialog(String player1Name, boolean player1Won) {
        this.player1name = player1Name;
        this.player1won = player1Won;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final MainActivity main = (MainActivity) requireActivity();

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_end_game, null, false);

        TextView tvTitle   = view.findViewById(R.id.tvTitle);
        TextView tvResult  = view.findViewById(R.id.tvResult);
        TextView tvPlayer1 = view.findViewById(R.id.tvPlayer1);
        ImageView ivCrown  = view.findViewById(R.id.ivCrown);
        Button btnReiniciar = view.findViewById(R.id.btnReiniciar);
        Button btnSalir     = view.findViewById(R.id.btnSalir);

        tvTitle.setText(R.string.txtDialogoFinalTitulo);

        if (player1won) {
            tvResult.setText(R.string.txtGanador);
            ivCrown.setImageResource(R.drawable.ic_crown);
        } else {
            tvResult.setText(R.string.txtPerdedor);
            ivCrown.setImageResource(R.drawable.ic_crown_broken);
        }

        // Debajo del resultado, solo el nombre del jugador 1
        tvPlayer1.setText(player1name);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnReiniciar.setOnClickListener(v -> {
            main.resetCronometro();
            main.startCronometro();
            main.juegoBantumi.inicializar(JuegoBantumi.Turno.turnoJ1);
            dialog.dismiss();
        });

        btnSalir.setOnClickListener(v -> {
            main.finish();
            dialog.dismiss();
        });

        return dialog;
    }
}
