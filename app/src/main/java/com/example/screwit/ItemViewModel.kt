package com.example.screwit

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class ItemViewModel : ViewModel() {
    private val itemColors = mutableMapOf<Int, Color>()

    val itemNames =  mutableStateListOf<String>()
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


}