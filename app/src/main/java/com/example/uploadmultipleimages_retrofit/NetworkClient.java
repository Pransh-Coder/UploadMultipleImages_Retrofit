package com.example.uploadmultipleimages_retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//We are creating this class so taht we could have only singualar instance of retrofit throught the whole app
public class NetworkClient {
    private  static Retrofit retrofit;
    public static String BASE_URL = "http://paytmpay001.dx.am/api/raeces/";


    public  static Retrofit getRetrofit(){
        if(retrofit==null){         // we check it so that whwether this retrofit instance is null in the begning so that  so that this is not everytime when we call this getRetrofit() method
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();        //retrofit instance
        }
        return retrofit;
    }
}
