package es.upm.miw.bantumi.ui.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.viewmodel.FilterState;

public class FiltrosResultadosBottomSheet extends BottomSheetDialogFragment {

    public interface OnApplyFilters {
        void onApply(FilterState filter);
        void onClear();
    }

    private static final String ARG_FILTER = "arg_filter";

    private FilterState current;
    private OnApplyFilters callback;

    public FiltrosResultadosBottomSheet() {
    }

    public static FiltrosResultadosBottomSheet newInstance(@Nullable FilterState filter,
                                                           OnApplyFilters callback) {
        FiltrosResultadosBottomSheet sheet = new FiltrosResultadosBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILTER, filter != null ? filter : FilterState.defaults());
        sheet.setArguments(args);
        sheet.setOnApplyFilters(callback);
        return sheet;
    }

    public void setOnApplyFilters(OnApplyFilters callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        current = (FilterState) (getArguments() != null
                ? getArguments().getSerializable(ARG_FILTER)
                : FilterState.defaults());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(),
                com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog);

        View view = LayoutInflater.from(dialog.getContext())
                .inflate(R.layout.bottomsheet_filtros_resultados, null, false);
        dialog.setContentView(view);

        RadioButton rbAsc   = view.findViewById(R.id.rbSeedsAsc);
        RadioButton rbDesc  = view.findViewById(R.id.rbSeedsDesc);

        RadioButton rbAll   = view.findViewById(R.id.rbAll);
        RadioButton rbWins  = view.findViewById(R.id.rbWins);
        RadioButton rbLosses= view.findViewById(R.id.rbLosses);

        RadioButton rbAllModes = view.findViewById(R.id.rbAllModes);
        RadioButton rbModeA    = view.findViewById(R.id.rbModeA);
        RadioButton rbModeB    = view.findViewById(R.id.rbModeB);
        RadioButton rbModeC    = view.findViewById(R.id.rbModeC);

        EditText etName = view.findViewById(R.id.etPlayerName);
        Button btnClear = view.findViewById(R.id.btnClear);
        Button btnApply = view.findViewById(R.id.btnApply);

        if (rbAsc == null || rbDesc == null || rbAll == null || rbWins == null ||
                rbLosses == null || rbAllModes == null || rbModeA == null ||
                rbModeB == null || rbModeC == null || etName == null ||
                btnClear == null || btnApply == null) {
            dismiss();
            return dialog;
        }

        if (current != null) {
            if (current.order == FilterState.Order.SEEDS_ASC) rbAsc.setChecked(true);
            else rbDesc.setChecked(true);

            if (current.outcome == FilterState.Outcome.WINS) rbWins.setChecked(true);
            else if (current.outcome == FilterState.Outcome.LOSSES) rbLosses.setChecked(true);
            else rbAll.setChecked(true);

            if (current.mode == null) rbAllModes.setChecked(true);
            else if ("Clásico".equals(current.mode)) rbModeA.setChecked(true);
            else if ("Rápido".equals(current.mode)) rbModeB.setChecked(true);
            else if ("Fiebre de la semilla".equals(current.mode)) rbModeC.setChecked(true);
            else rbAllModes.setChecked(true);

            etName.setText(current.nameContains != null ? current.nameContains : "");
        } else {
            rbDesc.setChecked(true);
            rbAll.setChecked(true);
            rbAllModes.setChecked(true);
        }

        btnClear.setOnClickListener(v -> {
            if (callback != null) callback.onClear();
            dismiss();
        });

        btnApply.setOnClickListener(v -> {
            FilterState fs = new FilterState();
            fs.order = rbAsc.isChecked() ? FilterState.Order.SEEDS_ASC : FilterState.Order.SEEDS_DESC;
            if (rbWins.isChecked()) fs.outcome = FilterState.Outcome.WINS;
            else if (rbLosses.isChecked()) fs.outcome = FilterState.Outcome.LOSSES;
            else fs.outcome = FilterState.Outcome.ALL;

            if (rbModeA.isChecked()) fs.mode = "Clásico";
            else if (rbModeB.isChecked()) fs.mode = "Rápido";
            else if (rbModeC.isChecked()) fs.mode = "Fiebre de la semilla";
            else fs.mode = null;

            String name = etName.getText().toString().trim();
            fs.nameContains = name.isEmpty() ? null : name;

            if (callback != null) callback.onApply(fs);
            dismiss();
        });

        return dialog;
    }
}

