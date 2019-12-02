package Builders

import Command.Command
import Util.Execute
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Defines the builders apply method (reduces creation time syntax for client)
 * Also, defines all possible states. For some params states are not necessary
 * Still, I have added them for future extensibility
 */
object FindStrBuilder{

  def apply(): FindStrBuilder[states.empty] = new FindStrBuilder[states.empty]()

  sealed trait state

  // all possible states. extendable.
  object states {
    // empty state. initial
    sealed trait empty extends state
    // basic string to search for
    sealed trait string extends state
    // args to add for string search
    sealed trait args extends state
    sealed trait ignoreCase extends args
    // Input Styles. From a path or pipelined from another command.
    sealed trait inputStyle extends state
    sealed trait path extends inputStyle
    sealed trait pipelined extends inputStyle
    // minimum type required for build to be called
    type minimumRequest = empty with string with inputStyle
  }
}

/**
 * FindStrBuilder class using phantom types to ensure correct states at compile time.
 * @param components = initial(required) components for the evaluation of task
 * @tparam T = State of the FindStrBuilder
 */
class FindStrBuilder[T <: FindStrBuilder.state](components: Seq[String] = Seq("findstr")) {

  import FindStrBuilder.states._

  val LOG_TAG = Logger(LoggerFactory.getLogger("FindStrBuilder"))

  // The string to be searched from
  // Implicitly states that set string can only follow empty, pipelined or other args.
  def setString(value:String)(implicit ev: empty with pipelined with args <:< T): FindStrBuilder[T with string] = {
    LOG_TAG.info("Setting search string to " + value)
    new FindStrBuilder(components :+ "/C:\"%s\"".format(value))
  }

  // inputStyle option1. Path to search in.
  // Implicitly states that setString must be implemented before setPath can be called
  def setPath(value:String = "*")(implicit ev: T <:< string): FindStrBuilder[T with path] = {
    LOG_TAG.info("Setting path to " + value)
    new FindStrBuilder(components :+ value)
  }

  // Ignores the case while searching for the string
  // Implicity states that it can only follow empty, piplined or other args. Can't be put after setString or setPath.
  def ignoreCase(implicit ev: empty with pipelined with args <:< T): FindStrBuilder[T with args] = {
    LOG_TAG.info("Ignoring case in search results")
    new FindStrBuilder(components :+ "/i")
  }

  // inputStyle option2. Pipelines another command. Takes the seq of strings to be executed.
  // implicitily states that pipelineFrom must be the first command after decleration.
  def pipelineFrom(value: Seq[String])(implicit ev: T =:= empty): FindStrBuilder[T with pipelined] = {
    LOG_TAG.info("Pipeling from some other command")
    new FindStrBuilder(value ++ Seq("|", "findstr"))
  }

  // executes the command if bare request met.
  def build(implicit ev: T <:< minimumRequest): String = {
    LOG_TAG.info("Building")
    val output = Execute.cmd(Command(components))
    output.getOrElse("Execution error!")
  }

}