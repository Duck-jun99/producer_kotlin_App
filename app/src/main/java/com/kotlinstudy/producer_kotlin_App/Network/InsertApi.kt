package com.kotlinstudy.producer_kotlin_App.Network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kotlinstudy.producer_kotlin_App.Secret
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface InsertApi {
    @FormUrlEncoded
    @POST(Secret.INSERT_PHP_FILE)
    @Headers(
        "accept: application/json",
        "content-type: application/x-www-form-urlencoded; charset=utf-8"
    )
    fun insertData(
        @Field("log_ID") log_ID: String,
        @Field("sensor_x") sensor_x: String,
        @Field("sensor_y") sensor_y: String,
        @Field("sensor_z") sensor_z: String

        //@Body data: PostModel
        //@Field를 사용하고 @FormUrlEncoded를 사용해야 올바른 Json형식으로 넘어감
    ): Call<InsertPostModel>

    companion object {
        fun create(): InsertApi {
            val gson: Gson = GsonBuilder().setLenient().create()

            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val moshi: Moshi = Moshi.Builder().build()
            val converterFactory: MoshiConverterFactory = MoshiConverterFactory.create(moshi)

            return Retrofit.Builder()
                .baseUrl(Secret.MY_IP_ADDRESS)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
              //  .addConverterFactory(converterFactory)
                .build()
                .create(InsertApi::class.java)
        }
    }
}