package com.example.screwit.data

import com.example.screwit.model.PlayerModel

interface IScrewLocalDataSource {
    suspend fun getAllPlayers() : List<PlayerModel>
    suspend fun insertAllPlayer(players : List<PlayerModel>)
    suspend fun deleteAllPlayer(players: List<PlayerModel>)
    suspend fun insertPlayer(player: PlayerModel)
    suspend fun updatePlayerScore(playerName: String, newScore: Int)
    suspend fun getPlayerByName(playerName: String): PlayerModel?
    suspend fun updatePlayer(player: PlayerModel)

}