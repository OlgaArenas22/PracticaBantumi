package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.viewmodel.CargarPartidaViewModel;

public class EliminarPartidaDialog extends DialogFragment {

    private String filename;

    public EliminarPartidaDialog(String filename){
        this.filename = filename;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_game, null);
        CargarPartidaViewModel vm =
                new ViewModelProvider(requireActivity()).get(CargarPartidaViewModel.class);

        Button btnAceptar = view.findViewById(R.id.btnAceptar);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnAceptar.setOnClickListener(v->{
            vm.confirmDelete(filename);
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v->{
            dialog.dismiss();
        });
        return dialog;
    }
}
