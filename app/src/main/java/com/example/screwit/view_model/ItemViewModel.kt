package com.example.screwit.view_model

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.screwit.data.IScrewLocalDataSource
import com.example.screwit.model.PlayerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ItemViewModel(val local : IScrewLocalDataSource) : ViewModel() {
    private val itemColors = mutableMapOf<Int, Color>()
    val itemNames =  mutableStateListOf<String>()

    private var _playersList = mutableStateListOf<PlayerModel>()
    val playerList: List<PlayerModel> get() = _playersList

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


    fun getAllPlayers(){
        viewModelScope.launch(Dispatchers.IO) {
            val players = local.getAllPlayers()
            withContext(Dispatchers.Main){
                _playersList.clear()
                _playersList.addAll(players)
            }
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

    override fun onCleared() {
        super.onCleared()
        insertAllPlayers(_playersList)
    }
}