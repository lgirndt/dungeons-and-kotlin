package io.dungeons.world

fun isInRange(from: Coordinate, to: Coordinate, range: Feet): Boolean {
    return from.distance(to) <= range
}
