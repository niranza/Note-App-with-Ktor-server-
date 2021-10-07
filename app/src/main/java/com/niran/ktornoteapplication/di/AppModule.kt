package com.niran.ktornoteapplication.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.niran.ktornoteapplication.dataset.retrofit.BasicAuthInterceptor
import com.niran.ktornoteapplication.dataset.retrofit.apis.NoteApi
import com.niran.ktornoteapplication.dataset.room.AppDatabase
import com.niran.ktornoteapplication.dataset.room.daos.NoteDao
import com.niran.ktornoteapplication.utils.Constants.BASE_URL
import com.niran.ktornoteapplication.utils.Constants.DATABASE_NAME
import com.niran.ktornoteapplication.utils.Constants.ENCRYPTED_SHARED_PREF_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Singleton
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigrationOnDowngrade()
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideNoteDao(
        database: AppDatabase
    ): NoteDao = database.noteDao

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor(): BasicAuthInterceptor = BasicAuthInterceptor()

    @Singleton
    @Provides
    fun provideLoggingInterceptorInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { setLevel(BODY) }

    @Singleton
    @Provides
    fun provideOkHttpClientBuilder(): OkHttpClient.Builder {
        val trustAllCertificates: Array<TrustManager> = arrayOf(
            //for testing
            object : X509TrustManager {
                override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                    /* NO-OP */
                }

                override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                    /* NO-OP */
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCertificates, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCertificates[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
    }

    @Singleton
    @Provides
    fun provideNoteApi(
        okHttpClientBuilder: OkHttpClient.Builder,
        basicAuthInterceptor: BasicAuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): NoteApi {

        val client = okHttpClientBuilder
            .addInterceptor(loggingInterceptor)
            .addInterceptor(basicAuthInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoteApi::class.java)
    }

    @Singleton
    @Provides
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_SHARED_PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}