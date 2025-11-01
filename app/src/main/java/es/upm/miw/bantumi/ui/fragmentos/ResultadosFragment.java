package es.upm.miw.bantumi.ui.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.data.database.entities.ResultEntity;
import es.upm.miw.bantumi.ui.adapters.ResultadosAdapter;
import es.upm.miw.bantumi.ui.viewmodel.FilterState;
import es.upm.miw.bantumi.ui.viewmodel.ResultadosViewModel;

public class ResultadosFragment extends Fragment {

    private ResultadosViewModel viewModel;
    private ResultadosAdapter adapter;
    private RecyclerView recyclerView;
    private ImageButton btnClose;
    private ImageButton btnDeleteAll;
    private TextView emptyText;

    public ResultadosFragment() {
    }

    public static ResultadosFragment newInstance() {
        return new ResultadosFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_resultados, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.results_recycler);
        btnClose = view.findViewById(R.id.btn_close_results);
        btnDeleteAll = view.findViewById(R.id.btn_delete_all);
        emptyText = view.findViewById(R.id.results_empty_text);
        ImageButton btnFilter = view.findViewById(R.id.btn_filter);

        viewModel = new ViewModelProvider(requireActivity()).get(ResultadosViewModel.class);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        adapter = new ResultadosAdapter();
        recyclerView.setAdapter(adapter);

        btnClose.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnFilter.setOnClickListener(v -> {
            FilterState current = viewModel.getCurrentFilter();
            FiltrosResultadosBottomSheet
                    .newInstance(current, new FiltrosResultadosBottomSheet.OnApplyFilters() {
                        @Override
                        public void onApply(FilterState filter) {
                            viewModel.applyFilter(filter);
                        }
                        @Override
                        public void onClear() {
                            viewModel.applyFilter(FilterState.defaults());
                        }
                    })
                    .show(getParentFragmentManager(), "FiltroResultadosDialog");
        });


        btnDeleteAll.setOnClickListener(v -> {
            if (adapter == null || adapter.getItemCount() == 0) {
                new NingunResultadoBorrarDialog()
                        .show(getParentFragmentManager(), "NingunResultadoBorrarDialog");
            } else {
                new BorrarResultadosDialog()
                        .show(getParentFragmentManager(), "BorrarResultadosDialog");
            }
        });

        viewModel.getResults().observe(getViewLifecycleOwner(), this::renderList);
        viewModel.getDeleteAllSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success) && getView() != null) {
                Snackbar.make(getView(), R.string.txtResultsDeletedOK, Snackbar.LENGTH_SHORT).show();
            }
        });
        viewModel.getDeleteAllError().observe(getViewLifecycleOwner(), error -> {
            if (Boolean.TRUE.equals(error) && getView() != null) {
                Snackbar.make(getView(), R.string.txtResultsDeletedERROR, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.loadDefault();

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }
        );
    }

    /** Actualiza la lista del ranking */
    private void renderList(List<ResultEntity> list) {
        adapter.submit(list);
        boolean empty = (list == null || list.isEmpty());
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}
