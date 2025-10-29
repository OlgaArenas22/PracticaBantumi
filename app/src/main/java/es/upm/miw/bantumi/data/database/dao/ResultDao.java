package es.upm.miw.bantumi.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import es.upm.miw.bantumi.data.database.entities.ResultEntity;

@Dao
public interface ResultDao {
    @Insert
    void insert(ResultEntity result);
    @Query("SELECT * FROM results ORDER BY CASE WHEN seedsPlayer1 >= seedsPlayer2 THEN seedsPlayer1 ELSE seedsPlayer2 END DESC, id ASC LIMIT 10")
    LiveData<List<ResultEntity>> getTop10ByBestSeedsLive();
    @Query("DELETE FROM results")
    void clearAll();
}
