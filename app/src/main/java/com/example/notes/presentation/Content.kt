package com.example.notes.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.notes.R
import com.example.notes.domain.ContentItem

@Composable fun Content(
    modifier: Modifier = Modifier,
    content: List<ContentItem>,
    onExitClick: (Int) -> Unit,
    onTextChange: (Int, String) -> Unit
) {
    LazyColumn (
        modifier = modifier
    ) {
        content.forEachIndexed {index, contentItem ->
            item(key = index) {
                when (contentItem) {
                    is ContentItem.Image -> {
                        val isAlreadyTaken = index > 0 && content[index-1] is ContentItem.Image
                        content
                            .takeIf { !isAlreadyTaken }
                            ?.drop(index)
                            ?.takeWhile {
                                it is ContentItem.Image
                            }
                            ?.map {
                                (it as ContentItem.Image).url
                            }?.let {urls ->
                                ImageGroup(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    imageUrls = urls,
                                    onExitClick = { imageIndex ->
                                        onExitClick(index + imageIndex)
                                    }
                                )
                            }
                    }
                    is ContentItem.Text -> {
                        TextContent(
                            text = contentItem.content,
                            onTextChange = {
                                onTextChange(index, it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable fun ImageGroup(
    modifier: Modifier = Modifier,
    imageUrls: List<String>,
    onExitClick: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imageUrls.forEachIndexed { index, url ->
            ImageContent(
                modifier = Modifier
                    .weight(1f),
                imageUrl = url,
                onExitClick = {
                    onExitClick(index)
                }
            )
        }
    }
}

@Composable fun ImageContent(
    modifier: Modifier = Modifier,
    imageUrl: String,
    onExitClick: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            model = imageUrl,
            contentDescription = "image from gallery",
            contentScale = ContentScale.FillWidth
        )
        Icon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
                .clickable {
                    onExitClick()
                },
            imageVector = Icons.Default.Close,
            contentDescription = "close button",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable fun TextContent(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        value = text,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        placeholder = {
            Text(
                text = stringResource(R.string.note_something_below),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            )
        },
        onValueChange = onTextChange
    )
}