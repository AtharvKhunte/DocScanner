package com.example.documentscanner.ui.theme

import androidx.compose.ui.graphics.Color

object DocVaultColors {

    // ── Backgrounds ──────────────────────────────────────────────
    val DarkBackground      = Color(0xFF0B0E14)   // primary app background
    val CardSurface         = Color(0xFF161B26)   // card / surface background
    val DarkGlassAlpha      = Color(0x26161B26)   // translucent dark overlay

    // ── Brand Accents ─────────────────────────────────────────────
    val ElectricIndigo      = Color(0xFF5B61F6)   // primary accent (indigo/purple)
    val EmeraldVerified     = Color(0xFF10B981)   // secondary accent (teal/green)

    // ── Glass Tints ───────────────────────────────────────────────
    val WhiteGlassAlpha     = Color(0x14FFFFFF)   // 8% white — glass frost layer

    // ── Borders ───────────────────────────────────────────────────
    val Border              = Color(0xFF232A3B)   // card border / divider

    // ── Text Hierarchy ────────────────────────────────────────────
    val TextPrimary         = Color(0xFFFFFFFF)   // headings, file names
    val TextSecondary       = Color(0xFF94A3B8)   // subtitles, dates, metadata
    val TextTertiary        = Color(0xFF4B5563)   // placeholder, disabled

    // ── Semantic ──────────────────────────────────────────────────
    val Error               = Color(0xFFFF6B6B)
    val Success             = Color(0xFF10B981)
    val Warning             = Color(0xFFFFA500)
}