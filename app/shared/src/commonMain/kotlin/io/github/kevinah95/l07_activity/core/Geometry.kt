package io.github.kevinah95.l07_activity.core

import kotlin.math.cos
import kotlin.math.sin

data class Point2D(
    val x: Double,
    val y: Double
)

data class Rect2D(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
) {
    /**
     * Determina si este rectángulo se superpone con [other]
     * mediante detección AABB.
     *
     * Los bordes que únicamente se tocan no se consideran una colisión.
     */
    fun overlaps(other: Rect2D): Boolean {
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y
    }
}

/**
 * Convierte un ángulo expresado en radianes en un vector unitario.
 */
fun vectorFromAngle(radians: Double): Point2D {
    return Point2D(
        x = cos(radians),
        y = sin(radians)
    )
}

fun clamp(value: Double, min: Double, max: Double): Double =
    if (value < min) min else if (value > max) max else value

fun clamp(value: Int, min: Int, max: Int): Int =
    if (value < min) min else if (value > max) max else value