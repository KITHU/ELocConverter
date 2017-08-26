package james.kithu2.com.elocconverter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;
import com.facebook.login.LoginManager;

import java.util.Locale;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.facebook.share.widget.JoinAppGroupDialog.show;

public class LocMainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final int PERMISSION_REQUEST_CODE = 1234;
    private static final String TAG = "Test2";

    private LocationManager lm           = null;
    private LocationListenerExt listener = null;

    // the DISPLAYED value is capped at six decimal places, so keep the ACTUAL value separate
    private double m_dLatitude  = Double.NaN;
    private double m_dLongitude = Double.NaN;

    private String m_strLatitudeDMS  = "";
    private String m_strLongitudeDMS = "";

    private ImageButton btnDown = null;
    private ImageButton btnUp   = null;

    private ProgressDialog dialog = null;


    //================================================================

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);

        if (! (Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        setContentView(R.layout.activity_loc_main);

        btnDown = (ImageButton) findViewById(R.id.btnDown);
        btnUp   = (ImageButton) findViewById(R.id.btnUp);

        btnDown.setOnClickListener(this);
        btnUp.setOnClickListener(this);
    }

    //================================================================

    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause()");

        // just in case a listener still happens to be active, remove it
        if (lm != null)
            lm.removeUpdates(listener);

        super.onPause();
    }

    //================================================================

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btnUp)
        {
            if (convertLatitudeToDecimal())
            {
                convertLongitudeToDecimal();
            }
        }
        else if (v.getId() == R.id.btnDown)
        {
            if (convertLatitudeToDMS())
            {
                convertLongitudeToDMS();
            }
        }
    }

    //================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {
        // most have two valid coordinates for the Map option to be available
        MenuItem menuMap = menu.findItem(R.id.action_map);
        menuMap.setEnabled(! (Double.isNaN(m_dLatitude) && Double.isNaN(m_dLongitude)));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_my_location)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            else
                startListener();

            return true;
        }
        else if (id == R.id.action_map)
        {
            double[] dCoords = new double[2];
            Intent intent = new Intent(this, MapsActivity.class);

            try
            {
                // latitude
                dCoords[0] = m_dLatitude;

                Spinner spinLatitudeDir = (Spinner) findViewById(R.id.spinLatitudeDir1);
                if (spinLatitudeDir.getSelectedItem().equals("S")) // south
                    dCoords[0] = -1.0 * dCoords[0];

                // longitude
                dCoords[1] = m_dLongitude;

                Spinner spinLongitudeDir = (Spinner) findViewById(R.id.spinLongitudeDir1);
                if (spinLongitudeDir.getSelectedItem().equals("W")) // west
                    dCoords[1] = -1.0 * dCoords[1];
            }
            catch(Exception e)
            {
                Log.e(TAG, "Settings coords to 0.0 because: " + e.getMessage());

                dCoords[0] = 0.0;
                dCoords[1] = 0.0;
            }
            finally
            {
                intent.putExtra("COORDS", dCoords);
                startActivity(intent);
            }

            return true;
        }
        else if (id == R.id.action_help)
        {
            HowFragment how = new HowFragment();
            how.show(getFragmentManager(), "how");

            return true;
        }
        else if (id == R.id.action_about)
        {
           Util.aboutDisplay(this,getString(R.string.title_activity_main),getString(R.string.about));

            return true;
        }
        else if (id == R.id.logout)
        {
            onLogout();
        }

        return super.onOptionsItemSelected(item);
    }

    //================================================================

    private void startListener()
    {
        Log.d(TAG, "startListener()");

        try
        {
            dialog = new ProgressDialog(this);
            dialog.setIndeterminate(true);
            dialog.setMessage("Obtaining current location...");
            dialog.show();
        }
        catch(Exception e)
        {
            Log.e(TAG, getResources().getString(R.string.unable_to_display_dialog) + e.getMessage());
        }

        try
        {
            listener = new LocationListenerExt();

            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm != null)
            {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
            }
        }
        catch(SecurityException se)
        {
            Log.e(TAG, getResources().getString(R.string.location_request_failed) + se.getMessage());
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    //================================================================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        Log.d(TAG, "onRequestPermissionsResult()");

        if (requestCode == PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startListener();
            else
                Log.e(TAG, getResources().getString(R.string.permission_not_granted));
        }
    }

    //================================================================

    private boolean convertLatitudeToDMS()
    {
        Log.d(TAG, "convertLatitudeToDMS()");
        boolean bResult = false;

        try
        {
            EditText etLatitude1 = (EditText) findViewById(R.id.etLatitude1);
            String strLatitude = etLatitude1.getText().toString();
            if (! strLatitude.isEmpty())
            {
                m_dLatitude = Double.parseDouble(strLatitude);
                if (Util.isValidLatitude(m_dLatitude))
                {
                    EditText etLatitude2 = (EditText) findViewById(R.id.etLatitude2);
                    m_strLatitudeDMS = Location.convert(m_dLatitude, Location.FORMAT_SECONDS);
                    etLatitude2.setText(m_strLatitudeDMS);

                    Spinner spinLatitudeDir1 = (Spinner) findViewById(R.id.spinLatitudeDir1);
                    Spinner spinLatitudeDir2 = (Spinner) findViewById(R.id.spinLatitudeDir2);
                    spinLatitudeDir2.setSelection(spinLatitudeDir1.getSelectedItemPosition());

                    bResult = true;
                }
                else
                    Util.displayMessage(this, "Incorrect latitude", getResources().getString(R.string.latitude_dec_format));
            }
            else
                Util.displayMessage(this, "Incorrect latitude", getResources().getString(R.string.latitude_dec_format));
        }
        catch(NumberFormatException nfe)
        {
            Log.e(TAG, getResources().getString(R.string.incorrect_latitude) + nfe.getMessage());
        }
        catch(Exception e)
        {
            Log.e(TAG, getResources().getString(R.string.error_converting_latitude_dms) + e.getMessage());
        }

        return bResult;
    }

    private boolean convertLongitudeToDMS()
    {
        Log.d(TAG, "convertLongitudeToDMS()");
        boolean bResult = false;

        try
        {
            EditText etLongitude1 = (EditText) findViewById(R.id.etLongitude1);
            String strLongitude = etLongitude1.getText().toString();
            if (! strLongitude.isEmpty())
            {
                m_dLongitude = Double.parseDouble(strLongitude);
                if (Util.isValidLongitude(m_dLongitude))
                {
                    EditText etLongitude2 = (EditText) findViewById(R.id.etLongitude2);
                    m_strLongitudeDMS = Location.convert(m_dLongitude, Location.FORMAT_SECONDS);
                    etLongitude2.setText(m_strLongitudeDMS);

                    Spinner spinLongitudeDir1 = (Spinner) findViewById(R.id.spinLongitudeDir1);
                    Spinner spinLongitudeDir2 = (Spinner) findViewById(R.id.spinLongitudeDir2);
                    spinLongitudeDir2.setSelection(spinLongitudeDir1.getSelectedItemPosition());

                    bResult = true;
                }
                else
                    Util.displayMessage(this, "Incorrect longitude", getResources().getString(R.string.longitude_dec_format));
            }
            else
                Util.displayMessage(this, "Incorrect longitude", getResources().getString(R.string.longitude_dec_format));
        }
        catch(NumberFormatException nfe)
        {
            Log.e(TAG, getResources().getString(R.string.incorrect_longitude) + nfe.getMessage());
        }
        catch(Exception e)
        {
            Log.e(TAG, getResources().getString(R.string.error_converting_longitude_dms) + e.getMessage());
        }

        return bResult;
    }

    //================================================================

    private boolean convertLatitudeToDecimal()
    {
        boolean bResult = false;

        try
        {
            EditText etLatitude2 = (EditText) findViewById(R.id.etLatitude2);
            m_strLatitudeDMS = etLatitude2.getText().toString();
            if (Util.isValidLatitude(m_strLatitudeDMS))
            {
                String[] strDMS = TextUtils.split(m_strLatitudeDMS, ":");

                int nDegrees    = Integer.valueOf(strDMS[0]);
                int nMinutes    = Integer.valueOf(strDMS[1]);
                double dSeconds = Double.valueOf(strDMS[2]);

                m_dLatitude = nDegrees + (nMinutes / 60.0) + (dSeconds / 3600.0);
                EditText etLatitude1 = (EditText) findViewById(R.id.etLatitude1);
                etLatitude1.setText(String.format(Locale.getDefault(), "%.6f", m_dLatitude));

                Spinner spinLatitudeDir1 = (Spinner) findViewById(R.id.spinLatitudeDir1);
                Spinner spinLatitudeDir2 = (Spinner) findViewById(R.id.spinLatitudeDir2);
                spinLatitudeDir1.setSelection(spinLatitudeDir2.getSelectedItemPosition());

                bResult = true;
            }
            else
                Util.displayMessage(this, "Incorrect latitude", getResources().getString(R.string.latitude_dms_format));
        }
        catch(Exception e)
        {
            Log.e(TAG, getResources().getString(R.string.error_converting_latitude_decimal) + e.getMessage());
        }

        return bResult;
    }

    private boolean convertLongitudeToDecimal()
    {
        boolean bResult = false;

        try
        {
            EditText etLongitude2 = (EditText) findViewById(R.id.etLongitude2);
            m_strLongitudeDMS = etLongitude2.getText().toString();
            if (Util.isValidLongitude(m_strLongitudeDMS))
            {
                String[] strDMS = TextUtils.split(m_strLongitudeDMS, ":");

                int nDegrees    = Integer.valueOf(strDMS[0]);
                int nMinutes    = Integer.valueOf(strDMS[1]);
                double dSeconds = Double.valueOf(strDMS[2]);

                m_dLongitude = nDegrees + (nMinutes / 60.0) + (dSeconds / 3600.0);
                EditText etLongitude1 = (EditText) findViewById(R.id.etLongitude1);
                etLongitude1.setText(String.format(Locale.getDefault(), "%.6f", m_dLongitude));

                Spinner spinLongitudeDir1 = (Spinner) findViewById(R.id.spinLongitudeDir1);
                Spinner spinLongitudeDir2 = (Spinner) findViewById(R.id.spinLongitudeDir2);
                spinLongitudeDir1.setSelection(spinLongitudeDir2.getSelectedItemPosition());

                bResult = true;
            }
            else
                Util.displayMessage(this, "Incorrect longitude", getResources().getString(R.string.longitude_dms_format));
        }
        catch(Exception e)
        {
            Log.e(TAG, getResources().getString(R.string.error_converting_longitude_decimal) + e.getMessage());
        }

        return bResult;
    }

    public void onLogout() {
        // logout of Account Kit
        AccountKit.logOut();
        //facebook logout
        LoginManager.getInstance().logOut();
        launchLoginActivity();
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(LocMainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    //================================================================
    //================================================================

    private class LocationListenerExt implements LocationListener
    {
        private final String[] mStatus = { "OUT_OF_SERVICE", "TEMPORARILY_UNAVAILABLE", "AVAILABLE" };
        private String m_strLastProvider = "";

        @Override
        public void onLocationChanged( Location location )
        {
            Log.d(TAG, "onLocationChanged()");

            if (dialog != null)
                dialog.cancel();

            // since the location could change frequently, only show the Toast when the provider changes
            if (location.getProvider() != m_strLastProvider)
            {
                m_strLastProvider = location.getProvider();
                Toast.makeText(LocMainActivity.this, "Location obtained from " + m_strLastProvider, Toast.LENGTH_SHORT).show();
            }

            // decimal
            EditText tvLatitude = (EditText) findViewById(R.id.etLatitude1);
            m_dLatitude = Math.abs(location.getLatitude());
            tvLatitude.setText(String.format(Locale.getDefault(), "%f", m_dLatitude));

            Spinner spinLatitude = (Spinner) findViewById(R.id.spinLatitudeDir1);
            spinLatitude.setSelection((location.getLatitude() < 0.0) ? 1 : 0);

            EditText tvLongitude = (EditText) findViewById(R.id.etLongitude1);
            m_dLongitude = Math.abs(location.getLongitude());
            tvLongitude.setText(String.format(Locale.getDefault(), "%f", m_dLongitude));

            Spinner spinLongitude = (Spinner) findViewById(R.id.spinLongitudeDir1);
            spinLongitude.setSelection((location.getLongitude() < 0.0) ? 0 : 1);

            // DMS
            tvLatitude = (EditText) findViewById(R.id.etLatitude2);
            tvLatitude.setText(String.format(Locale.getDefault(), "%s", Location.convert(Math.abs(location.getLatitude()), Location.FORMAT_SECONDS)));

            spinLatitude = (Spinner) findViewById(R.id.spinLatitudeDir2);
            spinLatitude.setSelection((location.getLatitude() < 0.0) ? 1 : 0);

            tvLongitude = (EditText) findViewById(R.id.etLongitude2);
            tvLongitude.setText(String.format(Locale.getDefault(), "%s", Location.convert(Math.abs(location.getLongitude()), Location.FORMAT_SECONDS)));

            spinLongitude = (Spinner) findViewById(R.id.spinLongitudeDir2);
            spinLongitude.setSelection((location.getLongitude() < 0.0) ? 0 : 1);

            // we don't need to continue listening once our location has been obtained
            lm.removeUpdates(listener);
        }

        @Override
        public void onStatusChanged( String provider, int status, Bundle extras )
        {
            Log.d(TAG, "Provider: " + provider + ", status = " + mStatus[status]);

            if (provider.equals(LocationManager.GPS_PROVIDER) && status == LocationProvider.AVAILABLE)
            {
                int numSatellites = extras.getInt("satellites", 0);
                Log.d("Test2", "  Satellites = " + numSatellites);
            }
        }

        @Override
        public void onProviderEnabled( String provider )
        {
            Log.d(TAG, "Enabled provider: " + provider);
        }

        @Override
        public void onProviderDisabled( String provider )
        {
            Log.d(TAG, "Disabled provider: " + provider);
        }


    }
}
