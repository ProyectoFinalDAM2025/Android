package leo.rios.officium.core.navigation

import androidx.compose.runtime.Composable
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
import kotlin.reflect.typeOf

@Composable
fun NavigationApp(){
    val navController = rememberNavController()
    val  viewModelLogin = LoginViewModel()
    NavHost(navController=navController, startDestination = Login)
    {
        composable<Login> {
            LoginScreen(
                navigationToHome = navController,
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