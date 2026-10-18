package com.creativeblisslabs.nuxmg101.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeblisslabs.nuxmg101.ui.theme.CreamPrimary

enum class StudioNavRoute(val label: String, val icon: ImageVector) {
    SIGNAL("Signal", Icons.Default.Tune),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    TUNER("Tuner", Icons.Default.Vibration),
    DRUM_LOOP("Drum", Icons.Default.Loop),
    HARDWARE("Hardware", Icons.Default.DeveloperBoard),
    CLOUD_CONV("Cloud", Icons.Default.Cloud),
    SYSTEM("System", Icons.Default.Settings)
}

@Composable
fun BottomStudioNav(
    currentRoute: StudioNavRoute,
    onNavigate: (StudioNavRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayRoutes = listOf(
        StudioNavRoute.SIGNAL,
        StudioNavRoute.LIBRARY,
        StudioNavRoute.TUNER,
        StudioNavRoute.DRUM_LOOP,
        StudioNavRoute.HARDWARE,
        StudioNavRoute.CLOUD_CONV,
        StudioNavRoute.SYSTEM
    )

    // Pull from MaterialTheme so it responds correctly to light/dark themes
    val activeColor = MaterialTheme.colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        displayRoutes.forEach { route ->
            val isSelected = route == currentRoute
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) activeColor.copy(alpha = 0.18f) else Color.Transparent)
                    .clickable { onNavigate(route) }
                    .padding(vertical = 6.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = route.icon,
                        contentDescription = route.label,
                        tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = route.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}