
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.jetbrains.edu.kotlin.MovieService
import org.jetbrains.edu.kotlin.MovieNotFoundException
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MovieServiceImpl(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    },
    private val dispatcher: CoroutineDispatcher
) : MovieService {
    private val logger = LoggerFactory.getLogger(MovieServiceImpl::class.java)
    override suspend fun getActors(movieTitle: String): List<String> = withContext(dispatcher) {
        // val movieT = movieTitle.replace(" ","_")
        val movie1 = URLEncoder.encode(movieTitle, StandardCharsets.UTF_8.toString())

        val movieId = fetchMovieId(movie1) ?: throw MovieNotFoundException(movieTitle)
        fetchCastMembers(movieId)
    }

    override suspend fun topActors(movies: List<String>): List<String> = withContext(dispatcher) {
        val actorCounts = mutableMapOf<String, Int>()

        coroutineScope {
            movies.map { movie ->
                async {
                    try {
                        val actors = getActors(movie)
                        actors.forEach { actor ->
                            synchronized(actorCounts) {
                                actorCounts[actor] = actorCounts.getOrDefault(actor, 0) + 1
                            }
                        }
                    } catch (e: MovieNotFoundException) {
                        logger.error("An error occurred: ${e.message}", e)
                    }
                }
            }.awaitAll()
        }

        actorCounts.entries.sortedByDescending { it.value }
            .take(5)
            .map { it.key }
    }

    private suspend fun fetchMovieId(movieTitle: String): String? = withContext(dispatcher) {
        val url = "https://www.wikidata.org/w/api.php?action=wbsearchentities&search=$movieTitle&language=en&format=json"
        val response: SearchResult = client.get(url) {
            contentType(ContentType.Application.Json)
        }.body()  

        response.search.find { it.description.contains("film", ignoreCase = true) }?.id
    }
    private suspend fun fetchCastMembers(movieId: String): List<String> = withContext(dispatcher) {
        val url = "https://www.wikidata.org/w/api.php?action=wbgetclaims&entity=$movieId&property=P161&format=json"
        print(url)
        val castResult: CastResult = client.get(url) {
            contentType(ContentType.Application.Json)
        }.body()  

        val castIds = castResult.claims.pp161.map { it.mainsnak.datavalue.value.id }

        
        castIds.map { castId ->
            val url2 = "https://www.wikidata.org/w/api.php?action=wbgetentities&ids=$castId&format=json"
            val actorResult: ActorResult = client.get(url2) {
                contentType(ContentType.Application.Json)
            }.body()  

           
            actorResult.entities[castId]?.labels?.get("en")?.value ?: "Unknown Actor"
        }
    }

    companion object {
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

@Serializable
data class SearchResult(
    val search: List<SearchItem>
)

@Serializable
data class SearchItem(
    @SerialName("id") val id: String,
    @SerialName("description") val description: String = ""
)

@Serializable
data class CastResult(
    val claims: Claims
)

@Serializable
data class Claims(
    @SerialName("P161")
    val pp161: List<P161Claim>
)

@Serializable
data class P161Claim(
    val mainsnak: Mainsnak
)

@Serializable
data class Mainsnak(
    val datavalue: Datavalue
)

@Serializable
data class Datavalue(
    val value: Value
)

@Serializable
data class Value(
    val id: String
)
@Serializable
data class ActorResult(
    val entities: Map<String, Entity>
)
@Serializable
data class Entity(
    val labels: Map<String, Label>
)
@Serializable
data class Label(
    val value: String
)
