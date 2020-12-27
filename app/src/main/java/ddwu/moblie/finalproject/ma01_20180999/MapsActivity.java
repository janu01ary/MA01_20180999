package ddwu.moblie.finalproject.ma01_20180999;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
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
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder geocoder;

    private Marker marker;

    private Performance performance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(MapsActivity.this);
        performance = (Performance) getIntent().getSerializableExtra("performance");

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            addMarkerToMap(performance);
        } catch (AddressNotFoundException e) {
            e.printStackTrace();
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
            } catch (Exception e) {
                throw new AddressNotFoundException(p.getTitle() + "의 장소인 " + p.getVenue() + "를 찾지 못했습니다.", MapsActivity.this);
            }
        }

        if (venueLatlng != null) {
            MarkerOptions options = new MarkerOptions();
            options.position(venueLatlng)
                    .title(p.getVenue())
                    .snippet(p.getTitle() + "\n" + p.getPeriod())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

            marker = mMap.addMarker(options);
            marker.showInfoWindow();

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(venueLatlng, 14));
        }
    }
}