package com.robrousejr.ohiocovid;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final String url = "https://covidtracking.com/data/state/ohio"; // Ohio URL
    final String filename = "info";

    List<DateCases> dateCases = new ArrayList<DateCases>(); // All dates and case numbers

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String lastRunDate = "";
        String cases = "";
        Date dateObj = null;
        boolean isScrape = false; // Should we scrape again or not

        // Read date/cases from file
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            FileInputStream inputStream = openFileInput(filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int i = 0;
            while ((line = r.readLine()) != null) {
                if(i == 0)
                    lastRunDate = line; // Date on first line
                else if (i == 1)
                    cases = line; // Number of cases on second line
                else if (i > 1){
                    String[] dateLine = line.split("\t");
                    DateCases dc = new DateCases(formatter.parse(dateLine[0]), Integer.parseInt(dateLine[1].trim()));
                    dateCases.add(dc);
                }

                ++i;
            }
            if (lastRunDate.isEmpty() || cases.isEmpty())
                throw new Exception("Issue getting date or cases");
            dateObj = formatter.parse(lastRunDate);
            r.close();
            inputStream.close();
        } catch (Exception e) { // No data saved, scrape data
            Log.e("Error", "Issue getting date or cases");
            Log.e("Error", e.getMessage());
            isScrape = true;
        }

        // Logging
        Log.i("Last Scraping Date: ", lastRunDate);
        Log.i("Saved Covid Case #", cases);

        // Scraping needed
        if(isScrape || !compareDateDays(dateObj, new Date())) {
            getWebsite();
        } else {
            showCases(cases);
            showChart();
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
                    dateCases.clear();

                   // Print out new cases from table
                    Elements tableRows = doc.select(".state-history-table tr");

                    // Iterate through rows and add all dateCases
                    for(Element e : tableRows) {
                        Elements tableCells = e.getElementsByTag("td");
                        String[] date = tableCells.get(0).text().split(" ");
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat formatter = new SimpleDateFormat("MMM"); // 3-letter month name

                        String month = String.format("%02d", formatter.parse(date[1]).getMonth() + 1); // 2 number month
                        String dayOfMonth = String.format("%02d", Integer.parseInt(date[2])); // 2 number day
                        String year = date[3];
                        Date finalDate = parseDate(year + "-" + month + "-" + dayOfMonth); // Final date
                        DateCases dc = new DateCases(finalDate, NumberFormat.getNumberInstance(java.util.Locale.US).parse(tableCells.get(3).text()).intValue());
                        dateCases.add(dc);
                    }

                    for (DateCases d : dateCases) {
                        Log.i("DateCase: ", d.toString());
                    }
                } catch (IOException | ParseException e) {
                    Log.e("Error", e.getMessage());
                }

                try {
                    // Todo: Save dateCases

                    // Save info to file
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String output = formatter.format(new Date()) + "\n" + builder.toString() + "\n";

                    for(DateCases c : dateCases){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        output += sdf.format(c.getDate()) + "\t" + c.getCases() + "\n";
                    }

                    FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(output.getBytes());
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    Log.e("Error", "File not found");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("Error", "IOException Error");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showCases(builder.toString());
                        showChart();
                    }
                });
            }

        }).start();
    }

    // Todo: Change display of graph
    public void showChart() {

        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // Sets value formatter
        chart.getXAxis().setValueFormatter(new XAxisDateFormatter());

        // Chart settings
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.getDescription().setEnabled(false);
        ArrayList<Entry> yValues = new ArrayList<>();

        // Sort by increasing order
        Collections.reverse(dateCases);

        for (DateCases dc : dateCases) {
            Entry newEntry = new Entry((float) dc.getDate().getTime() / 1000, (float) dc.getCases(), dc);
            yValues.add(newEntry);
        }

        Collections.sort(yValues, new EntryXComparator());

        LineDataSet ySet = new LineDataSet(yValues, "Number of Cases");
        ySet.setLineWidth(5f);
        ySet.setDrawValues(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(ySet);
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    public void showCases(String cases) {
        TextView output = (TextView) findViewById(R.id.output);
        output.setText(cases);
        output.setX(-2000);
        output.animate().translationXBy(2000).setDuration(2000);
    }

    /**
     * Compares two dates to see if they're on the same day of a month
     */
    public boolean compareDateDays(Date dOne, Date dTwo) {
        return dOne.getDate() == dTwo.getDate();
    }

    public Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }


}