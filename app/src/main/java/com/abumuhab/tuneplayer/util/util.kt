package com.abumuhab.tuneplayer.util

fun generateRandomLongBetween(min: Long, max: Long): Long {
    return min + (Math.random() * (max - min)).toLong()
}