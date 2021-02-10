package com.hubertp

import org.slf4j.{Logger, LoggerFactory}

object Hello extends Greeting {

  private val logger: Logger = LoggerFactory.getLogger(classOf[Hello.type])

  def main(args: Array[String]): Unit = {

    logger.info("What's up")

    println(greeting)
  }
}

trait Greeting {
  lazy val greeting: String = "hello"
}
