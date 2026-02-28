package com.example.t_20.util

import com.example.t_20.R

object ImageHelper {

    private val productImageMap = mapOf(
        // Accesorios
        "black ring" to R.drawable.black_ring,
        "mate bracelet" to R.drawable.mate_bracelet,
        "military necklace" to R.drawable.military_necklace,
        "necklace" to R.drawable.necklace,
        "beanie" to R.drawable.beanie,
        "new era cap" to R.drawable.new_era_cap,
        "scarf" to R.drawable.scarf,
        "sunglasses" to R.drawable.sunglasses,

        // Camisas
        "sky blue shirt" to R.drawable.skyblue_shirt,
        "black shirt" to R.drawable.black_shirt,
        "brown shirt" to R.drawable.brown_shirt,
        "vintage shirt" to R.drawable.vintage,
        "dark blue shirt" to R.drawable.dark_blue_shirt,
        "sage shirt" to R.drawable.sage_shirt,
        "camisa casual" to R.drawable.camisa_casual,

        // Pantalones
        "sky blue jeans" to R.drawable.sky_blue_jeans,
        "dark jean" to R.drawable.dark_jean,
        "baggy street pants" to R.drawable.baggy_street_pants,
        "black cargo pants" to R.drawable.black_cargo_pants,
        "beige cargo pants" to R.drawable.beige_cargo_pants,
        "black jogger" to R.drawable.black_jogger,
        "vintage sweatpants" to R.drawable.vintage_sweatpants,
        "wind pants" to R.drawable.wind_pants,

        // Poleras
        "boston" to R.drawable.boston,
        "galaxy hoodie" to R.drawable.galaxy_hoodie,
        "personality hoodie" to R.drawable.personality_hoodie,
        "blue sweatshirt" to R.drawable.macracosm,
        "universe hoodie" to R.drawable.universe_hoodie,
        "fearless" to R.drawable.fearless,
        "human vs human" to R.drawable.human_vs_human,
        "olive jacket" to R.drawable.olive_jacket,

        // Polos
        "manchester united jersey" to R.drawable.manchester_united,
        "barcelona sweatshirt" to R.drawable.barcelona,
        "green palm t-shirt" to R.drawable.green_palm,
        "basic gray t-shirt" to R.drawable.basic_gray_tshirt,
        "formula1" to R.drawable.formula1,
        "breakout" to R.drawable.breakout,
        "today you inspired me" to R.drawable.today_you_inspired_me,
        "blue sweater" to R.drawable.blue_sweater
    )

    fun getImageResForProduct(productName: String): Int {
        val normalizedName = productName.lowercase().trim()
        return productImageMap[normalizedName] ?: R.drawable.error404
    }

    fun isValidResourceId(context: android.content.Context, resourceId: Int): Boolean {
        if (resourceId == 0) return false
        return try {
            context.resources.getResourceName(resourceId)
            true
        } catch (e: Exception) {
            false
        }
    }
}
