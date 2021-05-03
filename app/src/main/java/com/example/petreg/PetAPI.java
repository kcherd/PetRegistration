package com.example.petreg;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PetAPI {
    @GET("pet")
    Call<JsonObject> getPet(@Query("id") long id);

    @FormUrlEncoded
    @POST("pet")
    Call<JsonPrimitive> insertPet(@Field("pet") String pet);
}
