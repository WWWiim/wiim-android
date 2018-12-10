/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import br.com.joseafga.wiim.models.Process;
import br.com.joseafga.wiim.models.Record;
import br.com.joseafga.wiim.models.Tag;
import br.com.joseafga.wiim.models.Timeline;
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
    // connection objects
    // limit number of connections and keep-alive duration
    private static ConnectionPool pool = new ConnectionPool(5, 60, TimeUnit.SECONDS);;
    private static OkHttpClient client = new OkHttpClient.Builder().connectionPool(pool).build();

    /**
     * API interface
     */
    public interface Service {
        // http://example.com/api/v1/processes/
        @Headers("Accept: application/json")
        @GET("processes/")
        Call<ArrayList<Process>> getProcesses();

        // http://example.com/api/v1/processes/:id
        @Headers("Accept: application/json")
        @GET("processes/{id}")
        Call<Process> getProcess(@Path("id") String id);

        // http://example.com/api/v1/processes/:id/tags
        @Headers("Accept: application/json")
        @GET("processes/{id}/tags")
        Call<ArrayList<Tag>> getProcessTags(@Path("id") String id);

        // http://example.com/api/v1/tags/:id
        @Headers("Accept: application/json")
        @GET("tags/{id}")
        Call<Tag> getTag(@Path("id") String id);

        // http://example.com/api/v1/tags/:id/records
        @Headers("Accept: application/json")
        @GET("tags/{id}/records")
        Call<ArrayList<Record>> getTagRecords(@Path("id") String id);

        // http://example.com/api/v1/processes/:id/timeline
        @Headers("Accept: application/json")
        @GET("processes/{id}/timeline")
        Call<ArrayList<Timeline>> getProcessTimeline(@Path("id") String id);
    }

    /**
     * Cancel all OkHttpClient requests
     */
    public static void cancelRequests() {
        client.dispatcher().cancelAll();
    }

    /**
     * Get a Retrofit service
     *
     * @param url API URL
     * @return retrofit instance
     */
    public static Service getService(String url) {
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
