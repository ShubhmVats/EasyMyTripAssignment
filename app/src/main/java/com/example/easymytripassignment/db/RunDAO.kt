package com.example.easymytripassignment.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKilometer DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeter DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>


    @Query("SELECT SUM(timeInMillis) from running_table")
    fun getTotalTimeMillis() : LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) from running_table")
    fun getCaloriesBurned() : LiveData<Long>

    @Query("SELECT SUM(distanceInMeter) from running_table")
    fun getDistanceInMeter() : LiveData<Int>


    @Query("SELECT AVG(avgSpeedInKilometer) from running_table")
    fun getAvgSpeed() : LiveData<Float>


}