package leo.rios.officium.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import leo.rios.officium.core.navigation.type.createNavType
import leo.rios.officium.detail.presentation.views.DetailScreen
import leo.rios.officium.home.presentation.views.HomeScreen
import leo.rios.officium.login.presentation.viewModel.LoginViewModel
import leo.rios.officium.login.presentation.views.LoginScreen
import leo.rios.officium.settings.presentation.views.SettingsScreen
import leo.rios.officium.splash.presentation.view.SplashScreen
import kotlin.reflect.typeOf

@Composable
fun NavigationApp(){
    val navController = rememberNavController()
    val  viewModelLogin : LoginViewModel = viewModel()
    val authState by viewModelLogin.authState.collectAsState()
    val token by viewModelLogin.authState.collectAsState()
    val isChekingToken by viewModelLogin.isCheckingToken.collectAsState()

    LaunchedEffect(Unit) {
        viewModelLogin.checkAuthStatus()
    }


    NavHost(navController=navController, startDestination = Splash)
    {
        composable<Splash> {
            when{
                isChekingToken -> {
                    SplashScreen()
                }
                !token.isNullOrEmpty() && authState == "Token valido encontrado" -> {
                    navController.navigate(Login){
                        popUpTo<Splash>{inclusive = true}
                    }
                }
                else -> {
                    navController.navigate(Home){
                        popUpTo<Splash>{inclusive = true}
                    }
                }
            }
        }

        composable<Login> {
            LoginScreen(
                navigationTo = navController,
                viewModel = viewModelLogin
            )
        }
        composable<Home>{
            HomeScreen{ name -> navController.navigate(Detail(name = name))}
        }
        composable<Detail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Detail>()
            DetailScreen(
                detail.name,
                navigateBack = {navController.navigate(Login){
                    popUpTo<Login>{inclusive = true}
                    //En false limpia hasta el login sin borrar el login
                    //o navigate.navigateUp() cuando se habre otra aplicacion
                    //navigate.popBackStack() vuelve el stack anterior
                }},
                navigateToSettings = { navController.navigate(Settings(it))},
            )
        }
        composable<Settings>(
            typeMap = mapOf(typeOf<SettingsInfo>() to createNavType<SettingsInfo>())) { backStackEntry ->
            val settings : Settings = backStackEntry.toRoute()
            SettingsScreen(settings.info)
        }
    }
}