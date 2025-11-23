package com.surya.portfolioadmin.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.surya.portfolioadmin.screens.*
import com.surya.portfolioadmin.viewmodel.CategoryViewModel
import com.surya.portfolioadmin.viewmodel.UpdateViewModel
import com.surya.portfolioadmin.viewmodel.WebsiteSettingsViewModel

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onThemeChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val categoryViewModel: CategoryViewModel = viewModel()
    val settingsViewModel: WebsiteSettingsViewModel = viewModel()
    val updateViewModel: UpdateViewModel = viewModel()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
            val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
            SplashScreen(onTimeout = {
                val destination = when {
                    isFirstLaunch -> "onboarding"
                    auth.currentUser != null -> "main"
                    else -> "login"
                }
                navController.navigate(destination) { popUpTo("splash") { inclusive = true } }
            })
        }
        composable("onboarding") {
            val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
            OnboardingScreen(onFinished = {
                sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
                navController.navigate("login") { popUpTo("onboarding") { inclusive = true } }
            })
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") { popUpTo("login") { inclusive = true } }
                },
                isDarkTheme = isDarkTheme
            )
        }
        composable("main") {
            MainScreen(
                onLogout = {
                    auth.signOut()
                    navController.navigate("login") { popUpTo("main") { inclusive = true } }
                },
                onAddProject = { navController.navigate("add_edit_project") },
                onEditProject = { projectId -> navController.navigate("add_edit_project?projectId=$projectId") },
                onAddCategory = { navController.navigate("add_edit_category") },
                onEditCategory = { categoryId -> navController.navigate("add_edit_category?categoryId=$categoryId") },
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                categoryViewModel = categoryViewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                updateViewModel = updateViewModel
            )
        }
        composable(
            route = "add_edit_project?projectId={projectId}",
            arguments = listOf(navArgument("projectId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            AddEditProjectScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "add_edit_category?categoryId={categoryId}",
            arguments = listOf(navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            AddEditCategoryScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() },
                categoryViewModel = categoryViewModel
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = settingsViewModel,
                onThemeChange = onThemeChange
            )
        }
    }
}
