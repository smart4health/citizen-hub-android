package pt.uninova.s4h.citizenhub.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MedExDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(MedEx medEx);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(MedEx medEx);

    @Delete
    void delete(MedEx medEx);

    void deleteAll();

    List<MedEx> getAll();


}
