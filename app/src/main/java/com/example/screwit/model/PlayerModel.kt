package com.example.screwit.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "player")
data class PlayerModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var score :Int,
    var color : Int
)
