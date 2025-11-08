package uk.ac.tees.mad.cleancity.navigation

sealed class Screen(val route:String) {

    object SplashScreen:Screen("splash")
    object LoginScreen:Screen("login")
    object SignUpScreen:Screen("sign_up")
    object MainScreen:Screen("main")
}