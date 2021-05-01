package com.example.petreg;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PetAPI {
    @GET("pet")
    Call<JsonObject> getPet(@Query("id") long id);

    @POST("pet")
    Call<JsonObject> insertPet(@Body String pet);
}
