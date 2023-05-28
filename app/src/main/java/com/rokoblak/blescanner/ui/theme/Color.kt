package com.rokoblak.blescanner.ui.theme

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val PrimaryDark = Color.DarkGray
val PrimaryLight = Color(0xFFD6D6EC)

val SecondaryLight = Color.DarkGray
val SecondaryDark = Color.LightGray

fun Color.alpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Color = this.copy(alpha = alpha)
fun Color.alpha(@IntRange(from = 0, to = 100) alpha: Int): Color =
    this.copy(alpha = alpha.toFloat().div(100f))
