package com.surya.portfolioadmin.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.surya.portfolioadmin.R
import com.surya.portfolioadmin.navigation.BottomNavItem
import com.surya.portfolioadmin.navigation.CurvedBottomNavigation
import com.surya.portfolioadmin.viewmodel.AssetViewModel
import com.surya.portfolioadmin.viewmodel.CategoryViewModel
import com.surya.portfolioadmin.viewmodel.DashboardViewModel
import com.surya.portfolioadmin.viewmodel.UpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onLogout: () -> Unit,
    onAddProject: () -> Unit,
    onEditProject: (String) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (String) -> Unit,
    onThemeToggle: () -> Unit,
    categoryViewModel: CategoryViewModel,
    onNavigateToSettings: () -> Unit,
    updateViewModel: UpdateViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel()
    val assetViewModel: AssetViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.favicon),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Text("Admin Panel", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Conditionally show the Add button in the TopAppBar
                    if (currentRoute == BottomNavItem.Projects.route || currentRoute == BottomNavItem.Categories.route) {
                        IconButton(onClick = {
                            if (currentRoute == BottomNavItem.Projects.route) {
                                onAddProject()
                            } else {
                                onAddCategory()
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item")
                        }
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {
                        val url = "https://sharmila-portfolio.web.app"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Language, contentDescription = "Visit Website")
                    }
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            CurvedBottomNavigation(
                navController = navController,
                items = listOf(
                    BottomNavItem.Projects,
                    BottomNavItem.Categories,
                    BottomNavItem.Assets,
                    BottomNavItem.Update
                )
            )
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Projects.route,
            Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Projects.route) {
                ProjectScreen(
                    dashboardViewModel = dashboardViewModel,
                    onEditProject = onEditProject,
                    onDeleteProject = { project ->
                        dashboardViewModel.deleteProject(project)
                    }
                )
            }
            composable(BottomNavItem.Categories.route) {
                CategoryScreen(
                    categoryViewModel = categoryViewModel,
                    onEditCategory = onEditCategory
                )
            }
            composable(BottomNavItem.Assets.route) {
                AssetManagerScreen(assetViewModel = assetViewModel)
            }
            composable(BottomNavItem.Update.route) {
                UpdateScreen(updateViewModel = updateViewModel)
            }
        }
    }
}
