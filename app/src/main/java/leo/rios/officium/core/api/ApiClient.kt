package leo.rios.officium.core.api


import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import androidx.room.Room
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.core.database.OfficiumDatabase
import leo.rios.officium.core.database.dao.ProvinciaDao
import leo.rios.officium.core.database.dao.SectorDao
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiClient {

    @Singleton
    @Provides
    fun provideOkHttpClient(dataStoreManager: DataStoreManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                val token = runBlocking {
                    dataStoreManager.getAccessToken().firstOrNull()
                }

                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient):Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.officium.es/api/")
           .client(okHttpClient)
           .addConverterFactory(GsonConverterFactory.create())
           .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit) :ApiService{
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideOfficiumDatabase(@ApplicationContext context: Context): OfficiumDatabase {
        return Room.databaseBuilder(
            context,
            OfficiumDatabase::class.java,
            "officium_database"
        ).build()
    }

    @Provides
    fun provideSectorDao(database: OfficiumDatabase): SectorDao {
        return database.sectorDao()
    }

    @Provides
    fun provideProvinciaDao(database: OfficiumDatabase): ProvinciaDao {
        return database.provinciaDao()
    }



    @Singleton
    @Provides
    fun provideDataStore(context: Context) :DataStoreManager{
        return DataStoreManager(context)
    }

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context) :Context{
        return context
    }

}


//val retrofit : Retrofit = Retrofit.Builder() //Devuelve un objeto Retrofit
//    .baseUrl("http://127.0.0.1:3505/") //Ruta Fija
//    .addConverterFactory(GsonConverterFactory.create()) //
//    .build()
//
//val apiService: ApiService = retrofit.create(ApiService::class.java)
