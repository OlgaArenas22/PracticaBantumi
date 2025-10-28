package es.upm.miw.bantumi.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import es.upm.miw.bantumi.data.database.entities.ResultEntity;

@Dao
public interface ResultDao {
    @Insert
    void insert(ResultEntity result);

    @Query("SELECT * FROM results ORDER BY finishedAtUtc DESC")
    List<ResultEntity> getAllDesc();

    @Query("DELETE FROM results")
    void clearAll();
}
