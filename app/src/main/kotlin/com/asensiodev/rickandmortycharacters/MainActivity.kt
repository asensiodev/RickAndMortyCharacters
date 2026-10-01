package com.asensiodev.rickandmortycharacters

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import coil3.SingletonImageLoader
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState = savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val imageLoader = SingletonImageLoader.get(this)
        setContent {
            RickAndMortyTheme {
                AppNavigation(imageLoader = imageLoader)
            }
        }
    }
}
