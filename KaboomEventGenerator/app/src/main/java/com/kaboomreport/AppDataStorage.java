package com.kaboomreport;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
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
import java.util.UUID;

class AppDataStorage {
    private static class SavedAppData {
        String version;
        String userId;
    }

    private static final String AppDataFileName = "kaboom-appdata";
    private static final String AppDataVersion = "1.0";

    public AppDataStorage() {
    }

    public AppData getAppData(Context context) {
        String fileName = AppDataFileName + AppDataVersion + ".json";
        File file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) {
            return null;
        }

        Gson gson = new Gson();
        InputStream stream = null;
        BufferedReader reader = null;
        try {
            FileInputStream input = context.openFileInput(fileName);
            stream = new BufferedInputStream(input);
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString();
            SavedAppData savedData = gson.fromJson(json, SavedAppData.class);
            if (savedData != null) {
                return new AppData(UUID.fromString(savedData.userId));
            } else {
                // File was empty
                return null;
            }
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

        // Was never saved
        return null;
    }

    public void saveAppData(AppData appData, Context context) {
        String fileName = AppDataFileName + AppDataVersion + ".json";

        SavedAppData savedData = new SavedAppData();
        savedData.version = AppDataVersion;
        savedData.userId = appData.getUserId().toString();

        Gson gson = new Gson();
        String json = gson.toJson(savedData, SavedAppData.class);

        OutputStream stream = null;
        OutputStreamWriter writer = null;
        try {
            FileOutputStream output = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            stream = new BufferedOutputStream(output);
            writer = new OutputStreamWriter(stream);
            writer.write(json);
            stream.flush();
        } catch (IOException e) {
            // Ignore
            Log.e("com.kaboomreport", "Something failed badly. Please report this issue", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            }
            catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing writer", e);
            }

            try {
                if (stream != null) {
                    stream.close();
                }
            }
            catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing stream", e);
            }
        }
    }
}
