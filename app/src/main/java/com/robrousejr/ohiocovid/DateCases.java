package com.robrousejr.ohiocovid;

import java.util.Calendar;
import java.util.Date;

public class DateCases {

    private Date date;
    private int cases;

    public DateCases(Date date, int cases) {
        this.date = date;
        this.cases = cases;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCases() {
        return cases;
    }

    public void setCases(int cases) {
        this.cases = cases;
    }

    @Override
    public String toString() {
        return getDate().toString() + " : " + getCases();
    }

    public int getDayOfYear() {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_YEAR);
    }
}
