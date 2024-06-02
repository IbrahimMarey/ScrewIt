package com.example.screwit.view_model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.screwit.data.IScrewLocalDataSource
import com.example.screwit.model.PlayerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class ItemViewModel(private val local : IScrewLocalDataSource) : ViewModel() {
    private val itemColors = mutableMapOf<Int, Color>()
    val itemNames =  mutableStateListOf<String>()

    private val _playerList = MutableStateFlow<List<PlayerModel>>(emptyList())
    val playerList: StateFlow<List<PlayerModel>> get() = _playerList

    init {
        getAllPlayers()
    }
    fun insertPlayer(player: PlayerModel) {
        viewModelScope.launch {
            val existingPlayer = local.getPlayerByName(player.name)
            if (existingPlayer != null) {
                local.updatePlayerScore(player.name, player.score)
            } else {
                local.insertPlayer(player)
            }
        }
    }

    fun updatePlayer(player: PlayerModel) {
        viewModelScope.launch {
            local.updatePlayer(player)
            getAllPlayers()
        }
    }
    fun addPlayer(name: String) {
        viewModelScope.launch {
            val newPlayer = PlayerModel(name = name, score = 0, color = generateRandomColor().toArgb())
            local.insertPlayer(newPlayer)
            getAllPlayers()
        }
    }

    fun incrementScore(player: PlayerModel) {
        viewModelScope.launch {
            val updatedPlayer = player.copy(score = player.score + 1)
            local.updatePlayer(updatedPlayer)
            getAllPlayers()
        }
    }

    fun decrementScore(player: PlayerModel) {
        viewModelScope.launch {
            val updatedPlayer = player.copy(score = player.score - 1)
            local.updatePlayer(updatedPlayer)
            getAllPlayers()
        }
    }

    private fun getAllPlayers() {
        viewModelScope.launch {
            _playerList.value = local.getAllPlayers()
        }
    }

    fun insertAllPlayers(players: List<PlayerModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            local.insertAllPlayer(players)
        }
    }

    fun deleteAllPlayers(players: List<PlayerModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            local.deleteAllPlayer(players)
            getAllPlayers()
        }
    }

    fun resetAllPlayers() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch {
                playerList.value.forEach { player ->
                    val updatedPlayer = player.copy(score = 0)
                    local.updatePlayer(updatedPlayer)
                }
                getAllPlayers()
            }
        }
    }

    fun getColorForItem(index: Int): Color {
        return itemColors[index] ?: generateRandomColor().also {
            itemColors[index] = it
        }
    }

    fun editColor(index:Int,color:Color){
        itemColors[index] = color
    }

    private fun generateRandomColor(): Color {
        return Color(
            red = Random.nextFloat(),
            green = Random.nextFloat(),
            blue = Random.nextFloat(),
            alpha = 1f
        )
    }

    companion object {
        fun provideFactory(local: IScrewLocalDataSource): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
                        return ItemViewModel(local) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}