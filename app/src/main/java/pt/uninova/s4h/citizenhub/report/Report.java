package pt.uninova.s4h.citizenhub.report;

import java.util.LinkedList;
import java.util.List;

/** Class responsible to save the information to put in the different reports. */
public class Report {

    private final LocalizedResource title;
    private final LocalizedResource date;
    private final List<Group> groups;

    public Report(LocalizedResource title, LocalizedResource date) {
        this.title = title;
        this.date = date;
        this.groups = new LinkedList<>();
    }

    public LocalizedResource getTitle() {
        return title;
    }

    public LocalizedResource getDate() {
        return date;
    }

    public List<Group> getGroups() {
        return groups;
    }
}
