package com.james.disasterapp.api

import com.james.disasterapp.model.DisasterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET ("reports?timeperiod=604800")
    suspend fun getDisaster(
    ) : DisasterResponse

    @GET ("reports?timeperiod=604800")
    suspend fun getSearchingDisaster(
        @Query("admin") admin : String
    ) : DisasterResponse

    @GET ("reports?timeperiod=604800")
    suspend fun getFilterDisaster(
        @Query("disaster") disaster : String
    ) : DisasterResponse

    @GET ("reports?timeperiod=604800&disaster=flood")
    suspend fun getNewrestDisaster(
    ) : DisasterResponse

}