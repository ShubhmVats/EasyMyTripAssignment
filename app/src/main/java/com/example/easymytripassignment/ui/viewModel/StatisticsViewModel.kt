package com.example.easymytripassignment.ui.viewModel

import androidx.lifecycle.ViewModel
import com.example.easymytripassignment.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class StatisticsViewModel  constructor(val mainRepository: MainRepository)  : ViewModel(){
}