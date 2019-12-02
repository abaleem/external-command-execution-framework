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
object DirBuilder{

  def apply(): DirBuilder[states.empty] = new DirBuilder[states.empty]()

  sealed trait state

  // all possible states. extendable.
  object states {
    // empty state. initial
    sealed trait empty extends state
    // path the search the in.
    sealed trait path extends state
    // args to add for string search
    sealed trait args extends state
    sealed trait recursive extends args
    sealed trait sorted extends args
    sealed trait filetype extends args
    // minimum type required for build to be called
    type minimumRequest = empty with path
  }

}

/**
 * DirBuilder class using phantom types to ensure correct states at compile time.
 * @param components = initial(required) components for the evaluation of task
 * @tparam T = State of the DirBuilder
 */
class DirBuilder[T <: DirBuilder.state](components: Seq[String] = Seq("cmd.exe", "/c", "dir", "/b")) {

  import DirBuilder.states._

  val LOG_TAG = Logger(LoggerFactory.getLogger("DirBuilder"))

  // Path to list the directory for. defaults to current working directory. but do have to initialize.
  // Implicitly states that setDir must be implemented on empty
  def setDir(value: String = ".")(implicit ev: T =:= empty): DirBuilder[T with path] = {
    LOG_TAG.info("Setting directory to " + value)
    new DirBuilder(components :+ value)
  }

  // makes the search recursive (checks all subfolders)
  def makeRecursive: DirBuilder[T with recursive] = {
    LOG_TAG.info("Making search recursive")
    new DirBuilder(components :+ "/s")
  }

  // sorts the result of search result according to name, date or size. defaults to name.
  def sortBy(value: String): DirBuilder[T with sorted] = {
    LOG_TAG.info("Sorting search according to " + value)
    value match{
      case "name" => new DirBuilder(components :+ "/o:n")
      case "date" => new DirBuilder(components :+ "/o:d")
      case "size" => new DirBuilder(components :+ "/o:s")
      case _ => new DirBuilder(components)
    }
  }

  // filters the type of file search for. e.g. hidden, system etc.
  def fileType(value: String): DirBuilder[T with filetype] = {
    LOG_TAG.info("Filtering filetypes for " + value)
    value match{
      case "directory" => new DirBuilder(components :+ "/A:d")
      case "file" => new DirBuilder(components :+ "/A:-d")
      case "system" => new DirBuilder(components :+ "/A:s")
      case "hidden" => new DirBuilder(components :+ "/A:h")
      case _ => new DirBuilder(components)
    }
  }

  // Outputs the command to be pipelined to other builders
  def pipeline(implicit ev: T <:< minimumRequest): Seq[String] = {
    LOG_TAG.info("Piplining to some other command")
    components
  }

  // executes the command if barerequest met.
  def build(implicit ev: T <:< minimumRequest): String = {
    LOG_TAG.info("Building")
    val output = Execute.cmd(Command(components))
    output.getOrElse("Execution error!")
  }

}
  
