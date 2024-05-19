package com.example.screwit.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.screwit.model.PlayerModel

@Dao
interface PlayerDao {

    @Query("SElECT * From player")
   suspend fun getAllPlayer() : List<PlayerModel>
   @Insert
   suspend fun insertAllPlayer(players : List<PlayerModel>)

   @Insert
   suspend fun insertPlayer(player: PlayerModel)

   @Delete
   suspend fun deleteAllPlayers(players: List<PlayerModel>)
   @Query("UPDATE player SET score = :newScore WHERE name = :playerName")
   suspend fun updatePlayerScore(playerName: String, newScore: Int)

   @Query("SELECT * FROM player WHERE name = :playerName LIMIT 1")
   suspend fun getPlayerByName(playerName: String): PlayerModel?

   @Update
   suspend fun update(player: PlayerModel)

}