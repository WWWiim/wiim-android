package br.com.joseafga.wiim;

import br.com.joseafga.wiim.models.Process;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class WiimApi {
    // for future implementation
    private static final String API_KEY = "xyz";

    // http://www.joseafga.com.br/wiim/api/v1/processes/:id
    public interface WiimService {
        @GET("processes/{id}")
        Call<Process> getProcess(@Path("id") String id);
    }

    // http://www.joseafga.com.br/wiim/api/v1/tags/:id
    // TODO

    public static WiimService getService(String url) {
        // Create a very simple REST adapter which points the Wiim API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create an instance of our Wiim API interface.
        return retrofit.create(WiimService.class);
    }
}
