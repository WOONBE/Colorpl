package com.colorpl.di

import android.content.Context
import com.colorpl.BuildConfig
import com.colorpl.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    //local property로 따로 빼기
    val baseUrl = "http://192.168.100.142:8080/"
    val tmapUrl = "https://apis.openapi.sk.com/"

    @Singleton
    @Provides
    @NormalRetrofit
    fun provideRetrofit(@NormalOkHttp okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @NormalOkHttp
    fun provideOkHttpClient() = OkHttpClient.Builder().run {
        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        connectTimeout(120, TimeUnit.SECONDS)
        readTimeout(120, TimeUnit.SECONDS)
        writeTimeout(120, TimeUnit.SECONDS)
        build()
    }

    @Singleton
    @Provides
    @TmapRetrofit
    fun provideTmapRetrofit(@TmapOkHttp okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .baseUrl(tmapUrl)
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @TmapOkHttp
    fun provideTmapOkHttpClient() = OkHttpClient.Builder().run {
        addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("appKey", BuildConfig.TMAP_APP_KEY)
                .method(original.method, original.body)
                .build()
            chain.proceed(request = request)
        }
        connectTimeout(120, TimeUnit.SECONDS)
        readTimeout(120, TimeUnit.SECONDS)
        writeTimeout(120, TimeUnit.SECONDS)
        build()
    }

    @Singleton
    @Provides
    fun provideGetSignInWithGoogleOption(
        @ApplicationContext context: Context
    ): GetSignInWithGoogleOption {
        return GetSignInWithGoogleOption.Builder(
            context.getString(R.string.default_web_client_id)
        )
            .build()

    }
}