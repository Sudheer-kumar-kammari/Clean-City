package uk.ac.tees.mad.cleancity.navigation

sealed class Screen(val route:String) {


    // auth screen
    object SplashScreen:Screen("splash")
    object LoginScreen:Screen("login")
    object SignUpScreen:Screen("sign_up")

    // main screen
    object MainScreen:Screen("main")

    object IssueDetailScreen:Screen("detail/{reportId}"){
        fun createRoute(reportId:String)="detail/$reportId"
    }
}