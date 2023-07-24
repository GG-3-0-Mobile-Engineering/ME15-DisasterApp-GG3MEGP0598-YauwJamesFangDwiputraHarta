package com.james.disasterapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.james.disasterapp.api.ApiConfig
import com.james.disasterapp.model.GeometriesItem


class Repository {

    fun getDisaster() : LiveData<ResultCustom<List<GeometriesItem?>?>> = liveData {
        emit (ResultCustom.Loading)
        try {
            val response = ApiConfig.getApiService().getDisaster()
            if (response.result?.objects?.output?.geometries!!.isNotEmpty()){
                emit(ResultCustom.Success(response.result.objects.output.geometries))
            } else{
                emit (ResultCustom.Error("disaster tidak ditemukan"))
            }
        }catch (exception: Exception) {
            emit(ResultCustom.Error(exception.message ?: "Unknown error"))
        }
    }

    fun getSearchingDisaster(admin : String) : LiveData<ResultCustom<List<GeometriesItem?>?>> = liveData {
        emit (ResultCustom.Loading)
        try {
            val response = ApiConfig.getApiService().getSearchingDisaster(admin)
            if (response.result?.objects?.output?.geometries!!.isNotEmpty()){
                emit(ResultCustom.Success(response.result.objects.output.geometries))
            } else{
                emit (ResultCustom.Error("Marker tidak ditemukan"))
            }
        }catch (exception: Exception) {
            emit(ResultCustom.Error(exception.message ?: "Unknown error"))
        }
    }
}