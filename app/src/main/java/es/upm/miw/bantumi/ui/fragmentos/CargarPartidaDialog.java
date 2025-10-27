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
import es.upm.miw.bantumi.ui.actividades.CargarPartidaActivity;

public class CargarPartidaDialog extends DialogFragment {
    private static String filename;

    public CargarPartidaDialog(@NonNull String filename) {
        CargarPartidaDialog.filename = filename;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_load_game, null);

        Button btnAceptar = view.findViewById(R.id.btnAceptar);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnAceptar.setOnClickListener(v->{
            if (getActivity() instanceof CargarPartidaActivity) {
                ((CargarPartidaActivity) getActivity()).onConfirmLoad(filename);
            }
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v->{
            dialog.dismiss();
        });
        return dialog;
    }
}
