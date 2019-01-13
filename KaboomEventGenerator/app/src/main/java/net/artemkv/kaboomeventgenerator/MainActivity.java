package net.artemkv.kaboomeventgenerator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kaboomreport.KaboomClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KaboomClient.configure("4398759834759"); // Use your app code
        KaboomClient.reportLaunch(this);
        KaboomClient.reportLastSavedCrash(this);
    }

    public void onGenericExceptionButtonClick(View view) {
        throw new IllegalStateException("Hello Exception!");
    }
}
