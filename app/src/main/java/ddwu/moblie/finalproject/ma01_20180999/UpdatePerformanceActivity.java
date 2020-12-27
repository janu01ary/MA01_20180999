package ddwu.moblie.finalproject.ma01_20180999;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.message.template.TemplateParams;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdatePerformanceActivity extends AppCompatActivity {

    final static int PERMISSION_REQ_CODE = 100;
    private static final int REQUEST_TAKE_PHOTO = 200;

    private TextView tvTitle;
    private TextView tvVenue;
    private TextView tvPeriod;
    private EditText etMemo;
    private ImageView ivPerformance;
    private Button btnUpdate;
    private String type;

    private String mCurrentPhotoPath;
    private String tmpPath;

    private Performance performance;
    private PerformanceDBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_performance);

        performance = (Performance) getIntent().getSerializableExtra("performance");
        helper = new PerformanceDBHelper(this);

        tvTitle = findViewById(R.id.tvUpdatePerformanceTitle);
        tvTitle.setText(performance.getTitle());

        tvVenue = findViewById(R.id.tvUpdatePerformanceVenue);
        tvVenue.setText(performance.getVenue());

        tvPeriod = findViewById(R.id.tvUpdatePerformancePeriod);
        tvPeriod.setText(performance.getPeriod());

        etMemo = findViewById(R.id.etUpdatePerformanceMemo);

        ivPerformance = findViewById(R.id.ivPerformance);

        type = getIntent().getStringExtra("type");
        Log.d("ASDF", "intent에 type: " + type);
        if (type.equals("view")) {
            btnUpdate = findViewById(R.id.btnUpdate);
            btnUpdate.setText("북마크에 추가");

            TextView tvMemo = findViewById(R.id.tvMemo);
            tvMemo.setVisibility(View.INVISIBLE);
            etMemo.setVisibility(View.INVISIBLE);

            Button btnNewPic = findViewById(R.id.btnNewPic);
            btnNewPic.setVisibility(View.INVISIBLE);
            ivPerformance.setVisibility(View.INVISIBLE);
        } else {
            etMemo.setText(performance.getMemo());

            Bitmap bitmap = BitmapFactory.decodeFile(performance.getImgPath());
            ivPerformance.setImageBitmap(bitmap);
        }
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnViewMap:
                Intent intent = new Intent(UpdatePerformanceActivity.this, MapsActivity.class);
                intent.putExtra("performance", performance);
                startActivity(intent);
                break;
            case R.id.btnNewPic:
                if (checkPermission()) {
                    dispatchTakePictureIntent();
                }
                break;
            case R.id.btnUpdate:
                if (type.equals("view")) {
                    if (addPerformance(performance)) {
                        Toast.makeText(this, "북마크에 추가했습니다.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

                performance.setMemo(etMemo.getText().toString());
                performance.setImgPath(tmpPath);

                if (updatePerformance(performance)) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("performanceTitle", performance.getTitle());
                    setResult(RESULT_OK, resultIntent);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
                break;
            case R.id.btnUpdateCancel:
                finish();
                break;
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

    public boolean updatePerformance(Performance performance) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(PerformanceDBHelper.COL_MEMO, performance.getMemo());
        value.put(PerformanceDBHelper.COL_IMGPATH, performance.getImgPath());

        String whereClause = PerformanceDBHelper.COL_ID + "=?";
        String[] whereArgs = new String[] { String.valueOf(performance.get_id()) };
        int result = db.update(PerformanceDBHelper.TABLE_NAME, value, whereClause, whereArgs);
        helper.close();
        if (result > 0) return true;
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_performance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuShareKakaoTalk:
                TemplateParams params = LocationTemplate.newBuilder(
                        performance.getVenue(),
                        ContentObject.newBuilder(
                                performance.getTitle(),
                                "https://postfiles.pstatic.net/MjAyMDEyMTlfMTUw/MDAxNjA4MzIzMTg1OTk3.sVZkR4yVBOFV-Z4Gk_HdkRDNeiw9kw142hBBQ7hOLScg.0ZVaSAk-JNpz3oAVGpRYBocENFI7jCKjxj5ffVSKhpog.PNG.332cccbb/img-performance.png?type=w966",
                                LinkObject.newBuilder()
                                        .setWebUrl("https://developers.kakao.com")
                                        .setMobileWebUrl("https://developers.kakao.com")
                                        .setAndroidExecutionParams("title=" + performance.getTitle() + "&venue=" + performance.getVenue() + "&period=" + performance.getPeriod())
                                        .build())
                                .setDescrption("장소: " + performance.getVenue() + "\n기간: " + performance.getPeriod())
                                .build())
                        .setAddressTitle("")
                        .addButton(new ButtonObject(
                                "앱에서 보기",
                                LinkObject.newBuilder()
                                        .setWebUrl("https://developers.kakao.com")
                                        .setMobileWebUrl("https://developers.kakao.com")
                                        .setAndroidExecutionParams("title=" + performance.getTitle() + "&venue=" + performance.getVenue() + "&period=" + performance.getPeriod())
                                        .build()))
                        .build();

                KakaoLinkService.getInstance().sendDefault(
                        this, params, new ResponseCallback<KakaoLinkResponse>() {
                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "카카오링크 공유 실패: " + errorResult);
//                        Toast.makeText(UpdatePerformanceActivity.this, "카카오링크 공유 실패: " + errorResult, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(KakaoLinkResponse result) {
                        Log.i("KAKAO_API", "카카오링크 공유 성공");
                        Toast.makeText(UpdatePerformanceActivity.this, "카카오링크 공유 성공", Toast.LENGTH_SHORT).show();
                        // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                        Log.w("KAKAO_API", "warning messages: " + result.getWarningMsg());
                        Log.w("KAKAO_API", "argument messages: " + result.getArgumentMsg());
//                        Toast.makeText(UpdatePerformanceActivity.this, "Warning messages: " + result.getWarningMsg() + " / Argument message: " + result.getArgumentMsg(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
       }
        return true;
    }

    /*원본 사진 파일 저장*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
                tmpPath = photoFile.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                try {
                    Uri photoUri = FileProvider.getUriForFile(this,
                            "ddwu.moblie.finalproject.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    galleryAddPic();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void galleryAddPic() {
        Log.d("ASDF", "galleryAddPic");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /*사진의 크기를 ImageView에서 표시할 수 있는 크기로 변경*/
    private void setPic() {
        // Get the dimensions of the View
        int targetW = ivPerformance.getWidth();
        int targetH = ivPerformance.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ivPerformance.setImageBitmap(bitmap);
    }

    /*현재 시간 정보를 사용하여 파일 정보 생성*/
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
        }
    }
}