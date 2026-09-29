package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Rich Spiritual Emerald Green Gradient Colors
val EmeraldGradTop = Color(0xFF041E15)      // Mystical deep forest pine
val EmeraldGradMid1 = Color(0xFF0B3324)     // Rich royal emerald green
val EmeraldGradMid2 = Color(0xFF114633)     // Radiant Islamic jade
val EmeraldGradBottom = Color(0xFF062016)   // Deep serene emerald base
val DeepForestNight = EmeraldGradTop

// Shared Atmospheric Gradient Brush
val IslamicGreenGradient = Brush.verticalGradient(
    colors = listOf(
        EmeraldGradTop,
        EmeraldGradMid1,
        EmeraldGradMid2,
        EmeraldGradBottom
    )
)

// Liquid Frosted Glass Colors
val LiquidGlassFillTop = Color(0xDD184A37)    // Translucent emerald glass top
val LiquidGlassFillBottom = Color(0xA80C2D21) // Translucent deep jade base
val LiquidGlassBorderTop = Color(0x99FFFFFF)  // Specular reflection rim
val LiquidGlassBorderMid = Color(0x2BFFFFFF)  // Subtle translucent refraction
val LiquidGlassBorderBottom = Color(0x5952B788) // Soft emerald glow bottom

val LiquidGlassBorderBrush = Brush.verticalGradient(
    colors = listOf(
        LiquidGlassBorderTop,
        LiquidGlassBorderMid,
        LiquidGlassBorderBottom
    )
)

val LiquidGlassBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        LiquidGlassFillTop,
        LiquidGlassFillBottom
    )
)

// Card Surfaces with soft emerald translucency
val EmeraldCardSurface = Color(0xEB0E3326)
val EmeraldCardVariant = Color(0xDE154332)
val EmeraldBorder = Color(0x4052B788)

// Gold Accents (Diyanet Zarafeti)
val GoldPrimary = Color(0xFFF1C40F)
val GoldLight = Color(0xFFFDE68A)
val GoldDark = Color(0xFFD4AF37)
val GoldMuted = Color(0x33F1C40F)

// Emerald & Mint Radiance
val EmeraldPrimary = Color(0xFF2D6A4F)
val EmeraldAccent = Color(0xFF52B788)
val EmeraldLight = Color(0xFF74C69D)
val MintSoft = Color(0xFFD8F3DC)

// Functional Colors
val KerahatAmber = Color(0xFFF59E0B)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFFA5C4B7)
val TextMutedDark = Color(0xFF739989)

// Light Theme Green Palette (Açık Adaçayı ve Nane Tonları)
val SageBackgroundLight = Color(0xFFF0F5F2)
val CardSurfaceLight = Color(0xFFFFFFFF)
val CardSurfaceVariantLight = Color(0xFFE3EDE6)
val BorderSubtleLight = Color(0xFFCADBD0)
val TextPrimaryLight = Color(0xFF0B241A)
val TextSecondaryLight = Color(0xFF335848)
