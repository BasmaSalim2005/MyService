package org.jetbrains.edu.kotlin

import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val service = MovieService.get(2)  // Using 2 threads for parallel processing

    // Fetch the cast of "The Fly"
    val castNames = service.getActors("The_Fly")
    println("Cast of 'The Fly': $castNames")

    // Fetch the top 5 actors from a list of movies
    val topActors = service.topActors(listOf(
        "The Fly",
        "Non-existent movie which definitely does not exist",
        "Jurassic Park",
        "La La Land",
        "Thor: Love and Thunder"
    ))
    println("Top 5 Actors: $topActors")

    // This will throw an exception
    service.getActors("Non-existent movie which definitely does not exist")
}
