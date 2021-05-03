package com.example.petreg;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtils {
    private static Retrofit retrofit;
    private static PetAPI petAPI;

    public static Retrofit getRetrofit(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.0.105:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static PetAPI getApi(){
        if(petAPI == null){
            petAPI = getRetrofit().create(PetAPI.class);
        }
        return petAPI;
    }
}
