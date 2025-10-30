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
    @Query("SELECT * FROM results " +
            "ORDER BY seedsPlayer1 DESC, id ASC " +
            "LIMIT 10")
    List<ResultEntity> getTop10ByBestSeedsList();

    @Query("SELECT * FROM results " +
            "WHERE (:wins IS NULL OR player1Won = :wins) " +
            "AND (:mode IS NULL OR mode = :mode) " +
            "AND (:name IS NULL OR LOWER(player1Name) LIKE '%' || LOWER(:name) || '%') " +
            "ORDER BY seedsPlayer1 DESC, id ASC " +
            "LIMIT 10")
    List<ResultEntity> getFilteredDesc(Integer wins, String mode, String name);

    @Query("SELECT * FROM results " +
            "WHERE (:wins IS NULL OR player1Won = :wins) " +
            "AND (:mode IS NULL OR mode = :mode) " +
            "AND (:name IS NULL OR LOWER(player1Name) LIKE '%' || LOWER(:name) || '%') " +
            "ORDER BY seedsPlayer1 ASC, id ASC " +
            "LIMIT 10")
    List<ResultEntity> getFilteredAsc(Integer wins, String mode, String name);


    @Query("DELETE FROM results")
    void clearAll();
}
