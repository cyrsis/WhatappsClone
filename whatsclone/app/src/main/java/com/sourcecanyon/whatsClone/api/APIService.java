package com.sourcecanyon.whatsClone.api;


import android.content.Context;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sourcecanyon.whatsClone.BuildConfig;

import org.xml.sax.ErrorHandler;

import io.realm.RealmObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Abderrahim El imame on 27/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class APIService {

    protected final Context context;

    public static APIService with(Context context) {
        return new APIService(context);
    }

    public APIService(Context context) {
        this.context = context;
    }

    static Gson gson = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().equals(RealmObject.class);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    public <S> S RootService(Class<S> serviceClass, final String token, String baseUrl) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // Customize the request
            Request request = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("token", token)
                    .method(original.method(), original.body())
                    .build();
            // Customize or return the response

            return chain.proceed(request);
        });


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
       /*if (BuildConfig.DEBUG) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);//todo khasni nchof liha l7el
        } else {*/
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
       // }
        OkHttpClient client = httpClient.addInterceptor(interceptor).build();
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return builder.create(serviceClass);
    }


    public static <S> S RootService(Class<S> serviceClass, String baseUrl) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // Customize the request
            Request request = original.newBuilder()
                    .method(original.method(), original.body())
                    .build();

            // Customize or return the response
            return chain.proceed(request);
        });

        OkHttpClient client = httpClient.build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client);

        Retrofit adapter = builder.build();

        return adapter.create(serviceClass);
    }
}