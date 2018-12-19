package com.kaboomreport;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

final class HttpClient {
    private static final int CONNECTION_TIMEOUT = 1000; // 1 second
    private static final int READ_TIMEOUT = 5000; // 5 seconds

    private HttpClient() {}

    public static boolean trySend(String url, String json) {
        HttpURLConnection connection = null;
        OutputStream outStream = null;
        InputStream inStream = null;
        InputStream errorStream = null;
        BufferedReader reader = null;
        try {
            byte[] data = json.getBytes("UTF-8");

            URL serverUrl = new URL(url);
            connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            connection.setRequestProperty("Authorization", "Token 1af538baa9045a84c0e889f672baf83ff24"); // TODO:

            connection.setDoInput(true);

            // Write body
            outStream = connection.getOutputStream();
            outStream.write(data);
            outStream.flush();

            // Read response
            InputStream resultStream = null;
            errorStream = connection.getErrorStream();
            if (errorStream != null) {
                resultStream = errorStream;
            } else {
                resultStream = connection.getInputStream();
            }
            reader = new BufferedReader(new InputStreamReader(resultStream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("com.kaboomreport", line);
            }
        } catch (IOException e) {
            Log.d("com.kaboomreport", e.getMessage(), e);
            return false;
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing reader", e);
            }

            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing output stream", e);
            }

            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                // Ignore
                Log.e("com.kaboomreport", "Error closing input stream", e);
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        return true;
    }
}
