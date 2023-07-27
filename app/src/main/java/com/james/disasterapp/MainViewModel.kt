package com.james.disasterapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.james.disasterapp.model.GeometriesItem

class MainViewModel() : ViewModel() {
    private val mRepository: Repository = Repository()

    fun getDisaster() : LiveData<ResultCustom<List<GeometriesItem?>?>> {
        return mRepository.getDisaster()
    }

    fun getSearchingDisaster(admin : String) : LiveData<ResultCustom<List<GeometriesItem?>?>> {
        return mRepository.getSearchingDisaster(admin)
    }

    fun getFilterDisaster(disaster : String) : LiveData<ResultCustom<List<GeometriesItem?>?>> {
        return mRepository.getFilterDisaster(disaster)
    }
}