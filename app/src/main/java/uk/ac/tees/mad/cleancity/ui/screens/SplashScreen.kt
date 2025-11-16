package uk.ac.tees.mad.cleancity.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colors for Splash Screen - Eco-friendly Green Theme
private val GreenPrimary = Color(0xFF4CAF50)
private val GreenDark = Color(0xFF2E7D32)
private val GreenDarker = Color(0xFF1B5E20)
private val White = Color(0xFFFFFFFF)
private val WhiteTransparent = Color(0xE6FFFFFF) // 90% opacity
private val WhiteSemiTransparent = Color(0xB3FFFFFF) // 70% opacity

@Composable
fun SplashScreen(
    isAuthenticated:()->Boolean,
    onNavigateToHome: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {}
) {
    var isCheckingAuth by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Animation values
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val dotsAlpha = remember { Animatable(0f) }  // NEW: Separate animation for dots

    // Start animations on launch - ALL IN PARALLEL
    LaunchedEffect(Unit) {
        // Launch all animations in parallel using separate coroutines
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }

        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    delayMillis = 300
                )
            )
        }

        launch {
            // Dots fade in AFTER text (delayed start)
            dotsAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 800  // Start after text begins
                )
            )
        }
    }

    // Check Firebase Authentication Status
    LaunchedEffect(Unit) {
        scope.launch {
            // Show splash for minimum 2.5 seconds
            delay(2500)

            // Check if user is logged in
//            val currentUser = FirebaseAuth.getInstance().currentUser

            isCheckingAuth = false

            // Navigate based on auth status
            if (isAuthenticated()) {
                onNavigateToHome()
            } else {
                onNavigateToAuth()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GreenPrimary,
                        GreenDark,
                        GreenDarker
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // App Icon with circular background
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .background(
                        color = White.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Recycling,
                    contentDescription = "CleanCity Logo",
                    tint = White,
                    modifier = Modifier.size(140.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name
            Text(
                text = "CleanCity",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline - "Keep Your City Clean"
            Text(
                text = "Keep Your City Clean",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = WhiteTransparent,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading Indicator - NOW WITH FADE IN!
            if (isCheckingAuth) {
                LoadingDots(
                    modifier = Modifier.alpha(dotsAlpha.value)  // Apply fade-in alpha
                )
            }
        }

        // Bottom Message
        Text(
            text = "Making our environment better",
            fontSize = 14.sp,
            color = WhiteSemiTransparent,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(textAlpha.value)
        )
    }
}

@Composable
private fun LoadingDots(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    // Animate three dots with staggered delays
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1Scale"
    )

    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2Scale"
    )

    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3Scale"
    )

    Row(
        modifier = modifier,  // Alpha applied from parent
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoadingDot(scale = dot1Scale)
        LoadingDot(scale = dot2Scale)
        LoadingDot(scale = dot3Scale)
    }
}

@Composable
private fun LoadingDot(scale: Float) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .background(
                color = White,
                shape = CircleShape
            )
    )
}

//@Preview(showBackground = true)
//@Composable
//private fun SplashScreenPreview() {
//    SplashScreen()
//}