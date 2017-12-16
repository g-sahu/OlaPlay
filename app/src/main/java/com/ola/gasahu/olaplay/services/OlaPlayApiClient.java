package com.ola.gasahu.olaplay.services;

import com.ola.gasahu.olaplay.beans.Track;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OlaPlayApiClient {
    private static final String BASE_URL = "http://starlord.hackerearth.com/";
    private static Retrofit retrofit = null;

    private static Retrofit getClient() {
        if(retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static List<Track> fetchTrackList() throws IOException {
        OlaPlayApi apiService = getClient().create(OlaPlayApi.class);
        Call<List<Track>> call = apiService.getTracks();
        return call.execute().body();
    }
}
