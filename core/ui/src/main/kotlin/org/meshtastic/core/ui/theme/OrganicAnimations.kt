package org.meshtastic.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

const val ORGANIC_DURATION_SHORT = 2000
const val ORGANIC_DURATION_MEDIUM = 3500
const val ORGANIC_DURATION_LONG = 5000
const val ORGANIC_DURATION_EXTRA_LONG = 7000

const val ORGANIC_STAGGER_DELAY = 600
const val ORGANIC_STAGGER_DELAY_SHORT = 400

val OrganicEasing: Easing = CubicBezierEasing(0.8f, 0.0f, 0.9f, 0.1f)

val OrganicEmphasizedEasing: Easing = CubicBezierEasing(0.95f, 0.05f, 0.95f, 0.05f)

val OrganicGentleEasing: Easing = CubicBezierEasing(0.5f, 0.0f, 0.5f, 1.0f)

fun <T> organicSpring() = spring<T>(
    dampingRatio = Spring.DampingRatioHighBouncy,
    stiffness = Spring.StiffnessVeryLow
)

fun <T> organicGentleSpring() = spring<T>(
    dampingRatio = 0.3f,
    stiffness = Spring.StiffnessVeryLow
)

fun <T> organicEmphasizedSpring() = spring<T>(
    dampingRatio = 0.2f,
    stiffness = 50f
)

fun <T> organicTween() = tween<T>(
    durationMillis = ORGANIC_DURATION_MEDIUM,
    easing = OrganicEasing
)

fun <T> organicTweenShort() = tween<T>(
    durationMillis = ORGANIC_DURATION_SHORT,
    easing = OrganicEasing
)

fun <T> organicTweenLong() = tween<T>(
    durationMillis = ORGANIC_DURATION_LONG,
    easing = OrganicEmphasizedEasing
)

fun <T> organicTweenGentle() = tween<T>(
    durationMillis = ORGANIC_DURATION_SHORT,
    easing = OrganicGentleEasing
)
