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
import es.upm.miw.bantumi.ui.actividades.MainActivity;
import es.upm.miw.bantumi.ui.viewmodel.ResultadosViewModel;

public class AcercaDeDialog extends DialogFragment {

    public AcercaDeDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_acerca_de, null);

        Button btnCerrar = view.findViewById(R.id.btnCerrar);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        btnCerrar.setOnClickListener(v -> {
            if(getActivity() instanceof MainActivity){
                MainActivity main = (MainActivity) getActivity();
                main.resumeCronometro();
                dialog.dismiss();
            }
        });

        return dialog;
    }
}
