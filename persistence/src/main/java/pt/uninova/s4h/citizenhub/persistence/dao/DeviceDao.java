package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.entity.DeviceRecord;

@Dao
public interface DeviceDao {

    @Delete
    void delete(DeviceRecord record);

    @Query("DELETE FROM device WHERE address = :address")
    void delete(String address);

    @Query("DELETE FROM device")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(DeviceRecord record);

    @Query("SELECT * FROM device WHERE address =:address")
    DeviceRecord select(String address);

    @Query("SELECT * FROM device")
    List<DeviceRecord> selectAll();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(DeviceRecord record);

    @Query("UPDATE device SET agent = :agent WHERE address = :address")
    void updateAgent(String address, String agent);
}
