package io.posa

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.posa.feature.breeds.BreedsScreen
import io.posa.feature.favourites.FavouritesScreen
import io.posa.theme.AppTheme

private sealed class Screen(val route: String) {
    data object Breeds : Screen("breeds")
    data object Favourites : Screen("favourites")
}

private const val NAV_ANIM_DURATION_MS = 380

@Composable
internal fun App() {
    val navController = rememberNavController()

    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Breeds.route,
            ) {
                composable(
                    route = Screen.Breeds.route,
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(NAV_ANIM_DURATION_MS),
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(NAV_ANIM_DURATION_MS),
                        )
                    },
                ) {
                    BreedsScreen(
                        onNavigateToFavourites = {
                            navController.navigate(Screen.Favourites.route)
                        },
                    )
                }

                composable(
                    route = Screen.Favourites.route,
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(NAV_ANIM_DURATION_MS),
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(NAV_ANIM_DURATION_MS),
                        )
                    },
                ) {
                    FavouritesScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}