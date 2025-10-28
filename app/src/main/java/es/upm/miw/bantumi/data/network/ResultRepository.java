package es.upm.miw.bantumi.data.network;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.upm.miw.bantumi.data.database.dao.ResultDao;
import es.upm.miw.bantumi.data.database.entities.ResultEntity;
import es.upm.miw.bantumi.data.database.ResultDatabase;

public class ResultRepository {
    private final ResultDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public ResultRepository(Context context) {
        this.dao = es.upm.miw.bantumi.data.database.ResultDatabase.getInstance(context).resultDao();
    }

    public void insertAsync(ResultEntity entity) {
        io.execute(() -> dao.insert(entity));
    }
}
