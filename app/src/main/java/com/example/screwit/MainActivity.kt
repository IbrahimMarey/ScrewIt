package com.example.screwit


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var showDialog by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }
    val playerList by viewModel.playerList.collectAsState()

    Scaffold(
        Modifier.background(color = Color.Black),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0, 0, 0, 3)),
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
                    playerList = playerList,
                    onIncrement = { player ->
                        viewModel.incrementScore(player)
                    },
                    onDecrement = { player ->
                        viewModel.decrementScore(player)
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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            viewModel.addPlayer(newItemText)
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
    playerList: List<PlayerModel>,
    onDecrement: (PlayerModel) -> Unit,
    onIncrement: (PlayerModel) -> Unit,
    viewModel: ItemViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var editPlayer: PlayerModel? by remember { mutableStateOf(null) }
    var showEditScoreDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(playerList.size) { index ->
            val player = playerList[index]

            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(player.color))
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.man),
                                    contentDescription = "Player Avatar",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = player.name,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )}
                        IconButton(onClick = {
                            editPlayer = player
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                            .background(Color.DarkGray)
                            .height(105.dp)
                    ) {
                        Button(
                            onClick = { onDecrement(player) },
                            //enabled = player.score > 0,
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ){
                            Text(
                                text = "score",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontSize = 16.sp,
                                color = Color.White,
                            )
                            Text(
                                text = "${player.score}",
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clickable {
                                        editPlayer = player
                                        showEditScoreDialog = true
                                      },
                                fontSize = 30.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { onIncrement(player) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
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

    if (showDialog && editPlayer != null) {
        val player = editPlayer!!
        var newName by remember { mutableStateOf(player.name) }
        var color by remember { mutableStateOf(Color(player.color)) }
        val controller: ColorPickerController = ColorPickerController()

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Edit Player") },
            text = {
                Column {
                    Text("Enter name:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newName,
                        onValueChange = {
                            newName = it
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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.updatePlayer(player.copy(name = newName, color = color.toArgb()))
                            viewModel.editColor(playerList.indexOf(player), color)
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


// dialog to edit
    OpenDialogToEdit(
        showDialog = showEditScoreDialog,
        onDismiss = { showEditScoreDialog = false },
        player = editPlayer,
        onConfirm = { newScore ->
            if (editPlayer != null) {
                viewModel.updatePlayer(editPlayer!!.copy(score = newScore))
                showEditScoreDialog = false
            }
        }
    )
}


@Composable
fun OpenDialogToEdit(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    player: PlayerModel?,
    onConfirm: (Int) -> Unit
) {
    if (showDialog && player != null) {
        var newScore by remember { mutableStateOf(player.score.toString()) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Edit Score") },
            text = {
                Column {
                    Text("Enter new score:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newScore,
                        onValueChange = { newScore = it },
                        label = { Text("Score") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val score = newScore.toIntOrNull()
                        if (score != null) {
                            onConfirm(score)
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
