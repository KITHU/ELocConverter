package james.kithu2.com.elocconverter;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;




public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    private static final String TAG = "Test2";

    //================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        try
        {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        catch(Exception e)
        {
            Log.e(TAG, getResources().getString(R.string.unable_to_obtain_map) + e.getMessage());
        }
    }

    //================================================================

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
    public void onMapReady(GoogleMap googleMap)
    {
        Log.d(TAG, "onMapReady()");

        // enable the +/- zoom buttons on the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            try
            {
                double[] dCoords = bundle.getDoubleArray("COORDS");
                LatLng ll = new LatLng(dCoords[0], dCoords[1]);

                MarkerOptions mo = new MarkerOptions();
                mo.position(ll);

                googleMap.addMarker(mo);
                googleMap.setInfoWindowAdapter(new InfoAdapter(getLayoutInflater(), dCoords));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dCoords[0], dCoords[1]), 14));
            }
            catch(Exception e)
            {
                Log.e(TAG, getResources().getString(R.string.unable_to_add_coordinates) + e.getMessage());
            }
        }
    }

    //================================================================
    //================================================================

    class InfoAdapter implements GoogleMap.InfoWindowAdapter
    {
        private LayoutInflater m_inflater = null;
        private View m_popup = null;
        private double[] m_dCoords;

        //============================================================

        public InfoAdapter( LayoutInflater inflater, double[] dCoords )
        {
            m_inflater = inflater;
            m_dCoords  = dCoords;
        }

        //============================================================

        @Override
        public View getInfoWindow( Marker marker )
        {
            return null;
        }

        //============================================================

        @Override
        public View getInfoContents( Marker marker )
        {
            if (m_popup == null)
                m_popup = m_inflater.inflate(R.layout.map_popup, null);

            TextView tv = (TextView) m_popup.findViewById(R.id.snippet1);
            tv.setText("Latitude: " + m_dCoords[0]);

            tv = (TextView) m_popup.findViewById(R.id.snippet2);
            tv.setText("Longitude: " + m_dCoords[1]);

            return m_popup;
        }
    }
}

