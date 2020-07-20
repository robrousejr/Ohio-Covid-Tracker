package com.robrousejr.ohiocovid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final String url = "https://covidtracking.com/data/state/ohio"; // Ohio URL
    final String filename = "info";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String date = "";
        String cases = "";
        boolean isScrape = false; // Should we scrape again or not

        // Read date/cases from file
        try {
            FileInputStream inputStream = openFileInput(filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int i = 0;
            while ((line = r.readLine()) != null) {
                if(i == 0)
                    date = line; // Date on first line
                else if (i == 1)
                    cases = line; // Number of cases on second line
                ++i;
            }
            if (date.isEmpty() || cases.isEmpty())
                throw new Exception("Issue getting date or cases");
            Log.i("Info", "File Contents; Date " + date);
            Log.i("Info", "File Contents; Cases " + cases);
            r.close();
            inputStream.close();
        } catch (Exception e) { // No data saved, scrape data
            Log.e("Error", "Issue getting date or cases");
            Log.e("Error", e.getMessage());
            isScrape = true;
        }

        if(isScrape) {
            getWebsite();
        } else {
            Log.i("Info", "Setting text from previously found cases: " + cases);
            final String finalCases = cases;
            TextView txtView = (TextView) findViewById(R.id.output);
            txtView.setText(cases);
        }

    }

    /**
     * Scrapes website information regarding total COVID cases for Ohio
     * */
    public void getWebsite() {
        Log.i("Info", "getWebsite()");
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

                try {
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String output = formatter.format(new Date()) + "\n" + builder.toString();
                    FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(output.getBytes());
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    Log.e("Error", e.getMessage());
                    Log.e("Error", "File not found");
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Error", e.getMessage());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView output = (TextView) findViewById(R.id.output);
                        output.setText(builder.toString());
                    }
                });
            }

        }).start();
    }

}