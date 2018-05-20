package org.jetbrains.demo.kotlinfractals

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.OutgoingContent
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.experimental.io.ByteWriteChannel
import kotlinx.coroutines.experimental.io.jvm.javaio.toOutputStream
import kotlinx.html.body
import kotlinx.html.h1
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO


fun main(args: Array<String>) {
  System.setProperty("java.awt.headless", "true")

  val server = embeddedServer(Netty, 8888, module = Application::main)
  server.start(wait = true)
}

fun Application.main() {
  install(DefaultHeaders)
  install(CallLogging)
  install(Routing) {
    get("/") {
      call.respondHtml {
        body {
          h1 { +"Kotlin Fractals" }
        }
      }
      call.respondText("Fractal rendering service!")
    }

    get("/mandelbrot") {
      val width = 600
      val height = 600

      val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

      img.draw {
        //TODO: call common code there
      }

      call.respond(status = HttpStatusCode.OK, message = img.toMessage())
    }
  }
}

private inline fun <T> BufferedImage.draw(action: Graphics2D.() -> T) : T {
  val g = createGraphics()
  try {
    return g.action()
  } finally {
    g.dispose()
  }
}

private fun BufferedImage.toMessage() = let { img ->
  object : OutgoingContent.WriteChannelContent() {
    override val contentType: ContentType?
      get() = ContentType.Image.PNG

    override suspend fun writeTo(channel: ByteWriteChannel) {
      channel.toOutputStream().use {
        ImageIO.write(img, "png", it)
      }
    }
  }
}

