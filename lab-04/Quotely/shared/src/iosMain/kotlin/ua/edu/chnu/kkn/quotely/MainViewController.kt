package ua.edu.chnu.kkn.quotely

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import ua.edu.chnu.kkn.quotely.di.initKoinIfNeeded

fun MainViewController(): UIViewController {
    initKoinIfNeeded()
    return ComposeUIViewController { App() }
}
