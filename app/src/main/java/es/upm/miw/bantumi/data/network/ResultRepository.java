package es.upm.miw.bantumi.data.network;

import android.accessibilityservice.TouchInteractionController;
import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.upm.miw.bantumi.data.database.dao.ResultDao;
import es.upm.miw.bantumi.data.database.entities.ResultEntity;

public class ResultRepository {
    private final ResultDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public ResultRepository(Context context) {
        this.dao = es.upm.miw.bantumi.data.database.ResultDatabase.getInstance(context).resultDao();
    }

    public LiveData<List<ResultEntity>> getTop10ByBestSeeds() {
        return dao.getTop10ByBestSeedsLive();
    }

    public void deleteAll(Callback callback) {
        io.execute(() -> {
            try {
                dao.clearAll();
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }


    public void insertAsync(ResultEntity entity) {
        io.execute(() -> dao.insert(entity));
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }
}
