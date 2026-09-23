package com.example.test2.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Compose mirror of `res/values/colors.xml`, which is the source of the brand
 * palette. Compose needs plain values here because most of these are read from
 * top-level `val`s, outside any composable, where `colorResource` is not
 * available. Change a colour in colors.xml and change it here too.
 *
 * Base colours: Brand800 #622B14, Brand500 #995F2F, BrandOlive #978F66,
 * Brand200 #E4D6A9.
 */
val Brand50 = Color(0xFFFBF7EC)
val Brand100 = Color(0xFFF3E9CF)
val Brand200 = Color(0xFFE4D6A9)
val Brand300 = Color(0xFFD3C08C)
val Brand400 = Color(0xFFB98F5B)
val Brand500 = Color(0xFF995F2F)
val Brand600 = Color(0xFF874F27)
val Brand700 = Color(0xFF713C1D)
val Brand800 = Color(0xFF622B14)
val Brand900 = Color(0xFF4A2010)

val BrandOlive = Color(0xFF978F66)
val BrandOliveSoft = Color(0xFFCFCBB3)

val BrandScreenBg = Color(0xFFFBF7EC)
val BrandCardBg = Color(0xFFFFFFFF)
val BrandCardBorder = Color(0xFFE8DEC4)
val BrandFieldBg = Color(0xFFFDFBF4)
val BrandFieldBorder = Color(0xFFE4D6A9)
val BrandHighlight = Color(0xFFF3E9CF)
val BrandTrackTodo = Color(0xFFEDE3C8)

val BrandTextPrimary = Color(0xFF2B1A10)
val BrandTextSecondary = Color(0xFF6B5A48)
val BrandTextMuted = Color(0xFFA2947F)

/** Status colours carry meaning, so they are not part of the brand ramp. */
val StatusDanger = Color(0xFFE03131)
val StatusDangerBg = Color(0xFFFEF3F3)
val StatusAmber = Color(0xFFB25E02)
val StatusAmberBg = Color(0xFFFFF6E5)
val StatusSuccessBg = Color(0xFFE7F6ED)
