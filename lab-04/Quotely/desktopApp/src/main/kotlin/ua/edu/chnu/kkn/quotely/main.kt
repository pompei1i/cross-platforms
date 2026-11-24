package ua.edu.chnu.kkn.quotely

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ua.edu.chnu.kkn.quotely.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Quotely",
            state = rememberWindowState(size = DpSize(420.dp, 860.dp)),
        ) {
            App()
        }
    }
}
