package pt.uninova.s4h.citizenhub.persistence.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import pt.uninova.s4h.citizenhub.persistence.entity.TagRecord;

@Dao
public interface TagDao {

    @Insert
    long insert(TagRecord record);

    @Query("INSERT INTO tag (sample_id, label) VALUES (:sampleId, :label)")
    long insert(Long sampleId, Integer label);

    @Query("SELECT sample_id FROM tag WHERE label == :label")
    List<Integer> selectBasedOnLabel(Integer label);
}