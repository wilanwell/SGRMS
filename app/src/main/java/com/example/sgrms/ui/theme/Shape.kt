package com.example.sgrms.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Corner radii: icon tile 12, field/button 14, card 20, hero 24. */
val SgrmsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object SgrmsRadius {
    val IconTile = 12.dp
    val Field = 14.dp
    val Card = 20.dp
    val Hero = 24.dp
    val NavBar = 22.dp
}

object SgrmsSpacing {
    val ScreenPadding = 20.dp
    val CardGap = 12.dp
    val SectionGap = 20.dp
}
