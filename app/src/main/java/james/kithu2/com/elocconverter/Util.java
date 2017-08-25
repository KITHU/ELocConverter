package james.kithu2.com.elocconverter;

import android.content.Context;
import android.text.TextUtils;
import android.support.v7.app.AlertDialog;

/**
 * Created by JAMES on 8/24/2017.
 */

public class Util
{
    public static void displayMessage(Context context, String strTitle, String strMessage )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setTitle(strTitle);
        builder.setMessage(strMessage);

        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //================================================================

    public static boolean isValidLatitude( double dLatitude )
    {
        return dLatitude >= 0.0 && dLatitude <= 90.0;
    }

    public static boolean isValidLatitude( String strLatitude )
    {
        // this handles the format but doesn't check the ranges
        // strLatitude.matches("[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}(\\.[0-9]+)?");

        String[] lat = TextUtils.split(strLatitude, ":");

        if (lat.length == 3)
        {
            double dDegrees = Double.parseDouble(lat[0]);
            double dMinutes = Double.parseDouble(lat[1]);
            double dSeconds = Double.parseDouble(lat[2]);

            return (dDegrees >= 0.0 && dDegrees <= 90.0) &&
                    (dMinutes >= 0.0 && dMinutes < 60.0) &&
                    (dSeconds >= 0.0 && dSeconds < 60.0);
        }

        return false;
    }

    //================================================================

    public static boolean isValidLongitude( double dLongitude )
    {
        return dLongitude >= 0.0 && dLongitude <= 180.0;
    }

    public static boolean isValidLongitude( String strLongitude )
    {
        // this handles the format but doesn't check the ranges
        // strLongitude.matches("[0-9]{1,3}:[0-9]{1,2}:[0-9]{1,2}(\\.[0-9]+)?");

        String[] lng = TextUtils.split(strLongitude, ":");

        if (lng.length == 3)
        {
            double dDegrees = Double.parseDouble(lng[0]);
            double dMinutes = Double.parseDouble(lng[1]);
            double dSeconds = Double.parseDouble(lng[2]);

            return (dDegrees >= 0.0 && dDegrees <= 180.0) &&
                    (dMinutes >= 0.0 && dMinutes < 60.0) &&
                    (dSeconds >= 0.0 && dSeconds < 60.0);
        }

        return false;
    }
    public static void aboutDisplay(Context context, String strTitle, String strMessage )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(strTitle);
        builder.setMessage(strMessage);

        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
