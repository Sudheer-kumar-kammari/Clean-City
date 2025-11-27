package uk.ac.tees.mad.cleancity.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.cleancity.navigation.bottom_navigation.BottomNavScreen
import uk.ac.tees.mad.cleancity.navigation.bottom_navigation.bottomNavScreens
import uk.ac.tees.mad.cleancity.ui.screens.bottom_screens.AddReportScreen
import uk.ac.tees.mad.cleancity.ui.screens.bottom_screens.HomeScreen
import uk.ac.tees.mad.cleancity.ui.screens.bottom_screens.ProfileScreen
import uk.ac.tees.mad.cleancity.viewmodel.AddReportViewModel

@Composable
fun MainScreen(
    navigateToIssueScreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val bottomNavController = rememberNavController()
    val navBackStackEntry = bottomNavController.currentBackStackEntryAsState()
    val currentRoute =
        navBackStackEntry.value?.destination?.route ?: BottomNavScreen.Home.route

    val addReportViewModel= hiltViewModel<AddReportViewModel>()
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = {
                            Text(screen.title)
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(BottomNavScreen.Home.route) {
                HomeScreen(
                    navigateToIssueScreen = { reportId ->
                        navigateToIssueScreen(reportId)
                    }
                )
            }

            composable(BottomNavScreen.AddReport.route) {
                AddReportScreen(
                    viewModel = addReportViewModel
                )
            }

            composable(BottomNavScreen.Profile.route) {
                ProfileScreen()
            }
        }
    }

}