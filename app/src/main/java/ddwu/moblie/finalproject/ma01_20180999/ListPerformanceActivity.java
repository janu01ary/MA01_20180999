package ddwu.moblie.finalproject.ma01_20180999;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListPerformanceActivity extends AppCompatActivity {

    private final int UPDATE_CODE = 200;

    private ListView lvPerformanceBookmark = null;
    private PerformanceDBHelper helper;
    private Cursor cursor;
    private PerformanceCursorAdapter adapter;
    private SQLiteDatabase db;

    private Intent intent = null;
    private ArrayList<Performance> performanceList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_performance);

        lvPerformanceBookmark = (ListView) findViewById(R.id.lvPerformanceBookmark);
        helper = new PerformanceDBHelper(this);
        adapter = new PerformanceCursorAdapter(this, R.layout.listview_layout, null);
        lvPerformanceBookmark.setAdapter(adapter);

        //리스트뷰 클릭 - 세부 내용 보기(+수정)
        lvPerformanceBookmark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Performance performance = pickPerformance(id);
                Intent intent = new Intent(ListPerformanceActivity.this, UpdatePerformanceActivity.class);
                intent.putExtra("performance", performance);
                intent.putExtra("type", "update");
                startActivityForResult(intent, UPDATE_CODE);
            }
        });

        //리스트뷰 롱클릭 - 삭제 확인 후 삭제
        lvPerformanceBookmark.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final long _id = id;
                AlertDialog.Builder builder = new AlertDialog.Builder(ListPerformanceActivity.this);
                builder.setTitle(pickPerformance(_id).getTitle())
                        .setMessage("삭제하시겠습니까?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (removePerformance(_id)) {
                                    Toast.makeText(ListPerformanceActivity.this, "삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                    setAdapterFromDBData();
                                } else {
                                    Toast.makeText(ListPerformanceActivity.this, "삭제 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (update) {
           setAdapterFromDBData();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //cursor 사용 종료
        if (cursor != null) cursor.close();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShowBookmarkMap:
                intent = new Intent(ListPerformanceActivity.this, MyMapActivity.class);
                if (performanceList != null) {
                    intent.putExtra("performanceList", performanceList);
                }
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    String performanceTitle = data.getStringExtra("performanceTitle");
                    Toast.makeText(this, performanceTitle + " 수정했습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(this, "수정 취소하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    protected void setAdapterFromDBData() {
        //db에서 데이터를 읽어와 adapter에 설정
        db = helper.getReadableDatabase();
        cursor = db.rawQuery("select * from " + PerformanceDBHelper.TABLE_NAME, null);

        performanceList = new ArrayList<>();
        Performance p = null;
        while (cursor.moveToNext()) {
            p = new Performance(
                    cursor.getLong(cursor.getColumnIndex(PerformanceDBHelper.COL_ID)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_VENUE)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_PERIOD)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_MEMO)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_IMGPATH)));
            performanceList.add(p);
        }

        adapter.changeCursor(cursor);
        helper.close();
    }

    public Performance pickPerformance(long _id) {
        db = helper.getReadableDatabase();
        cursor = db.rawQuery("select * from " + PerformanceDBHelper.TABLE_NAME + " where _id=" + _id, null);

        Performance performance = null;
        while (cursor.moveToNext()) {
            performance = new Performance( _id,
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_VENUE)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_PERIOD)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_MEMO)),
                    cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_IMGPATH)));
        }

        cursor.close();
        helper.close();
        return performance;
    }

    public boolean removePerformance(long _id) {
        db = helper.getWritableDatabase();
        String whereClause = PerformanceDBHelper.COL_ID + "=?";
        String[] whereArgs = new String[] { String.valueOf(_id) };
        int result = db.delete(PerformanceDBHelper.TABLE_NAME, whereClause, whereArgs);
        helper.close();
        if (result > 0) return true;
        return false;
    }
}