package pt.uninova.s4h.citizenhub.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FeatureDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Feature feature);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Feature feature);

    @Delete
    void delete(Feature feature);

    @Query("DELETE FROM feature WHERE device_address =:address")
    void deleteAll(String address);

    @Query("SELECT * FROM feature WHERE device_address =:address")
    List<Feature> getAll(String address);

    @Query("SELECT * FROM feature WHERE uuid =:feature_uuid")
    Feature get(String feature_uuid);

    @Query("SELECT * FROM feature")
    LiveData<List<Feature>> getAllLive();


}
