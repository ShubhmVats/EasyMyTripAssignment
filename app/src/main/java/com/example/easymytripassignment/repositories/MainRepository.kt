package com.example.easymytripassignment.repositories

import com.example.easymytripassignment.db.Run
import com.example.easymytripassignment.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(val runDao: RunDAO) {

suspend fun insertRun(run: Run) = runDao.insertRun(run)
    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)
     fun getAllRunSortedByDate() = runDao.getAllRunsSortedByDate()
    suspend fun getAllRunSortedByDistance(run: Run) = runDao.getAllRunsSortedByDistance()
    suspend fun getAllRunSortedByTimemillis(run: Run) = runDao.getAllRunsSortedByTimeMillis()
    suspend fun getAllRunSortedByCalorieBurned(run: Run) = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getAvgSpeed()
    fun getTotalDistance() = runDao.getDistanceInMeter()
    fun getTotalCalorieBurned() = runDao.getCaloriesBurned()
    fun getTotalTimeMillis() = runDao.getTotalTimeMillis()




}