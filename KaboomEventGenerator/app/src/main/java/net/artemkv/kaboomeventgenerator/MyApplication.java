package net.artemkv.kaboomeventgenerator;

import android.app.Application;
import android.util.Log;
import com.kaboomreport.KaboomClient;

public class MyApplication extends Application {
    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Don't forget to add to manifest: android:name=".MyApplication"
        // Save the default exception handler
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thread, Throwable e) {
                        handleUncaughtException(thread, e);
                    }
                });
    }

    private void handleUncaughtException(Thread thread, Throwable e) {
        try {
            KaboomClient.saveCrashInfo(e, this);
        } finally {
            // Re-throw original exception
            defaultUncaughtExceptionHandler.uncaughtException(thread, e);
        }
    }
}
