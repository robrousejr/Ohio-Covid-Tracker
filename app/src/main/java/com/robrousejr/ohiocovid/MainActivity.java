package com.robrousejr.ohiocovid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWebsite();
            }
        });
    }

    public void getWebsite() {
        Log.i("Info", "getWebsite()");

        new Thread(new Runnable() {
            @Override
            public void run() {

                final StringBuilder builder = new StringBuilder();

                try {
                   Document doc = Jsoup.connect("https://covidtracking.com/").get();
                   String title = doc.title();
                   builder.append(title);
                } catch (IOException e) {
                    Log.e("Error", e.getMessage());
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Info", builder.toString());
                    }
                });
            }

        }).start();
    }

}