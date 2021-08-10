import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.content.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.routing.*
import kotlinx.html.*

fun HTML.index() {
    head {
        title("Which jab?")
    }

    body {
        div {
            form(encType = FormEncType.applicationXWwwFormUrlEncoded,method = FormMethod.get) {
                p {
                    label { +"Age" }
                    textInput {
                        name = "Age"
                    }
                }
                p {
                    label { +"Age" }
                    textInput {
                        name = "Daily cases in your area"
                    }
                }
                p {
                    label { +"Age" }
                    textInput {
                        name = "Population in your area (where cases are appearing, eg your state or city)"
                    }
                }
                p {
                    label { +"Age" }
                    textInput {
                        name = "Daily cases in your area"
                    }
                }
                p {
                    label { +"How many weeks until you can receive the Astra Zeneca vaccine:" }
                    textInput {
                        name = "astraWeeks"
                    }
                }
                p {
                    label { +"How many weeks until you can receive the Pfizer vaccine:" }
                    textInput {
                        name = "pfizerWeeks"
                    }
                }
                p {
                    label { +"Age" }
                    submitInput {
                        value = "send"
                    }
                }
            }
            div {
                id = "root"
            }
            script(src = "/static/untitled.js") {}
        }
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
            static("/") {
                resources()
            }
        }
    }.start(wait = true)
}