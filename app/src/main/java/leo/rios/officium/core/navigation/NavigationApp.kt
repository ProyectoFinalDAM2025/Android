package leo.rios.officium.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import leo.rios.officium.core.navigation.type.createNavType
import leo.rios.officium.core.session.AuthState
import leo.rios.officium.desempleadoProfile.presentation.view.DesempleadoProfileScreen
import leo.rios.officium.desempleadoProfile.presentation.viewModel.DesempleadoProfileViewModel
import leo.rios.officium.detail.presentation.views.DetailScreen
import leo.rios.officium.empresaProfile.presentation.view.EmpresaProfileScreen
import leo.rios.officium.empresaProfile.presentation.viewModel.EmpresaProfileViewModel
import leo.rios.officium.home.presentation.views.HomeScreen
import leo.rios.officium.jobOfferDetail.presentation.view.JobOfferDetailScreen
import leo.rios.officium.jobOffers.presentation.view.JobOffersScreen
import leo.rios.officium.login.presentation.viewModel.LoginViewModel
import leo.rios.officium.login.presentation.views.LoginScreen
import leo.rios.officium.notifications.presentation.view.NotificationsScreen
import leo.rios.officium.publicationDetail.presentation.view.PublicationDetailScreen
import leo.rios.officium.recover.presentation.view.RecoverScreen
import leo.rios.officium.recover.presentation.viewModel.RecoverViewModel
import leo.rios.officium.registro.presentation.view.RegisterScreen
import leo.rios.officium.registro.presentation.viewModel.RegisterViewModel
import leo.rios.officium.search.presentation.view.SearchScreen
import leo.rios.officium.settings.presentation.views.SettingsScreen
import leo.rios.officium.splash.presentation.view.SplashScreen
import leo.rios.officium.subscriptions.presentation.view.SubscriptionsScreen
import leo.rios.officium.userProfile.presentation.view.UserProfileScreen
import leo.rios.officium.userProfile.presentation.viewModel.UserProfileViewModel
import leo.rios.officium.verificationCode.presentation.view.VerificationCodeScreen
import leo.rios.officium.verificationCode.presentation.viewModel.VerificationCodeViewModel
import leo.rios.officium.verifyProfile.presentation.view.VerifyProfileScreen
import kotlin.reflect.typeOf
import kotlinx.coroutines.delay



@Composable
fun NavigationApp(){
    val navController = rememberNavController()
    val viewModelLogin : LoginViewModel = hiltViewModel()
    val viewModelRegister: RegisterViewModel = hiltViewModel()
    val viewModelRecover: RecoverViewModel = hiltViewModel()
    val viewModelVerificationCode: VerificationCodeViewModel = hiltViewModel()
    val authState by viewModelLogin.authState.collectAsState()
    val token by viewModelLogin.token.collectAsState()
    val profilePhoto by viewModelLogin.profilePhoto.collectAsState()
    val profileRole by viewModelLogin.profileRole.collectAsState()
    val isChekingToken by viewModelLogin.isCheckingToken.collectAsState()

    LaunchedEffect(Unit) {
        viewModelLogin.checkAuthStatus()
    }

    NavHost(navController=navController, startDestination = Splash)
    {
        composable<Splash> {
            SplashScreen()

            LaunchedEffect(isChekingToken, token, authState) {
                if (!isChekingToken) {

                    delay(5000)
                    val destination = when (authState) {
                        AuthState.LOGGED_OUT -> Login
                        AuthState.EMAIL_PENDING -> Login // temporal hasta tener email/id para VerificationCode
                        AuthState.PROFILE_PENDING -> Login // temporal hasta conectar VerifyProfile bien
                        AuthState.AUTHENTICATED -> Home
                    }

                    navController.navigate(destination) {
                        popUpTo<Splash> { inclusive = true }
                        launchSingleTop = true
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
        composable<Register> {
            RegisterScreen(
                navigateTo = navController,
                viewModel = viewModelRegister
            )
        }
        composable<Recover> {
            RecoverScreen(
                navigateTo = navController,
                viewModel = viewModelRecover
            )
        }
        composable<VerificationCode>(
            typeMap = mapOf(typeOf<VerificationData>() to createNavType<VerificationData>())
        ){ navBackStackEntry ->
            val verificationCode = navBackStackEntry.toRoute<VerificationCode>()
            VerificationCodeScreen(
                verificationData = verificationCode.verificationData,
                viewModel = viewModelVerificationCode,
                navigateTo = navController
            )
        }
        composable<VerifyProfile>(
            typeMap = mapOf(typeOf<VerifyData>() to createNavType<VerifyData>())
        ){ navBackStackEntry ->
            val verifyProfile = navBackStackEntry.toRoute<VerifyProfile>()
            VerifyProfileScreen(
                verifyData = verifyProfile.verifyData,
                navigateTo = navController
            )

        }
        composable<VerifyUnemployedProfile>(
            typeMap = mapOf(typeOf<VerifyData>() to createNavType<VerifyData>())
        ){ navBackStackEntry ->
            val verifyProfile = navBackStackEntry.toRoute<VerifyUnemployedProfile>()
            DesempleadoProfileScreen(
                verifyData = verifyProfile.verifyData,
                viewModel = hiltViewModel<DesempleadoProfileViewModel>(),
                navigateTo = navController
            )
        }
        composable<VerifyCompanyProfile>(
            typeMap = mapOf(typeOf<VerifyData>() to createNavType<VerifyData>())
        ){ navBackStackEntry ->
            val verifyProfile = navBackStackEntry.toRoute<VerifyCompanyProfile>()
            EmpresaProfileScreen(
                verifyData = verifyProfile.verifyData,
                viewModel = hiltViewModel<EmpresaProfileViewModel>(),
                navigateTo = navController
            )
        }
        composable<Home>{
            LaunchedEffect(authState) {
                if (authState == AuthState.LOGGED_OUT) {
                    navController.navigate(Login) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            HomeScreen(
                profilePhoto = profilePhoto,
                profileRole = profileRole,
                navigateToDetail = { name -> navController.navigate(Detail(name = name)) },
                onProfileClick = { navController.navigate(Profile()) },
                onSecondClick = {
                    if (profileRole == "Empresa") {
                        navController.navigate(JobOffers)
                    } else {
                        navController.navigate(Subscriptions)
                    }
                },
                onNotificationsClick = { navController.navigate(Notifications) },
                onSearchClick = { navController.navigate(Search) },
                onUserProfileClick = { idUsuario -> navController.navigate(Profile(idUsuario = idUsuario)) },
                onLogout = {
                    viewModelLogin.logout {
                        navController.navigate(Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable<Profile> { navBackStackEntry ->
            val profileRoute = navBackStackEntry.toRoute<Profile>()
            LaunchedEffect(authState) {
                if (authState == AuthState.LOGGED_OUT) {
                    navController.navigate(Login) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            UserProfileScreen(
                viewModel = hiltViewModel<UserProfileViewModel>(),
                profileUserId = profileRoute.idUsuario,
                onHomeClick = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSecondClick = {
                    if (profileRole == "Empresa") {
                        navController.navigate(JobOffers)
                    } else {
                        navController.navigate(Subscriptions)
                    }
                },
                onNotificationsClick = { navController.navigate(Notifications) },
                onSearchClick = { navController.navigate(Search) },
                onMyProfileClick = {
                    navController.navigate(Profile()) {
                        launchSingleTop = true
                    }
                },
                onUserProfileClick = { idUsuario ->
                    navController.navigate(Profile(idUsuario = idUsuario)) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    viewModelLogin.logout {
                        navController.navigate(Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable<JobOffers> {
            JobOffersScreen(
                onBackClick = { navController.navigateUp() },
                onHomeClick = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSecondClick = {
                    navController.navigate(JobOffers) {
                        launchSingleTop = true
                    }
                },
                onNotificationsClick = { navController.navigate(Notifications) },
                onSearchClick = { navController.navigate(Search) },
                onProfileClick = {
                    navController.navigate(Profile()) {
                        launchSingleTop = true
                    }
                },
                onUserProfileClick = { idUsuario ->
                    navController.navigate(Profile(idUsuario = idUsuario)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<Notifications> {
            NotificationsScreen(
                onBackClick = { navController.navigateUp() },
                profilePhoto = profilePhoto,
                profileRole = profileRole,
                onHomeClick = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSecondClick = {
                    if (profileRole == "Empresa") {
                        navController.navigate(JobOffers)
                    } else {
                        navController.navigate(Subscriptions)
                    }
                },
                onNotificationsClick = {
                    navController.navigate(Notifications) {
                        launchSingleTop = true
                    }
                },
                onSearchClick = { navController.navigate(Search) },
                onProfileClick = {
                    navController.navigate(Profile()) {
                        launchSingleTop = true
                    }
                },
                onPublicationNotificationClick = { idPublicacion ->
                    navController.navigate(PublicationDetail(idPublicacion))
                },
                onJobOfferNotificationClick = { idOferta ->
                    navController.navigate(JobOfferDetail(idOferta))
                }
            )
        }
        composable<PublicationDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<PublicationDetail>()
            PublicationDetailScreen(
                publicationId = detail.idPublicacion,
                profilePhoto = profilePhoto,
                profileRole = profileRole,
                onBackClick = { navController.navigateUp() },
                onHomeClick = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSecondClick = {
                    if (profileRole == "Empresa") {
                        navController.navigate(JobOffers)
                    } else {
                        navController.navigate(Subscriptions)
                    }
                },
                onNotificationsClick = { navController.navigate(Notifications) },
                onSearchClick = { navController.navigate(Search) },
                onProfileClick = {
                    navController.navigate(Profile()) {
                        launchSingleTop = true
                    }
                },
                onAuthorClick = { idUsuario ->
                    navController.navigate(Profile(idUsuario = idUsuario)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<JobOfferDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<JobOfferDetail>()
            JobOfferDetailScreen(
                offerId = detail.idOferta,
                profilePhoto = profilePhoto,
                profileRole = profileRole,
                onBackClick = { navController.navigateUp() },
                onHomeClick = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSecondClick = {
                    if (profileRole == "Empresa") {
                        navController.navigate(JobOffers)
                    } else {
                        navController.navigate(Subscriptions)
                    }
                },
                onNotificationsClick = { navController.navigate(Notifications) },
                onSearchClick = { navController.navigate(Search) },
                onProfileClick = {
                    navController.navigate(Profile()) {
                        launchSingleTop = true
                    }
                },
                onUserProfileClick = { idUsuario ->
                    navController.navigate(Profile(idUsuario = idUsuario)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<Subscriptions> {
            SubscriptionsScreen(
                onBackClick = { navController.navigateUp() },
                profilePhoto = profilePhoto,
                profileRole = profileRole,
                onHomeClick = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSecondClick = {
                    navController.navigate(Subscriptions) {
                        launchSingleTop = true
                    }
                },
                onNotificationsClick = { navController.navigate(Notifications) },
                onSearchClick = { navController.navigate(Search) },
                onProfileClick = {
                    navController.navigate(Profile()) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<Search> {
            SearchScreen(
                onBackClick = { navController.navigateUp() },
                onHomeClick = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSecondClick = {
                    if (profileRole == "Empresa") {
                        navController.navigate(JobOffers)
                    } else {
                        navController.navigate(Subscriptions)
                    }
                },
                onNotificationsClick = { navController.navigate(Notifications) },
                onProfileClick = {
                    navController.navigate(Profile()) {
                        launchSingleTop = true
                    }
                },
                onUserProfileClick = { idUsuario ->
                    navController.navigate(Profile(idUsuario = idUsuario)) {
                        launchSingleTop = true
                    }
                }
            )
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
