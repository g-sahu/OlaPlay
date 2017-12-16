package com.ola.gasahu.olaplay.services;

import com.ola.gasahu.olaplay.beans.Track;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

interface OlaPlayApi {
    @GET("studio")
    Call<List<Track>> getTracks();
}
