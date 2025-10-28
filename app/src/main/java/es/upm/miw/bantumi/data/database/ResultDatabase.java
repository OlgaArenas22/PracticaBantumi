package es.upm.miw.bantumi.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import es.upm.miw.bantumi.data.database.dao.ResultDao;
import es.upm.miw.bantumi.data.database.entities.ResultEntity;

@Database(entities = {ResultEntity.class}, version = 1, exportSchema = false)
public abstract class ResultDatabase extends RoomDatabase {

    private static volatile ResultDatabase INSTANCE;

    public abstract ResultDao resultDao();

    public static ResultDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ResultDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ResultDatabase.class,
                                    "bantumi.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
