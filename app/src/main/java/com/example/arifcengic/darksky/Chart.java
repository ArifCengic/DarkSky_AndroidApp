package com.example.arifcengic.darksky;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Chart extends AppCompatActivity {

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar);
        progressDialog = new ProgressDialog(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            LatLng latLong = new LatLng(extras.getDouble("latitude"), extras.getDouble("longitude"));
            GetDataFromAPI(latLong);
        }

    }


    void GetDataFromAPI(LatLng latLong)
    {
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");

        final BarChart chart = (BarChart) findViewById(R.id.chart);
        final int DAYS = 7;
        //Pick Thread Safe Java Collection - Vector
        final Vector<ApiData> apiDataList = new Vector<ApiData>() ;

        DarkSkyService weatherService = DarkSkyService.retrofit.create(DarkSkyService.class);
        long todayTS = new Date().getTime()/1000;
        progressDialog.show();

        //get weather data, start from yesterday (today - 1 day ... today - 7 days
        for(int i=1; i < DAYS + 1; i++) {

            long previousDayTS = todayTS - (i * 24*60*60); //deduct 1 day 24*60*60 sec, we do not convert to Date object..
            final Call<Example> call = weatherService.getDailyWeather(latLong.latitude, latLong.longitude, previousDayTS);
            Log.i("REST", "Lat -" + Double.toString(latLong.latitude) + " Lng " + Double.toString(latLong.longitude) + " TS " + Long.toString(previousDayTS));

            call.enqueue(new Callback<Example>() {
                @Override
                public void onResponse(Call<Example> call, Response<Example> response) {

                    if (response.body() != null) {
                        Datum d = response.body().getDaily().getData().get(0);

                        //cast to long othervise timestamp Overflow int AND turns negative (pre 01-01-1970)
                        Date dd = new Date(d.getTime() * (long)1000);
                        Log.i("REST", response.body().getTimezone() + " Date " + new SimpleDateFormat("dd-MM-yyyy").format(dd) + " Max " + d.getTemperatureMax().toString() + " Min " + d.getTemperatureMin().toString());

                        apiDataList.add(new ApiData(d.getTemperatureMin(),d.getTemperatureMax(), d.getTime()));

                        if (apiDataList.size() == DAYS)
                        {
                            //Received all callbacks for all DAYS (7) days
                            if(progressDialog.isShowing()) progressDialog.dismiss();
                            getSupportActionBar().setTitle("Last " + DAYS + " days " + response.body().getTimezone());

                            //order apiDataList by date/timestamp, async REST calls can return in any order
                            Collections.sort(apiDataList, new Comparator<ApiData>() {
                                @Override
                                public int compare(ApiData d1, ApiData d2)
                                {
                                    return  Integer.compare(d2.ts, d1.ts);
                                }
                            });

                            final List<BarEntry> entriesMax = new ArrayList<BarEntry>();
                            final List<BarEntry> entriesMin = new ArrayList<BarEntry>();

                            //array of dayOfWeek values Mon, Tue ...
                            final String[] days = new String[7];

                            //move REST data from apiDataList to format
                            // required by BarChart component
                            for(int i=0; i< DAYS; i++) {
                                ApiData ad = apiDataList.get(i);
                                entriesMax.add(new BarEntry(i, (float)ad.maxTemp));
                                entriesMin.add(new BarEntry(i, (float)ad.minTemp));
                                days[i] = new SimpleDateFormat("EEE").format(new Date(ad.ts*(long)1000));
                            }

                            //setup and display BarChar
                            BarDataSet dataSetMax = new BarDataSet(entriesMax, "Max");
                            BarDataSet dataSetMin = new BarDataSet(entriesMin, "Min");
                            BarData data = new BarData(dataSetMax,dataSetMin);
                            data.setBarWidth(0.75f);
                            dataSetMax.setColor(Color.RED);
                            dataSetMin.setColor(Color.DKGRAY);
                            setX_Labels(chart, days);
                            chart.setData(data);
                            chart.invalidate(); // refresh Chart
                        }
                    }
                    else
                    {
                        if(progressDialog.isShowing()) progressDialog.dismiss();
                        Log.e("REST", "Rest Response null: " + response.raw());
                        Toast.makeText(Chart.this, "Error getting data from remote server", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<Example> call, Throwable t) {
                    Log.e("REST", "Error: " + t.getMessage());
                    if(progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(Chart.this, "Error getting data from remote server", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void setX_Labels(BarChart chart, final String[] days )
    {
        // the labels that should be drawn on the XAxis
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return days[(int) value];
            }
        };
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        chart.invalidate(); // refresh
    }
}

