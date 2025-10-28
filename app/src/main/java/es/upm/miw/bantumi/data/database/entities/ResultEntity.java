package es.upm.miw.bantumi.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "results")
public class ResultEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String winnerName;
    public String player1Name;
    public int seedsPlayer1;
    public int seedsPlayer2;
    public long elapsedMillis;
    public String mode;
    public long finishedAtUtc;
    public boolean player1Won;

    public ResultEntity(String winnerName,
                        String player1Name,
                        int seedsPlayer1,
                        int seedsPlayer2,
                        long elapsedMillis,
                        String mode,
                        long finishedAtUtc,
                        boolean player1Won) {
        this.winnerName = winnerName;
        this.player1Name = player1Name;
        this.seedsPlayer1 = seedsPlayer1;
        this.seedsPlayer2 = seedsPlayer2;
        this.elapsedMillis = elapsedMillis;
        this.mode = mode;
        this.finishedAtUtc = finishedAtUtc;
        this.player1Won = player1Won;
    }
}
