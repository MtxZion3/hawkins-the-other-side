package com.example.hawkins

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import com.example.hawkins.ui.room.RoomScreen
import com.example.hawkins.ui.theme.HawkinsTheme
import com.pico.spatial.ui.foundation.dsl.DefaultWindowContainer
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope
import com.pico.spatial.ui.platform.LocalPhysicalLengthConverter
import com.pico.spatial.ui.platform.LengthUnit

fun mainApp(scope: SpatialAppScope) = with(scope) {
    DefaultWindowContainer {
        val converter = LocalPhysicalLengthConverter.current
        Box(Modifier.windowConstraints(
            minWidth = converter.lengthToDp(1.12f, LengthUnit.Meters),
            minHeight = converter.lengthToDp(.64f, LengthUnit.Meters),
            minDepth = converter.lengthToDp(.64f, LengthUnit.Meters),
            maxWidth = converter.lengthToDp(1.68f, LengthUnit.Meters),
            maxHeight = converter.lengthToDp(.96f, LengthUnit.Meters),
            maxDepth = converter.lengthToDp(.96f, LengthUnit.Meters),
        )) { HawkinsTheme { RoomScreen() } }
    }
}
