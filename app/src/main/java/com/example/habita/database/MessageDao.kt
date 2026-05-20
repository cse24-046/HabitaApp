package com.example.habita.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatRoomId = :chatRoomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(chatRoomId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT DISTINCT chatRoomId FROM messages")
    fun getAllChatRooms(): Flow<List<String>>
    
    @Query("SELECT * FROM messages GROUP BY chatRoomId ORDER BY timestamp DESC")
    fun getLatestMessagesPerRoom(): Flow<List<Message>>
}