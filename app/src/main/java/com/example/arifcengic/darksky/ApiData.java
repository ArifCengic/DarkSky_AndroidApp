package com.example.arifcengic.darksky;

/**
 * Created by arifcengic on 3/14/17.
 */

class ApiData{
    public ApiData(double minTemp, double maxTemp,int ts)
    {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.ts = ts;
    }
    public double minTemp;
    public double maxTemp;
    public int  ts;
}
