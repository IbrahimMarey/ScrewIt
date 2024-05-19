package com.example.screwit


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.screwit.data.IScrewLocalDataSource
import com.example.screwit.data.ScrewLocalDataSource
import com.example.screwit.database.ScrewDatabase
import com.example.screwit.model.PlayerModel
import com.example.screwit.ui.theme.ScrewItTheme
import com.example.screwit.view_model.ItemViewModel
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker

class MainActivity : ComponentActivity() {
    private lateinit var localDataSource: IScrewLocalDataSource

    private val viewModel: ItemViewModel by viewModels {
        ItemViewModel.provideFactory(localDataSource)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localDataSource = ScrewLocalDataSource(ScrewDatabase.getDatabase(context = this).playerDao())

        setContent {
            MaterialTheme {
                colorResource(id = R.color.black)
                ScrewItTheme (true){
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.Black),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HomeScreen(viewModel = viewModel)
                    }
                }
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ItemViewModel) {
    var itemCount by remember { mutableStateOf(1) }
    var counts = remember { mutableStateListOf<Int>() }
    var showDialog by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getAllPlayers()
    }

    Scaffold(
        Modifier.background(color = Color.Black),
                topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0,0,0,3)),
                title = { Text("Screw Score", color = Color.White) },
                actions = {
                    IconButton(onClick = {
                        showDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            MeasureTopAppBarHeight {
                BodyContent(
                    modifier = Modifier.padding(innerPadding),
                    counts = counts,
                    playerList = viewModel.playerList,
                    itemNames = viewModel.itemNames,
                    onIncrement = { index ->
                        if (counts[index] < Int.MAX_VALUE) {
                            counts[index]++
                        }
                    },
                    onDecrement = { index ->
                        if (counts[index] > 0) {
                            counts[index]--
                        }
                    },
                    viewModel = viewModel
                )
            }
        }

    )


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Add Player") },
            text = {
                Column {
                    Text("Enter name:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                }
            }
            ,confirmButton = {
                TextButton(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            itemCount++
                            counts.add(0)
                            viewModel.itemNames.add(newItemText)
                            newItemText = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun MeasureTopAppBarHeight(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var topAppBarHeight by remember { mutableStateOf(0) }

    Box(
        modifier
            .onGloballyPositioned { coordinates ->
                topAppBarHeight = coordinates.size.height
            }
    ) {
        content()
    }
}

@Composable
fun BodyContent(
    modifier: Modifier = Modifier,
    counts: List<Int>,
    itemNames: List<String>,
    onDecrement: (Int) -> Unit,
    onIncrement: (Int) -> Unit,
    viewModel: ItemViewModel,
    playerList: List<PlayerModel>,
) {
    var showDialog by remember { mutableStateOf(false) }
    var newItemTextAfter by remember { mutableStateOf("") }
    val newItemNames = remember { mutableStateListOf<String>() }
    var clickIndexItemToEdit = remember {
      mutableStateOf(0)
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(counts.size) { index ->
            val player = PlayerModel(
                index
                ,itemNames.get(index),
                0,
                viewModel.getColorForItem(index).toArgb()
            )

            viewModel.insertPlayer(
                PlayerModel(
                    name = player.name,
                    color = player.color,
                    score = counts[index]
                )
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(player.color) /*viewModel.getColorForItem(index)*/)
                            .padding(horizontal = 16.dp, vertical = 2.dp)

                    ) {
                            Text(
                                text = player.name /*itemNames.getOrNull(index)*/ ?: "Name $index",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        IconButton(onClick = {
                            clickIndexItemToEdit.value = index
                            showDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Color",
                                tint = Color.White
                            )
                        }
                        }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                            .background(Color.DarkGray)
                    ) {
                        Button(
                            onClick = { onDecrement(index) },
                            enabled = counts[index] > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                            ),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "-",
                                fontSize = 30.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column {
                            Text(
                                text = "score",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontSize = 16.sp,
                                color = Color.White,
                            )
                            Text(
                                text = "  ${counts[index]}",
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clickable { openDialogToEdit() },
                                fontSize = 30.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { onIncrement(index) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor  = Color.White
                            ),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "+",
                                fontSize = 30.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        newItemTextAfter = itemNames.get(index = clickIndexItemToEdit.value)
        var color = remember {
            viewModel.getColorForItem(clickIndexItemToEdit.value)
        }
        val controller : ColorPickerController = ColorPickerController()
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Edit") },
            text = {
                Column {
                    Text("Enter name:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newItemTextAfter,
                        onValueChange = {
                            newItemTextAfter = it
                            viewModel.itemNames[clickIndexItemToEdit.value] = newItemTextAfter
                                        },
                        label = { Text("Name") },
                        singleLine = true
                    )

                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                            .padding(10.dp),
                        controller = controller,
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            color = colorEnvelope.color
                        }
                    )
                }
            }
            ,confirmButton = {
                TextButton(
                    onClick = {
                        if (newItemTextAfter.isNotBlank()) {
                            newItemNames.add(newItemTextAfter)
                            newItemTextAfter = ""
                            if(color != Color(0, 0, 0, 0))
                                viewModel.editColor(clickIndexItemToEdit.value,color)
                            showDialog = false
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun openDialogToEdit() {

}
