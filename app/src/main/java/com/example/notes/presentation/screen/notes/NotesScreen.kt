package com.example.notes.presentation.screen.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.notes.R
import com.example.notes.domain.ContentItem
import com.example.notes.domain.Note
import com.example.notes.presentation.ui.theme.OtherNotesColors
import com.example.notes.presentation.ui.theme.PinnedNotesColors
import com.example.notes.presentation.utils.DataFormatter
import okhttp3.internal.ignoreIoExceptions

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
    onNoteClick: (Note) -> Unit,
    onActionButtonClick: () -> Unit
) {

    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape ,
                onClick = {
                    onActionButtonClick()
                },
            ) {
                Icon(painter = painterResource(R.drawable.ic_add_note), contentDescription = "add note button")
            }
        }
    ) {innerPadding ->
        LazyColumn(
            contentPadding = innerPadding)
        {
            item {
                Title(
                    Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.all_notes)
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                SearchBar(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    query = state.query,
                    onQueryChange = {
                        viewModel.processCommands(NotesCommand.InputSearchQuery(it))
                    }
                )
            }


            if (state.pinnedNotes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    SubTitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.pinned)
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    LazyRow(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {

                        itemsIndexed(state.pinnedNotes, key = { _, note -> note.id })
                        { index, note ->
                            NoteCard(
                                Modifier.widthIn(max = 160.dp),
                                note = note,
                                onNoteClick = {
                                    onNoteClick(it)
                                },
                                onLongClick = {
                                    viewModel.processCommands(NotesCommand.SwitchPinnedStatus(note.id))
                                },
                                backgroundColor = PinnedNotesColors[index % PinnedNotesColors.size]
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
            }

            item {
                SubTitle(
                    Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.others)
                )
            }


            if (state.otherNotes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                }
                itemsIndexed(state.otherNotes, key = { _, note -> note.id })
                { index, note ->
                    val imageUrl = note.content.filterIsInstance<ContentItem.Image>().map { it.url }.firstOrNull()
                    if (imageUrl == null) {
                        NoteCard(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            note = note,
                            onNoteClick = {
                                onNoteClick(it)
                            },
                            onLongClick = {
                                viewModel.processCommands(NotesCommand.SwitchPinnedStatus(note.id))
                            },
                            backgroundColor = OtherNotesColors[index % OtherNotesColors.size]
                        )
                        Spacer(Modifier.height(8.dp))
                    } else {
                        NoteCardWithImage(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            note = note,
                            onNoteClick = {
                                onNoteClick(it)
                            },
                            onLongClick = {
                                viewModel.processCommands(NotesCommand.SwitchPinnedStatus(note.id))
                            },
                            imageUrl = imageUrl,
                            backgroundColor = OtherNotesColors[index % OtherNotesColors.size]
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            } else item {
                IsEmpty(modifier = Modifier.padding(start = 24.dp), text = stringResource(R.string.add_your_first_note))
            }
        }
    }
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .background(MaterialTheme.colorScheme.surface),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(R.string.search),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Notes",
                tint = MaterialTheme.colorScheme.onSurface)
        },
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun SubTitle(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Title(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun IsEmpty(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        text = text
    )
}

@Composable
fun NoteCardWithImage(
    modifier: Modifier = Modifier,
    note: Note,
    imageUrl: String,
    backgroundColor: Color,
    onNoteClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    onNoteClick(note)
                },
                onLongClick = {
                    onLongClick(note)
                },
            )

    ) {
        Box {
            AsyncImage(
                modifier = Modifier
                    .heightIn(max = 120.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                model = imageUrl,
                contentDescription = "First image of the note",
                contentScale = ContentScale.FillWidth
            )
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.onSurface
                            )
                        )
                    )
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = note.name,
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onPrimary,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = DataFormatter.formatDateToString(note.updatedAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        note.content.filterIsInstance<ContentItem.Text>()
            .filter { it.content.isNotBlank() }
            .joinToString("\n") {
            it.content
        }.takeIf { it.isNotBlank() }
                ?.let {
            Text(
                modifier = Modifier.padding(16.dp),
                text = it,
                fontSize = 16.sp,
                maxLines = 3,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    backgroundColor: Color,
    onNoteClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    onNoteClick(note)
                },
                onLongClick = {
                    onLongClick(note)
                },
            )
            .padding(16.dp),
    ) {
                Text(
                    text = note.name,
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = DataFormatter.formatDateToString(note.updatedAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

        note.content.filterIsInstance<ContentItem.Text>()
            .filter { it.content.isNotBlank() }
            .joinToString("\n") {
                it.content
            }.takeIf { it.isNotBlank() }
            ?.let {
                Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = it,
                fontSize = 16.sp,
                maxLines = 3,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}