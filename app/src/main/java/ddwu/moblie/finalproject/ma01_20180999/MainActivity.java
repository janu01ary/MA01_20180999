package ddwu.moblie.finalproject.ma01_20180999;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kakao.sdk.common.KakaoSdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    final static int PERMISSION_REQ_CODE = 100;

    private ListView lvPerformanceAll = null;
    private String apiAddress;
    private PerformanceDBHelper helper;

    private PerformanceAdapter adapter;
    private ArrayList<Performance> resultList;
    private Intent intent = null;

    private ProgressBar progressBar;
    private boolean lastItemVisibleFlag = false;    //리스트 스크롤이 최하단으로 이동했는지 체크
    private boolean mLockListView = false;          //데이터를 불러올 때 중복되지 않기위한 변수
    private int pageNo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KakaoSdk.init(this, getString(R.string.kakao_app_key));

        intent = getIntent();
        if (intent.getAction() == Intent.ACTION_VIEW) {
            Performance p = new Performance();
            Uri uri = intent.getData();
            p.setTitle(uri.getQueryParameter("title"));
            p.setVenue(uri.getQueryParameter("venue"));
            p.setPeriod(uri.getQueryParameter("period"));

            Intent viewIntent = new Intent(this, UpdatePerformanceActivity.class);
            viewIntent.putExtra("performance", p);
            viewIntent.putExtra("type", "view");
            startActivity(viewIntent);
        }

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        lvPerformanceAll = (ListView) findViewById(R.id.lvPerformanceAll);
        helper = new PerformanceDBHelper(this);
        resultList = new ArrayList<Performance>();
        adapter = new PerformanceAdapter(this, R.layout.listview_layout, resultList);
        lvPerformanceAll.setAdapter(adapter);

        apiAddress = getResources().getString(R.string.kopis_xml_url);

        //리스트뷰 클릭 - 장소를 볼 수 있음
        lvPerformanceAll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long _id) {
                final int pos = position;
                Performance performance = resultList.get(pos);

                intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("performance", performance);
                startActivity(intent);
            }
        });

        //리스트뷰 롱클릭 - 즐겨찾기 등록
        lvPerformanceAll.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long _id) {
                final int pos = position;
                Performance performance = resultList.get(pos);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(performance.getTitle())
                        .setMessage("북마크에 추가하시겠습니까?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (addPerformance(performance)) {
                                    Toast.makeText(MainActivity.this, performance.getTitle() + " 추가했습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "추가 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
                return true;
            }
        });

        //화면이 바닥에 닿으면 데이터를 더 불러옴
        lvPerformanceAll.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag && mLockListView == false) {
                    progressBar.setVisibility(View.VISIBLE);
                    getMoreList();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShowPerformanceMap:
                Log.d("ASDF", "버튼 누름 onClick");
                intent = new Intent(this, MyMapActivity.class);
                intent.putExtra("performanceList", resultList);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuShowBookmark:
                intent = new Intent(this, ListPerformanceActivity.class);
                break;
        }
        if (intent != null) startActivity(intent);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        resultList.clear();
        Log.d("ASDF", "onResume1");

        if (isOnline()) {
//            new NetworkAsyncTask().execute(apiAddress);
//            adapter.notifyDataSetChanged();
            pageNo = 1;
            Log.d("ASDF", "onResume2");
            getMoreList();
        } else {
            Toast.makeText(MainActivity.this, "네트워크를 사용가능하게 설정해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    public void getMoreList() {
        //리스트에 다음 데이터를 입력할 동안에 이 메소드가 또 호출되지 않도록
        mLockListView = true;

        new NetworkAsyncTask().execute(apiAddress + "&pageNo=" + pageNo);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pageNo++;
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                mLockListView = false;
            }
        }, 500);
    }

    class NetworkAsyncTask extends AsyncTask<String, Void, String> {

        final static String NETWORK_ERR_MSG = "Server Error!";
        public final static String TAG = "NetworkAsyncTask";
        ProgressDialog progressDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(MainActivity.this, "Wait", "Downloading...");     // 진행상황 다이얼로그 출력
        }

        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            String result = downloadContents(address);
            if (result == null) {
                cancel(true);
                return NETWORK_ERR_MSG;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDlg.dismiss();  // 진행상황 다이얼로그 종료

//           parser 생성 및 OpenAPI 수신 결과를 사용하여 parsing 수행
            KopisXmlParser parser = new KopisXmlParser();
            resultList.addAll(parser.parse(result));

            if (resultList == null) {       // 올바른 결과를 수신하지 못하였을 경우 안내
                Toast.makeText(MainActivity.this, "올바른 결과를 수신하지 못했습니다.", Toast.LENGTH_SHORT).show();
            } else if (!resultList.isEmpty()) {
                adapter.setList(resultList);
            }
        }

        @Override
        protected void onCancelled(String msg) {
            super.onCancelled();
            progressDlg.dismiss();
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean addPerformance(Performance newPerformance) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(PerformanceDBHelper.COL_TITLE, newPerformance.getTitle());
        value.put(PerformanceDBHelper.COL_VENUE, newPerformance.getVenue());
        value.put(PerformanceDBHelper.COL_PERIOD, newPerformance.getPeriod());
        value.put(PerformanceDBHelper.COL_MEMO, newPerformance.getMemo());
        value.put(PerformanceDBHelper.COL_IMGPATH, newPerformance.getImgPath());

        long count = db.insert(PerformanceDBHelper.TABLE_NAME, null, value);
        helper.close();
        if (count > 0) return true;
        return false;
    }


    /* 이하 네트워크 접속을 위한 메소드 */ //manager이런거로 따로 빼야겠다?

    /* 네트워크 환경 조사 */
    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /* 주소(apiAddress)에 접속하여 문자열 데이터를 수신한 후 반환 */
    protected String downloadContents(String address) {
        HttpURLConnection conn = null;
        InputStream stream = null;
        String result = null;

        try {
            URL url = new URL(address);
            Log.d("ASDF", "url: " + url.toString());
            conn = (HttpURLConnection)url.openConnection();
            Log.d("ASDF", "conn: " + conn.toString());
            stream = getNetworkConnection(conn);
            Log.d("ASDF", "stream: " + stream.toString());
            result = readStreamToString(stream);
            if (stream != null) stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return result;
    }

    /* URLConnection 을 전달받아 연결정보 설정 후 연결, 연결 후 수신한 InputStream 반환 */
    private InputStream getNetworkConnection(HttpURLConnection conn) throws Exception {
        conn.setReadTimeout(3000);
        conn.setConnectTimeout(3000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + conn.getResponseCode());
        }

        return conn.getInputStream();
    }

    /* InputStream을 전달받아 문자열로 변환 후 반환 */
    protected String readStreamToString(InputStream stream){
        StringBuilder result = new StringBuilder();

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String readLine = bufferedReader.readLine();

            while (readLine != null) {
                result.append(readLine + "\n");
                readLine = bufferedReader.readLine();
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

}