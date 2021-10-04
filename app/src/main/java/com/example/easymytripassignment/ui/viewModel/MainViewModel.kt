package com.example.easymytripassignment.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymytripassignment.db.Run
import com.example.easymytripassignment.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainRepository: MainRepository)  : ViewModel(){


    val runSortedByDate = mainRepository.getAllRunSortedByDate()

    fun insertRun(run: Run)= viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}