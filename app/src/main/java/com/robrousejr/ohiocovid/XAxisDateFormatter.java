package com.robrousejr.ohiocovid;

import android.util.Log;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class XAxisDateFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        Date date = new Date((long) value * 1000);
        Log.i("NEWDATE", date.toString());
        SimpleDateFormat fmt = new SimpleDateFormat("MMM dd");
        return fmt.format(date);
    }
}