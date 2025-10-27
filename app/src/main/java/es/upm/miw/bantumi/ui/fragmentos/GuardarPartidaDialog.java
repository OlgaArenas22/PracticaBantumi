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
import es.upm.miw.bantumi.ui.actividades.MainActivity;

public class GuardarPartidaDialog extends DialogFragment {

    public GuardarPartidaDialog(){}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle saveInstanceState){
        final MainActivity main = (MainActivity) requireActivity();
        main.stopCronometro();
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_save_game, null);

        Button btnGuardar = view.findViewById(R.id.btnGuardar);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnGuardar.setOnClickListener(v->{
            if(getActivity() instanceof MainActivity){
                main.guardarPartida();
                main.resumeCronometro();
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
