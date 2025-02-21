package leo.rios.officium.core.api


import android.content.Context
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import leo.rios.officium.core.dataStore.DataStoreManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent ::class)
object ApiClient {

    @Singleton
    @Provides
    fun provideRetrofit():Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3505/")
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

