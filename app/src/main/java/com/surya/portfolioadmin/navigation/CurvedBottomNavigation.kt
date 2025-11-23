package com.surya.portfolioadmin.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun CurvedBottomNavigation(
    navController: NavController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedIndex = remember(currentRoute) {
        items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    val animatedSelectedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = 400),
        label = "selectedIndex"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Transparent)
    ) {
        val backgroundColor = MaterialTheme.colorScheme.surface
        val density = LocalDensity.current

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val itemWidth = width / items.size
            val curveRadius = with(density) { 32.dp.toPx() }
            val fabOffset = (animatedSelectedIndex * itemWidth) + (itemWidth / 2)

            val path = Path().apply {
                moveTo(0f, curveRadius)
                quadraticBezierTo(0f, 0f, curveRadius, 0f)
                lineTo(fabOffset - curveRadius * 1.5f, 0f)
                cubicTo(
                    fabOffset - curveRadius * 0.8f, 0f,
                    fabOffset - curveRadius * 0.6f, height * 0.3f,
                    fabOffset, height * 0.3f
                )
                cubicTo(
                    fabOffset + curveRadius * 0.6f, height * 0.3f,
                    fabOffset + curveRadius * 0.8f, 0f,
                    fabOffset + curveRadius * 1.5f, 0f
                )
                lineTo(width - curveRadius, 0f)
                quadraticBezierTo(width, 0f, width, curveRadius)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(path, color = backgroundColor)
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Top
        ) {
            items.forEachIndexed { index, item ->
                CurvedNavigationItem(
                    item = item,
                    isSelected = index == selectedIndex,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CurvedNavigationItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        animationSpec = tween(300),
        label = "contentColor"
    )

    // --- FIX 1: RAISE THE ICONS ---
    // Unselected icons are now raised much higher for better alignment and touchability.
    val yOffset by animateDpAsState(
        targetValue = if (isSelected) (-20).dp else 0.dp,
        animationSpec = tween(300),
        label = "yOffset"
    )

    Box(
        modifier = Modifier
            .offset(y = yOffset)
            .size(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // --- FIX 2: HIDE TOUCH RIPPLE ---
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        // --- FIX 3: MORE ANIMATED ICONS ---
        // This composable animates the transition between the selected and unselected icons.
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn(animationSpec = tween(200)))
                    .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(200)))
            },
            label = "iconAnimation"
        ) { targetIsSelected ->
            Icon(
                imageVector = if (targetIsSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
