package org.hoshi.reshelper.widget

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
private fun SingleConfirmDialog(
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = { onConfirmation() }
            ) {
                Text("好的")
            }
        },
    )
}

@Composable
fun SingleConfirmDialog(state: MutableState<Pair<String, String>?>) {
    val stateValue = state.value
    when {
        stateValue != null -> {
            SingleConfirmDialog(
                onConfirmation = { state.value = null },
                dialogTitle = stateValue.first,
                dialogText = stateValue.second,
            )
        }
    }
}