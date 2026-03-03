@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.notes.presentation.screen.creations

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notes.R
import com.example.notes.presentation.Content
import com.example.notes.presentation.ui.theme.CustomIcons
import com.example.notes.presentation.utils.DataFormatter

@Composable
fun CreateNoteScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateNoteViewModel = hiltViewModel(),
    onFinished: () -> Unit,
) {

    val state = viewModel.state.collectAsState()
    val currentState = state.value

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {uri ->
            uri?.let {
                viewModel.processCommands(CreateNoteCommand.AddImage(it))
            }
        }
    )
    when (currentState) {
        is CreateNoteState.Creation -> {
            Scaffold(
                modifier = modifier,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.create_note),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            Icon(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 8.dp)
                                    .clickable {
                                        viewModel.processCommands(command = CreateNoteCommand.GoBack)
                                    },
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        },
                        actions = {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .clickable {
                                        imagePicker.launch("image/*")
                                    },
                                imageVector = CustomIcons.AddPhoto,
                                contentDescription = "Add photo",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        value = currentState.title,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.title_create_note),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            )
                        },
                        onValueChange = {
                            viewModel.processCommands(command = CreateNoteCommand.InputTitle(it))
                        }
                    )
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 24.dp),
                        text = DataFormatter.formatCurrentDate(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Content(
                      modifier = Modifier
                          .weight(1f),
                        content = currentState.content,
                        onTextChange = {index, content ->
                            viewModel.processCommands(CreateNoteCommand.InputContent(content, index))
                        },
                        onExitClick = {
                            viewModel.processCommands(CreateNoteCommand.DeleteImage(it))
                        }
                    )
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = currentState.isSaveEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = {
                            viewModel.processCommands(command = CreateNoteCommand.Save)
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.save_note_create_note)
                        )
                    }

                }
            }
        }
        
        CreateNoteState.Finished -> {
            LaunchedEffect(key1 = Unit) {
                onFinished()
            }
        }
    }
}
