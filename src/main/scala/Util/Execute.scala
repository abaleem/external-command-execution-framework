package Util

import Command.Command
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import sys.process._

object Execute{

  val LOG_TAG = Logger(LoggerFactory.getLogger("Execute"))

  /**
   * Executes the command using sys.process library
    * @param input = Sequence of strings to be executed
   * @return Option[String] output of the result received from executing
   */
  def cmd(input:Command): Option[String] = {
    LOG_TAG.info("Executing command: " + input)
    try {
      val result = input.components.!!
      LOG_TAG.info("Execution successful!")
      Some(result.trim)
    } catch {
      case _: RuntimeException => {
        LOG_TAG.error("Execution failed!")
        None
      }
    }
  }
}
