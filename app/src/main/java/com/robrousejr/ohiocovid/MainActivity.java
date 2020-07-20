package com.robrousejr.ohiocovid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Text;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    final String url = "https://covidtracking.com/data/state/ohio"; // Ohio URL

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWebsite();
    }

    public void getWebsite() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                final StringBuilder builder = new StringBuilder();

                try {
                   Document doc = Jsoup.connect(url).get(); // Connect to site

                   // Get total cases for Ohio
                   Element totalCasesElement = doc.select("tbody tr td").first();
                   String totalCases = totalCasesElement.text();
                   builder.append(totalCases);
                } catch (IOException e) {
                    Log.e("Error", e.getMessage());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Info", builder.toString());
                        TextView output = (TextView) findViewById(R.id.output);
                        output.setText(builder.toString());
                    }
                });
            }

        }).start();
    }

}