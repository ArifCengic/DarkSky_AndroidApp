package com.example.arifcengic.darksky;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by arifcengic on 3/14/17.
 */

interface DarkSkyService {

    final String URL =  "https://api.darksky.net/forecast/c57f7284ecdf0e3ef8309aa87e5cbc4d/";

    @GET("{lng},{lat},{ts}?exclude=hourly,flags")
    Call<Example> getDailyWeather(@Path("lng") double lng,
                                  @Path("lat") double lat,
                                  @Path("ts") long ts);

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}