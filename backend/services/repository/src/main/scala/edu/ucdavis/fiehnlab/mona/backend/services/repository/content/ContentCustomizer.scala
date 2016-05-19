package edu.ucdavis.fiehnlab.mona.backend.services.repository.content

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{ResourceHandler, ContextHandler}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer
import org.springframework.stereotype.Component

/**
  * Created by wohlg_000 on 5/19/2016.
  */
@Component
class ContentCustomizer extends JettyServerCustomizer with LazyLogging {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}")
  val dir: String = null

  override def customize(server: Server): Unit = {

    logger.info(s"adding content customizer ${dir}")
    val contextHandler = new ContextHandler("/repository")

    val handler = new ResourceHandler()
    handler.setDirectoriesListed(true)
    handler.setResourceBase(dir)
    contextHandler.setHandler(handler)

    server.setHandler(contextHandler)
    logger.info("configured embedded servlet container")
  }
}
