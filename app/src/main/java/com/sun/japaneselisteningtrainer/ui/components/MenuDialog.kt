package com.sun.japaneselisteningtrainer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog

class MenuItem(
    val title: String,
    val onClick: () -> Unit
)


@Composable
fun MenuDialog(
    onDismiss: () -> Unit,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(modifier = modifier) {
            Column {
                menuItems.forEach { it ->
                    TextButton(
                        onClick = {
                            it.onClick()
                        },
                        content = {
                            Text(text = it.title)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
