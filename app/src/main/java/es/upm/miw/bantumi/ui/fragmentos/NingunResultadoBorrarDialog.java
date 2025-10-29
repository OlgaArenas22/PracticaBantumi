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

public class NingunResultadoBorrarDialog extends DialogFragment {

    public NingunResultadoBorrarDialog() {
        // requerido
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_no_results_to_delete, null);

        Button btnAceptar = view.findViewById(R.id.btnAceptar);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(true)
                .create();

        btnAceptar.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }
}
