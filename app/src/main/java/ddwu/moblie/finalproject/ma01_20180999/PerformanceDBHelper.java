package ddwu.moblie.finalproject.ma01_20180999;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PerformanceDBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "performance_db";
    public final static String TABLE_NAME = "performance_table";
    public final static String COL_ID = "_id";
    public final static String COL_TITLE = "title";
    public final static String COL_VENUE = "venue";
    public final static String COL_PERIOD = "period";
    public final static String COL_MEMO = "memo";
    public final static String COL_IMGPATH = "imgPath";

    public PerformanceDBHelper(Context context) { super(context, DB_NAME, null, 1); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COL_ID + " integer primary key autoincrement,"
                + COL_TITLE + " TEXT, " + COL_VENUE + " TEXT, " + COL_PERIOD + " TEXT, " + COL_MEMO + " TEXT, " + COL_IMGPATH + " TEXT);");

        //샘플 데이터
//        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, 'title입니다', '장소입니다', '기간입니다', '메모입니다', null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table " + TABLE_NAME);
        onCreate(db);
    }
}
