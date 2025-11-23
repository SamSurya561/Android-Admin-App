package com.surya.portfolioadmin.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Projects : BottomNavItem(
        route = "projects",
        label = "Projects",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )
    object Categories : BottomNavItem(
        route = "categories",
        label = "Categories",
        selectedIcon = Icons.Filled.Category,
        unselectedIcon = Icons.Outlined.Category
    )
    object Assets : BottomNavItem(
        route = "assets",
        label = "Assets",
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder
    )
    object Update : BottomNavItem(
        route = "update",
        label = "Update",
        selectedIcon = Icons.Filled.SystemUpdate,
        unselectedIcon = Icons.Outlined.SystemUpdate
    )
}
