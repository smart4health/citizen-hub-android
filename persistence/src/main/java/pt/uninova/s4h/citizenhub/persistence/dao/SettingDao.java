package pt.uninova.s4h.citizenhub.persistence.dao;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.entity.SettingRecord;

@Dao
public interface SettingDao {

    @Delete
    void delete(SettingRecord record);

    @Query("INSERT OR REPLACE INTO setting (device_id, `key`, value) VALUES ((SELECT id FROM device WHERE device.address = :address), :key, :value);")
    long insert(String address, String key, String value);

    @Query("SELECT device_id, `key`, value FROM setting INNER JOIN device ON device_id = id WHERE address = :address;")
    List<SettingRecord> select(String address);

    @Query("SELECT value FROM setting INNER JOIN device ON device_id = id WHERE address = :address AND `key` = :key;")
    String selectValue(String address, String key);

    @Update
    void update(SettingRecord record);
}