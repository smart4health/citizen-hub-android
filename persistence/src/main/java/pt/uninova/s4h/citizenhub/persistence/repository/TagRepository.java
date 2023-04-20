package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.TagDao;
import pt.uninova.s4h.citizenhub.persistence.entity.TagRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the tag dao. */
public class TagRepository {

    private final TagDao tagDao;

    public TagRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        tagDao = citizenHubDatabase.tagDao();
    }

    /** Inserts a tag sample in the database.
     * @param record Record.
     * */
    public void create(TagRecord record) {
        CitizenHubDatabase.executorService().execute(() -> tagDao.insert(record));
    }

    /** Inserts a tag sample in the database given its id and label.
     * @param sampleId Id.
     * @param label Label.
     * */
    public void create(Long sampleId, Integer label) {
        CitizenHubDatabase.executorService().execute(() -> tagDao.insert(sampleId, label));
    }

    /** Selects a tag given its label.
     * @param label Label.
     * @param observer
     * */
    public void selectBasedOnLabel(Integer label, Observer<List<Integer>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(tagDao.selectBasedOnLabel(label)));
    }

    /** Updates a tag label.
     * @param sampleId Id of the sample to be updated.
     * @param label New label.
     * */
    public void updateLabel(Long sampleId, Integer label){
        CitizenHubDatabase.executorService().execute(() -> tagDao.updateLabel(sampleId, label));
    }
}