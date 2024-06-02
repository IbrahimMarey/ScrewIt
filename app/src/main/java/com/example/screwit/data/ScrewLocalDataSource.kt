package com.example.screwit.data

import com.example.screwit.database.PlayerDao
import com.example.screwit.model.PlayerModel

class ScrewLocalDataSource(val playerDao : PlayerDao) : IScrewLocalDataSource {
    override suspend fun getAllPlayers(): List<PlayerModel> {
        return playerDao.getAllPlayer()
    }
    override suspend fun insertAllPlayer(players: List<PlayerModel>) {
        playerDao.insertAllPlayer(players)
    }
    override suspend fun deleteAllPlayer(players: List<PlayerModel>) {
        playerDao.deleteAllPlayers(players)
    }

    override suspend fun insertPlayer(player: PlayerModel) {
        playerDao.insertPlayer(player)
    }

    override suspend fun updatePlayerScore(playerName: String, newScore: Int) {
        playerDao.updatePlayerScore(playerName, newScore)
    }

    override suspend fun getPlayerByName(playerName: String): PlayerModel? {
        return playerDao.getPlayerByName(playerName)
    }

    override suspend fun updatePlayer(player: PlayerModel) {
        playerDao.update(player)
    }
}