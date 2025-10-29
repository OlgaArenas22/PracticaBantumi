package es.upm.miw.bantumi.data.network;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.upm.miw.bantumi.data.database.ResultDatabase;
import es.upm.miw.bantumi.data.database.dao.ResultDao;
import es.upm.miw.bantumi.data.database.entities.ResultEntity;
import es.upm.miw.bantumi.ui.viewmodel.FilterState;

public class ResultRepository {
    private final ResultDao dao;
    private final ExecutorService io;
    private static volatile ResultRepository INSTANCE;

    private ResultRepository(Context appContext) {
        this.dao = ResultDatabase.getInstance(appContext).resultDao();
        this.io  = Executors.newSingleThreadExecutor();
    }


    // NUEVO: lista sin LiveData
    public List<ResultEntity> getTop10ByBestSeedsList() {
        return dao.getTop10ByBestSeedsList();
    }

    public void deleteAll(Callback callback) { /* igual que tienes */ }

    public void insertAsync(ResultEntity entity) {
        io.execute(() -> {
            try {
                dao.insert(entity);   // @Insert puede devolver long si quieres loguear el rowId
            } catch (Exception e) {
                android.util.Log.e("DB_DEBUG", "Error al insertar", e);
            }
        });
    }
    public interface Callback { void onSuccess(); void onError(Exception e); }

    public List<ResultEntity> getFilteredResults(FilterState f) {
        Integer wins = null;
        if (f != null && f.outcome != null) {
            if (f.outcome == FilterState.Outcome.WINS) wins = 1;
            else if (f.outcome == FilterState.Outcome.LOSSES) wins = 0;
        }
        String mode = (f != null && f.mode != null && !f.mode.trim().isEmpty()) ? f.mode.trim() : null;
        String name = (f != null && f.nameContains != null && !f.nameContains.trim().isEmpty()) ? f.nameContains.trim() : null;

        if (f == null || f.order == null || f.order == FilterState.Order.SEEDS_DESC) {
            return dao.getFilteredDesc(wins, mode, name);
        } else {
            return dao.getFilteredAsc(wins, mode, name);
        }
    }

    public static ResultRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ResultRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ResultRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }
}

