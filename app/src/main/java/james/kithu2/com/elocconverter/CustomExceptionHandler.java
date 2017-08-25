package james.kithu2.com.elocconverter;

import android.app.Activity;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by JAMES on 8/24/2017.
 */

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private static final String TAG = "Test2";

    private Thread.UncaughtExceptionHandler defaultHandler;
    private Activity activity;

    //================================================================

    public CustomExceptionHandler(Activity activity)
    {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.activity = activity;
    }

    //================================================================

    @Override
    public void uncaughtException( Thread t, Throwable e )
    {
        final Writer stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stacktrace = stringWriter.toString();
        printWriter.close();

        Log.e(TAG, stacktrace);
        Log.e(TAG, e.toString());

        // Chain to the normal uncaught exception handler
        defaultHandler.uncaughtException(t, e);
    }
}

