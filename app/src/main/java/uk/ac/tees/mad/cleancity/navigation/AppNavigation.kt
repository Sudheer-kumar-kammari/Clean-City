package uk.ac.tees.mad.cleancity.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.cleancity.ui.screens.LoginScreen
import uk.ac.tees.mad.cleancity.ui.screens.MainScreen
import uk.ac.tees.mad.cleancity.ui.screens.SignUpScreen
import uk.ac.tees.mad.cleancity.ui.screens.SplashScreen
import uk.ac.tees.mad.cleancity.viewmodel.AuthViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {

    val navController = rememberNavController()
    val viewModel = hiltViewModel<AuthViewModel>()


    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {

        composable(route = Screen.SplashScreen.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.MainScreen.route){
                        popUpTo(Screen.SplashScreen.route){
                            inclusive=true
                        }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.LoginScreen.route){
                        popUpTo(Screen.SplashScreen.route){
                            inclusive=true
                        }
                    }
                },
                isAuthenticated = {
                    viewModel.isAuthenticated()
                }
            )
        }
        composable(
            route = Screen.SignUpScreen.route
        ) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.MainScreen.route) {
//                        popUpTo(Screen.LoginScreen.route) {
//                            inclusive = true
//                        }
                        popUpTo(0) { inclusive = true }  // Clear everything
                    }
                },
                viewModel = viewModel
            )
        }

        composable(route = Screen.LoginScreen.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUpScreen.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.MainScreen.route) {
//                        popUpTo(Screen.LoginScreen.route) {
//                            inclusive = true
//                        }
                        popUpTo(0) { inclusive = true }  // Clear everything
                    }
                },
                viewModel = viewModel
            )
        }

        composable(route= Screen.MainScreen.route){
            MainScreen()
        }



    }

}