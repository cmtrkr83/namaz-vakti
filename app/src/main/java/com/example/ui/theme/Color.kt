package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Soft Pastel Sage & Eucalyptus Palette (Yumuşak, Belirgin Adaçayı Yeşili ve Dinlendirici Tonlar)
val SageGradTop = Color(0xFFC5D8CD)       // Soft misty eucalyptus sage
val SageGradMid1 = Color(0xFFD2E2D8)      // Gentle pastel sage breeze
val SageGradMid2 = Color(0xFFBED3C6)      // Calm muted botanical sage
val SageGradBottom = Color(0xFFABC4B5)    // Soft restful sage anchor

// Backward-compatible aliases
val EmeraldGradTop = SageGradTop
val EmeraldGradMid1 = SageGradMid1
val EmeraldGradMid2 = SageGradMid2
val EmeraldGradBottom = SageGradBottom
val DeepForestNight = Color(0xFF172A22)

// Shared Atmospheric Gradient Brush
val SereneSageGradient = Brush.verticalGradient(
    colors = listOf(
        SageGradTop,
        SageGradMid1,
        SageGradMid2,
        SageGradBottom
    )
)
val IslamicGreenGradient = SereneSageGradient

// Frosted Liquid Glass Colors (Soft misty sage frosted glass, never stark white)
val LiquidGlassFillTop = Color(0xDCE8F1EB)      // Soft frosted eucalyptus cream top
val LiquidGlassFillBottom = Color(0xD4CFE0D6)   // Frosted sage glass bottom
val LiquidGlassBorderTop = Color(0xCCFFFFFF)    // Delicate frosted top refraction
val LiquidGlassBorderMid = Color(0x66FFFFFF)    // Subtle side edge
val LiquidGlassBorderBottom = Color(0x66557B69) // Gentle sage glass rim

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

// Matte Tinted Sage-Cream Card Surfaces (Zero harsh white glare)
val SageCardSurface = Color(0xDCEBF2ED)         // Soft translucent sage-ivory card
val SageCardVariant = Color(0xFFD3E2D8)         // Muted pastel sage surface
val SageSheetSurface = Color(0xFFE3ECE6)        // Opaque soft sage bottom sheet
val SageHairlineBorder = Color(0x52577D6C)      // Delicate sage hairline border

val EmeraldCardSurface = SageCardSurface
val EmeraldCardVariant = SageCardVariant
val EmeraldBorder = SageHairlineBorder

// Botanical Sage Primary & Soft Accent Tones
val SagePrimary = Color(0xFF245C49)             // Deep calm eucalyptus-sage
val SagePrimarySoft = Color(0xFF3E7A65)         // Soft leaf sage
val SagePrimaryMuted = Color(0x21245C49)        // Soft sage highlight fill
val CeladonAccent = SagePrimarySoft
val CeladonDark = Color(0xFF1A4537)
val CeladonSoft = Color(0xFF6E9B89)
val MintSoft = Color(0xFFC8DDD1)

val EmeraldPrimary = SagePrimary
val EmeraldAccent = SagePrimarySoft
val EmeraldLight = CeladonSoft

// Warm Sand / Soft Antique Gold Accents
val WarmGoldAccent = Color(0xFF826026)          // Restful antique sand-gold
val WarmGoldSoft = Color(0xFFA88342)            // Warm sunlit sand
val WarmGoldMuted = Color(0x249E7A38)           // Soft warm sand tint

val ChampagneGold = WarmGoldAccent
val ChampagneLight = WarmGoldAccent
val ChampagneDark = Color(0xFF634719)
val ChampagneMuted = WarmGoldMuted
val GoldPrimary = WarmGoldAccent
val GoldLight = WarmGoldSoft
val GoldDark = ChampagneDark
val GoldMuted = WarmGoldMuted

// Text Hierarchy (Calm deep pine-ink)
val TextPrimary = Color(0xFF172A22)             // Deep serene pine-ink
val TextSecondary = Color(0xFF476256)           // Muted eucalyptus slate
val TextMuted = Color(0xFF6E897D)               // Gentle sage gray

val TextPrimaryDark = TextPrimary
val TextSecondaryDark = TextSecondary
val TextMutedDark = TextMuted

// Functional Colors
val KerahatAmber = Color(0xFFA85E18)

// Light Theme Reference Tokens
val SageBackgroundLight = SageGradTop
val CardSurfaceLight = SageCardSurface
val CardSurfaceVariantLight = SageCardVariant
val BorderSubtleLight = SageHairlineBorder
val TextPrimaryLight = TextPrimary
val TextSecondaryLight = TextSecondary
