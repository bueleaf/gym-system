package com.example.training.document;

import java.util.ArrayList;
import java.util.List;

public class YearSummary
{
    private Integer year;

    private List<MonthSummary> months;

    public Integer getYear() {
        return year;
    }

    public List<MonthSummary> getMonths() {
        return months;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setMonths(List<MonthSummary> months) {
        this.months = months;
    }
}
