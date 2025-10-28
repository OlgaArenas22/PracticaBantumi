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
        CargarPartidaManager cargarMgr = new CargarPartidaManager(requireContext().getApplicationContext());

        ImageButton btnCerrar = v.findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(view -> vm.requestCancel());

        RecyclerView rv = v.findViewById(R.id.recycler_saves);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));

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
        rv.setAdapter(new CargarPartidaAdapter(items, this));
    }

    @Override
    public void onSaveClicked(String filename) {
        if (filename == null || filename.isEmpty()) return;
        new CargarPartidaDialog(filename).show(getActivity().getSupportFragmentManager(), "CONFIRM_LOAD_GAME");
    }
}
