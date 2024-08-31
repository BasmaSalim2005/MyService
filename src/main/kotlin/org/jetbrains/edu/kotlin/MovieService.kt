
package org.jetbrains.edu.kotlin

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.json.Json

import MovieServiceImpl

interface MovieService {
    /**
     * @param movieTitle Name of a movie.
     * @return Cast of the movie (list of actors).
     * @throws MovieNotFoundException if no such movie could be found
     */
    suspend fun getActors(movieTitle: String): List<String>

    /**
     * @param movies List of movies.
     * @return Top 5 actors with most appearances in these movies.
     */
    suspend fun topActors(movies: List<String>): List<String>

    companion object {
        /**
         * @param maxThreads The maximum number of threads to use.
         */
        fun get(maxThreads: Int): MovieService {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            val dispatcher = newFixedThreadPoolContext(maxThreads, "MovieServicePool")

            return MovieServiceImpl(client, dispatcher)
        }
    }
}
