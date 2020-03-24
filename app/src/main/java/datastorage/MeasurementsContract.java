package datastorage;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class MeasurementsContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public static final String CONTENT_AUTHORITY = "pt.uninova.s4h.citizenhub";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MEASUREMENTS = "measurements";
    private MeasurementsContract() {}


    public static abstract class MeasureEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MEASUREMENTS);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEASUREMENTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEASUREMENTS;

        public final static String TABLE_NAME = "measurements";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_MEASUREMENT_VALUE = "value";
        public final static String COLUMN_CHARACTERISTIC_NAME = "characteristic";
    }

}
