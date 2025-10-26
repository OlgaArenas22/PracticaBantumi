package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.actividades.MainActivity;

public class ElegirNombreDialog extends DialogFragment {

    public ElegirNombreDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_player_name, null);
        TextInputEditText etNombre = content.findViewById(R.id.etPlayerName);
        Button btnConfirmar = content.findViewById(R.id.btnConfirmarNombre);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(content)
                .setCancelable(false)
                .create();

        btnConfirmar.setOnClickListener(v -> {
            String nombreNuevo = etNombre.getText() == null ? "" : etNombre.getText().toString().trim();

            // Actualizar el TextView y lanzar el siguiente diálogo
            if (getActivity() instanceof MainActivity) {
                MainActivity main = (MainActivity) getActivity();
                ((TextView) main.findViewById(R.id.tvPlayer1)).setText(nombreNuevo);
                main.onMostrarElegirTurno();
            }
            dialog.dismiss();
        });

        return dialog;
    }
}
