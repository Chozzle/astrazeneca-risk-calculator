import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.*
import kotlinx.html.*

fun HTML.index() {
    head {
        title("Which jab?")
    }
    body {
        div {
            val mortality = calculateAZOutcome(
                age = 35,
                chanceOftts = 1f / 100_000f,
                ttsMortality = 3f / 100f
            )
            +"Your AZ mortality per million: ${mortality * 1_000_000}"
        }
        div {
            id = "root"
        }
        script(src = "/static/untitled.js") {}
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        routing {
            get("/") {
                call.respondHtml {
                    index()
                }
            }
            static("/static") {
                resources()
            }
        }
    }.start(wait = true)
}