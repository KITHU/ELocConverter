package james.kithu2.com.elocconverter;

import android.app.Dialog;
import android.os.Bundle;
import android.view.InflateException;
import android.view.View;
import android.webkit.WebView;

import android.app.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.InflateException;
import android.view.View;
import android.webkit.WebView;

import static java.security.AccessController.getContext;

/**
 * Created by JAMES on 8/24/2017.
 */

public class HowFragment extends DialogFragment
{
    public HowFragment()
    {
        super();
    }

    //================================================================

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View view;
        WebView wvHow;

        try
        {
            view = View.inflate(getContext(), R.layout.how_dialog, null);
            wvHow = (WebView) view.findViewById(R.id.wvHow);
        }
        catch(InflateException e)
        {
            view = wvHow = new WebView(getActivity());
        }

        wvHow.loadUrl("file:///android_asset/how.html");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getContext().getResources().getString(R.string.title_activity_main))
                .setIcon(android.R.drawable.ic_menu_help)
                .setPositiveButton("OK", null)
                .setView(view);

        return builder.create();
    }
}
