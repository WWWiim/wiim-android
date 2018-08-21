/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import br.com.joseafga.wiim.models.Process;
import br.com.joseafga.wiim.models.Tag;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public class WiimApi {
    // for future implementation
    private static final String API_KEY = "xyz";

    public interface Service {
        // http://www.joseafga.com.br/wiim/api/v1/processes/
        @Headers("Accept: application/json")
        @GET("processes/")
        Call<ArrayList<Process>> getProcesses();

        // http://www.joseafga.com.br/wiim/api/v1/processes/:id
        @Headers("Accept: application/json")
        @GET("processes/{id}")
        Call<Process> getProcess(@Path("id") String id);

        // http://www.joseafga.com.br/wiim/api/v1/tags/:id
        @Headers("Accept: application/json")
        @GET("tags/{id}")
        Call<Tag> getTags(@Path("id") String id);
    }

    public static Service getService(String url) {
        // limit number of connections and keep-alive duration
        ConnectionPool pool = new ConnectionPool(5, 60, TimeUnit.SECONDS);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionPool(pool)
                .build();

        // Create a very simple REST adapter which points the Wiim API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create an instance of our Wiim API interface.
        return retrofit.create(Service.class);
    }

}
