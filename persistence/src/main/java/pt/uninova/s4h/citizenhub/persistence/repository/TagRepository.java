package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.TagDao;
import pt.uninova.s4h.citizenhub.persistence.entity.TagRecord;

public class TagRepository {

    private final TagDao tagDao;

    public TagRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        tagDao = citizenHubDatabase.tagDao();
    }

    public void create(TagRecord record) {
        CitizenHubDatabase.executorService().execute(() -> tagDao.insert(record));
    }

    public void create(Long sampleId, Integer label) {
        CitizenHubDatabase.executorService().execute(() -> tagDao.insert(sampleId, label));
    }
}
