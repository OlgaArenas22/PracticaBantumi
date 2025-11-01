package es.upm.miw.bantumi.ui.fragmentos;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.adapters.CargarPartidaAdapter;
import es.upm.miw.bantumi.ui.managers.CargarPartidaManager;
import es.upm.miw.bantumi.ui.managers.CargarPartidaManager.SaveMeta;
import es.upm.miw.bantumi.ui.viewmodel.CargarPartidaViewModel;

public class CargarPartidaFragment extends Fragment implements CargarPartidaAdapter.OnSaveClick {

    private CargarPartidaViewModel vm;
    private CargarPartidaManager cargarMgr;
    private CargarPartidaAdapter adapter;
    private RecyclerView rv;

    public CargarPartidaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_cargar_partida, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        vm = new ViewModelProvider(requireActivity()).get(CargarPartidaViewModel.class);
        super.onViewCreated(v, s);
        cargarMgr = new CargarPartidaManager(requireContext().getApplicationContext());

        ImageButton btnCerrar = v.findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(view -> vm.requestCancel());

        rv = v.findViewById(R.id.recycler_saves);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        adapter = new CargarPartidaAdapter(buildItems(), this);

        adapter.setOnDeleteClick(filename -> {
            if (filename == null || filename.isEmpty()) return;
            new EliminarPartidaDialog(filename)
                    .show(requireActivity().getSupportFragmentManager(), "CONFIRM_DELETE_GAME");
        });

        rv.setAdapter(adapter);

        vm.deleteConfirmed.observe(getViewLifecycleOwner(), filename -> {
            if (filename == null || filename.isEmpty()) return;
            boolean ok = cargarMgr.deleteSave(filename);
            if (ok) {
                adapter = new CargarPartidaAdapter(buildItems(), this);
                adapter.setOnDeleteClick(f -> {
                    if (f == null || f.isEmpty()) return;
                    new EliminarPartidaDialog(f)
                            .show(requireActivity().getSupportFragmentManager(), "CONFIRM_DELETE_GAME");
                });
                rv.setAdapter(adapter);
            }
            vm.clearEvents();
        });
    }

    private List<CargarPartidaAdapter.Item> buildItems() {
        List<SaveMeta> metas = cargarMgr.listSaves();
        List<CargarPartidaAdapter.Item> items = new ArrayList<>();
        for (SaveMeta m : metas) {
            File f = new File(requireContext().getFilesDir(), m.thumbRelativePath);
            items.add(new CargarPartidaAdapter.Item(
                    m.title,
                    f.exists() ? BitmapFactory.decodeFile(f.getAbsolutePath()) : null,
                    m.filename
            ));
        }
        for (int i = items.size(); i < CargarPartidaManager.MAX_SAVES; i++) {
            items.add(CargarPartidaAdapter.Item.placeholder());
        }
        return items;
    }

    @Override
    public void onSaveClicked(String filename) {
        if (filename == null || filename.isEmpty()) return;
        new CargarPartidaDialog(filename).show(getActivity().getSupportFragmentManager(), "CONFIRM_LOAD_GAME");
    }
}
