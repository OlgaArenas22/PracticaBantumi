package es.upm.miw.bantumi.ui.actividades;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.fragmentos.CargarPartidaDialog;
import es.upm.miw.bantumi.ui.managers.CargarPartidaManager;
import es.upm.miw.bantumi.ui.managers.CargarPartidaManager.SaveMeta;
import es.upm.miw.bantumi.ui.adapters.CargarPartidaAdapter;

public class CargarPartidaActivity extends AppCompatActivity implements CargarPartidaAdapter.OnSaveClick {
    public static final String EXTRA_SELECTED_FILENAME = "selected_filename";

    private CargarPartidaManager cargarMgr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_partida);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cargarPartida), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cargarMgr = new CargarPartidaManager(getApplicationContext());

        ImageButton btnClose = findViewById(R.id.btnCerrar);
        btnClose.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        RecyclerView rv = findViewById(R.id.recycler_saves);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        List<SaveMeta> metas = cargarMgr.listSaves();
        List<CargarPartidaAdapter.Item> items = new ArrayList<>();

        for (SaveMeta m : metas) {
            File thumbFile = new File(getFilesDir(), m.thumbRelativePath);
            items.add(new CargarPartidaAdapter.Item(
                    m.title,
                    thumbFile.exists() ? BitmapFactory.decodeFile(thumbFile.getAbsolutePath()) : null,
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
        new CargarPartidaDialog(filename).show(getSupportFragmentManager(), "CONFIRM_LOAD_GAME");
    }

    /** Llamado por el diálogo al aceptar */
    public void onConfirmLoad(String filename) {
        Intent data = new Intent();
        data.putExtra(EXTRA_SELECTED_FILENAME, filename);
        setResult(RESULT_OK, data);
        finish();
    }
}
