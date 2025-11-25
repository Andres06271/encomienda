package co.edu.unipiloto.encomienda.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // IMPORTANTE: Cambiar por tu IP local del PC (usa ipconfig en Windows)
    // Ejemplo: "http://192.168.1.15:8080/" 
    private static final String BASE_URL = "http://192.168.1.10:8080/"; // <-- CAMBIAR AQUÍ

    private static EncomiendaApi apiInstance;

    public static EncomiendaApi getApi() {
        if (apiInstance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiInstance = retrofit.create(EncomiendaApi.class);
        }
        return apiInstance;
    }
}
