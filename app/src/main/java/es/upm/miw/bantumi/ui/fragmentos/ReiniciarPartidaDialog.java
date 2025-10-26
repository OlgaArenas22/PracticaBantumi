package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.media.MicrophoneDirection;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.actividades.MainActivity;

public class ReiniciarPartidaDialog extends DialogFragment {

    public ReiniciarPartidaDialog(){}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        final MainActivity main = (MainActivity) requireActivity();
        main.stopCronometro();
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reboot_game, null);

        Button btnAceptar = view.findViewById(R.id.btnAceptar);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnAceptar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity){
                main.stopCronometro();
                main.resetCronometro();
                main.startCronometro();
                main.juegoBantumi.inicializar(main.getTurnoInicial());
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v->{
            if(getActivity() instanceof MainActivity){
                main.resumeCronometro();
            }
            dialog.dismiss();
        });
        return dialog;
    }
}
