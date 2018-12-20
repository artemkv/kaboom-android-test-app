package com.kaboomreport;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

final class Orchestrator {
    private static final String SERVER_ULR = "http://192.168.1.5:8000/event";
    private static final String FILE_NAME = "last_exception.json";

    private static final AppDataStorage appDataStorage = new AppDataStorage();
    private static AppData appData = null;

    private Orchestrator() {
    }

    public static void reportLaunch(Context context) {
        ensureAppDataLoaded(context);

        OnSuccess<Boolean> onSuccess = new OnSuccess<Boolean>() {
            @Override
            public void call(Boolean result) {
                if (result == true) {
                    Log.d("com.kaboomreport", "Reported launch");
                    // Do nothing, everything went well
                } else {
                    Log.d("com.kaboomreport", "Could not report launch");
                    // Could not send the launch info
                    // There is no retry in this case - this is a current limitation
                }
            }
        };

        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
                Log.e("com.kaboomreport", "Something failed badly. Please report this issue", e);
            }
        };

        AsyncWorkerTask.Worker<Boolean> worker = new AsyncWorkerTask.Worker<Boolean>() {
            @Override
            public Boolean getResult() {
                String event = String.format(
                        "{\"t\":\"S\",\"u\":\"%s\",\"dt\":\"%s\"}",
                        appData.getUserId().toString(),
                        getDateTimeString());
                return HttpClient.trySend(SERVER_ULR, event);
            }
        };

        new AsyncWorkerTask<>(worker, onSuccess, onError).execute();
    }

    public static void saveCrashInfo(Throwable e, Context context) {
        ensureAppDataLoaded(context);

        // Get exception stack trace for details
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String details = Base64.encodeToString(stringWriter.toString().getBytes(), Base64.NO_WRAP);

        // Use the innermost exception message for message
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        // Construct event
        final String event = String.format(
                "{\"t\":\"C\",\"u\":\"%s\",\"dt\":\"%s\",\"m\":\"%s\",\"d\":\"%s\"}",
                appData.getUserId().toString(),
                getDateTimeString(),
                cause.getMessage(),
                details);

        saveCrashInfo(event, context);
    }

    public static void reportLastSavedCrash(Context context) {
        String event = loadCrashInfo(context);
        if (event != null) {
            reportCrash(event, context);
        }
    }

    private static void ensureAppDataLoaded(Context context) {
        if (appData == null) {
            appData = appDataStorage.getAppData(context);
            if (appData == null) {
                appData = new AppData();
                appDataStorage.saveAppData(appData, context);
            }
        }
    }

    private static void reportCrash(final String event, final Context context) {
        OnSuccess<Boolean> onSuccess = new OnSuccess<Boolean>() {
            @Override
            public void call(Boolean result) {
                if (result == true) {
                    Log.d("com.kaboomreport", "Reported crash");
                    // Now we've been able to send the report, no need to keep the crash
                    removeCrashFile(context);
                } else {
                    Log.d("com.kaboomreport", "Could not report crash");
                    // Could not send the crash, keep it to try to re-send later
                }
            }
        };

        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
                Log.e("com.kaboomreport", "Something failed badly. Please report this issue", e);
            }
        };

        AsyncWorkerTask.Worker<Boolean> worker = new AsyncWorkerTask.Worker<Boolean>() {
            @Override
            public Boolean getResult() {
                return HttpClient.trySend(SERVER_ULR, event);
            }
        };

        new AsyncWorkerTask<>(worker, onSuccess, onError).execute();
    }

    private static void saveCrashInfo(String event, Context context) {
        OutputStream stream = null;
        OutputStreamWriter writer = null;
        try {
            FileOutputStream output = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            stream = new BufferedOutputStream(output);
            writer = new OutputStreamWriter(stream);
            writer.write(event);
            stream.flush();
        } catch (IOException e) {
            // Ignore
            Log.e("com.kaboomreport", "Something failed badly. Please report this issue", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing writer", e);
            }

            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing stream", e);
            }
        }
    }

    private static String loadCrashInfo(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            // No crash to report
            return null;
        }

        InputStream stream = null;
        BufferedReader reader = null;
        try {
            stream = context.openFileInput(FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException e) {
            // Ignore
            Log.e("com.kaboomreport", "Something failed badly. Please report this issue", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing reader", e);
            }

            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing stream", e);
            }
        }

        return null;
    }

    private static void removeCrashFile(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    private static String getDateTimeString() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"); // ISO8601TimeZone
        return format.format(new Date());
    }
}
