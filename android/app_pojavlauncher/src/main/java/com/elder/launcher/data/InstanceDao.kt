package com.elder.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanceDao {
    @Query("SELECT * FROM instances ORDER BY lastPlayed DESC, id ASC")
    fun observeAll(): Flow<List<InstanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instance: InstanceEntity): Long

    @Query("UPDATE instances SET lastPlayed = :timestamp WHERE id = :id")
    suspend fun markPlayed(id: Long, timestamp: Long)

    @Query("DELETE FROM instances WHERE id = :id")
    suspend fun delete(id: Long)
}