package com.example.luanvantotnghiep;

public class User {

    private int date;
    private int month;
    private int year;
    private boolean out_of_date;

    public User() {
    }

    public User(int date, int month, int year, boolean out_of_date){
        this.date = date;
        this.month = month;
        this.year = year;
        this.out_of_date = out_of_date;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isOut_of_date() {
        return out_of_date;
    }

    public void setOut_of_date(boolean out_of_date) {
        this.out_of_date = out_of_date;
    }

    @Override
    public String toString() {
        return  date +
                "/" + month +
                "/" + year ;
    }
}
