package uk.ac.tees.mad.cleancity.navigation.bottom_navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavScreen("home", "Home", Icons.Default.Home)
    object AddReport : BottomNavScreen("add", "Add", Icons.Default.Add)
    object Profile : BottomNavScreen("profile", "Profile", Icons.Default.Person)
}

val bottomNavScreens = listOf(
    BottomNavScreen.Home,
    BottomNavScreen.AddReport,
    BottomNavScreen.Profile
)