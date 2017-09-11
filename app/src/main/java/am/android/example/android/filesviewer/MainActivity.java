package am.android.example.android.filesviewer;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;

public class MainActivity extends Activity {
    public MainActivity() {
        super();
        setupStrictMode();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            getFragmentManager().
                    beginTransaction().add(android.R.id.content,
                    new FinderListFragment()).commit();
        }
    }

    private void setupStrictMode() {
        StrictMode.ThreadPolicy.Builder builder =
                new StrictMode.ThreadPolicy.Builder().
                        detectAll().penaltyLog();
        if (BuildConfig.DEBUG) {
            builder.penaltyFlashScreen();
        }
        StrictMode.setThreadPolicy(builder.build());
    }
}
