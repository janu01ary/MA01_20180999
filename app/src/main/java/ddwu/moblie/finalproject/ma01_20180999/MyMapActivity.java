package ddwu.moblie.finalproject.ma01_20180999;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//지도에 공연의 위치와 나의 위치를 함께 나타냄
public class MyMapActivity extends FragmentActivity implements OnMapReadyCallback {

    final static int PERMISSION_REQ_CODE = 100;

    private GoogleMap mMap;
    private Geocoder geocoder;
    private LocationManager locationManager;

    private Marker marker;
    private Marker currentMarker;
    private MarkerOptions options;
    private LatLng currentLoc;
    private int markerIdx = 0;

    private ArrayList<Performance> performanceList = null;
    private PerformanceDBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (checkPermission()) {
            mapFragment.getMapAsync(this);
        }

        geocoder = new Geocoder(MyMapActivity.this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        performanceList = (ArrayList) getIntent().getStringArrayListExtra("performanceList");
        helper = new PerformanceDBHelper(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);

        checkPermission();
        locationUpdate();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(locationButtonClickListener);
        mMap.setOnMarkerClickListener(markerClickListener);
        mMap.setOnInfoWindowLongClickListener(infoWindowLongClickListener);

        options = new MarkerOptions();
        for (Performance p : performanceList) {
            try {
                addMarkerToMap(p);
            } catch (AddressNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMarkerToMap(Performance p) throws AddressNotFoundException {
        List<Address> addressList = null;
        LatLng venueLatlng = null;
        try {
            addressList = geocoder.getFromLocationName(p.getVenue(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList != null) {
            Address venueAddress = null;
            try {
                venueAddress = addressList.get(0);
                double lat = venueAddress.getLatitude();
                double lng = venueAddress.getLongitude();
                venueLatlng = new LatLng(lat, lng);
                Log.d("ASDF", "addressList != null " + markerIdx);
            } catch (Exception e) {
                throw new AddressNotFoundException(p.getTitle() + "의 장소인 " + p.getVenue() + "를 찾지 못했습니다.", MyMapActivity.this);
            }
        }

        if (venueLatlng != null) {
            options = new MarkerOptions();
            options.position(venueLatlng)
                    .title(p.getVenue())
                    .snippet(p.getTitle() + "\n" + p.getPeriod())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

            marker = mMap.addMarker(options);
            marker.setTag(markerIdx++);
        }
    }

    private void locationUpdate() {
        if (checkPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3000, 0, locationListener);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 14));

            locationManager.removeUpdates(locationListener);

            if (currentMarker != null) {
                currentMarker.remove();
            }

            options = new MarkerOptions();
            options.position(currentLoc)
                    .title("내 위치");
            currentMarker = mMap.addMarker(options);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    GoogleMap.OnMyLocationButtonClickListener locationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            Toast.makeText(MyMapActivity.this, "내 현재 위치입니다.", Toast.LENGTH_SHORT).show();
            locationUpdate();
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));
            return true;
        }
    };

    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            //마커 윈도우 표시
            marker.showInfoWindow();
            return true;
        }
    };

    GoogleMap.OnInfoWindowLongClickListener infoWindowLongClickListener = new GoogleMap.OnInfoWindowLongClickListener() {
        @Override
        public void onInfoWindowLongClick(Marker marker) {
            //북마크에 추가하는 다이얼로그 표시...? id 어케 받아옴
            Performance performance = performanceList.get((Integer) marker.getTag());

            AlertDialog.Builder builder = new AlertDialog.Builder(MyMapActivity.this);
            builder.setTitle(performance.getTitle())
                    .setMessage("북마크에 추가하시겠습니까?")
                    .setNegativeButton("취소", null)
                    .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (addPerformance(performance)) {
                                Toast.makeText(MyMapActivity.this, performance.getTitle() + " 추가했습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MyMapActivity.this, "추가 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .show();
        }
    };

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

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //퍼미션을 획득하였을 경우 맵 로딩 실행
//                locationUpdate();
            } else {
                //퍼미션 미획득 시 액티비티 종료
                Toast.makeText(this, "앱 실행을 위해 권한 허용이 필요함", Toast.LENGTH_SHORT).show();
            }
        }
    }
}