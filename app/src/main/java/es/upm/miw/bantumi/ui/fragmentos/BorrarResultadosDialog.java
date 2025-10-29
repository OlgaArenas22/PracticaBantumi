package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.viewmodel.ResultadosViewModel;

public class BorrarResultadosDialog extends DialogFragment {

    public BorrarResultadosDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_results, null);

        ResultadosViewModel vm = new ViewModelProvider(requireActivity()).get(ResultadosViewModel.class);

        Button btnAceptar = view.findViewById(R.id.btnAceptar);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnAceptar.setOnClickListener(v -> {
            vm.confirmDeleteAll();
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }
}
