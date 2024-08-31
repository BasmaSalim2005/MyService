package org.jetbrains.edu.kotlin

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class MovieServiceTest {
    private val threadLimit = 3
    private val service = MovieService.get(threadLimit)

    @ParameterizedTest(name = "Actor or actress {1} should be present in the cast of {0}")
    @MethodSource("actors")
    fun getActors(movie: String, keyActor: String) {
        runBlocking {
            val retrievedActors = service.getActors(movie).toSet()
            assertTrue(retrievedActors.contains(keyActor))
        }
    }

    @Test
    fun getActorsThrows() {
        val retriever = MovieService.get(2)
        assertThrows(MovieNotFoundException::class.java) {
            runBlocking {
                retriever.getActors("Kotlin is a programming language")
            }
        }
        assertThrows(MovieNotFoundException::class.java) {
            runBlocking {
                retriever.getActors("Non-existent movie which definitely does not exist")
            }
        }
    }

    @ParameterizedTest(name = "{1} should be in top for {0}")
    @MethodSource("top")
    fun topActors(movies: List<String>, actors: List<String>) {
        runBlocking {
            val retrievedActors = service.topActors(movies).toSet()
            for (actor in actors) {
                assertTrue(retrievedActors.contains(actor))
            }
        }
    }

    companion object {
        @JvmStatic
        fun actors() =
            listOf(
                Arguments.of("2001: A Space Odyssey", "Keir Dullea"),
                Arguments.of("I'm Thinking of Ending Things", "Jessie Buckley"),
                Arguments.of("I'm Thinking of Ending Things", "Jesse Plemons"),
                Arguments.of("There Will Be Blood", "Daniel Day-Lewis"),
                Arguments.of("There Will Be Blood", "Paul Dano"),
                Arguments.of("Three Billboards Outside Ebbing, Missouri", "Frances McDormand")
            )

        @JvmStatic
        fun top() =
            listOf(
                Arguments.of(
                    listOf("The Newton Boys", "Surfer, Dude", "Welcome to Hollywood"),
                    listOf("Matthew McConaughey", "Woody Harrelson")
                ),
                Arguments.of(
                    listOf("Gangster Squad", "La La Land", "Crazy, Stupid, Love"),
                    listOf("Ryan Gosling", "Emma Stone")
                ),
                Arguments.of(
                    listOf("Gangster Squad", "La La Land", "Crazy, Stupid, Love", "Movie That 100% Does Not Exist"),
                    listOf("Ryan Gosling", "Emma Stone")
                )
            )
    }
}
