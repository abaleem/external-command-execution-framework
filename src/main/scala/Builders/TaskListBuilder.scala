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
object TaskListBuilder{

  def apply(): TaskListBuilder[states.empty] = new TaskListBuilder[states.empty]()

  sealed trait state
  // all possible states. extendable.
  object states {
    // empty state. initial
    sealed trait empty extends state
    // filters to refine results
    sealed trait filters extends state
    sealed trait user extends state
    sealed trait running extends state
    sealed trait memory extends state
    // minimum type required for build to be called
    type minimumRequest =  empty
  }
}

/**
 * TaskListBuilder class using phantom types to ensure correct states at compile time.
 * @param components = initial(required) components for the evaluation of task
 * @tparam T = State of the TaskListBuilder
 */
class TaskListBuilder[T <: TaskListBuilder.state](components: Seq[String] = Seq("cmd.exe", "/c", "tasklist","/FO", "CSV", "/v")) {

  import TaskListBuilder.states._

  val LOG_TAG = Logger(LoggerFactory.getLogger("TaskListBuilder"))

  // Filter the results based on the username provided.
  def setUser(username: String):TaskListBuilder[T with user] = {
    LOG_TAG.info("Setting User to: " + username)
    new TaskListBuilder(components ++ Seq("/fi" , "\"username eq %s\"".format(username)))
  }

  // Filter the results for the tasks that are running.
  def setRunning: TaskListBuilder[T with running] = {
    LOG_TAG.info("Checking for running tasks only")
    new TaskListBuilder(components ++ Seq("/fi" , "\"STATUS eq running\""))
  }

  // Filters the results that have memory usage (in kbs) above a certain point
  def memoryUsageAbove(value: Int): TaskListBuilder[T with memory] = {
    LOG_TAG.info("Checking for memory above: " + value)
    new TaskListBuilder(components ++ Seq("/fi" , "\"MEMUSAGE gt %s\"".format(value)))
  }

  // Outputs the command to be pipelined to other builders
  def pipeline(implicit ev: T <:< minimumRequest): Seq[String] = {
    LOG_TAG.info("Piplining to some other command")
    components
  }

  // executes the command if barerequest met. Returns string as it is.
  def build(implicit ev: T <:< minimumRequest): String = {
    LOG_TAG.info("Building")
    val output = Execute.cmd(Command(components))
    output.getOrElse("Execution error!")
  }

  // executes the command if barerequest met. Returns string after pasring. Each column is not a value e.g. pid or image name.
  def buildParsed(implicit ev: T <:< minimumRequest): Array[Array[String]] = {
    LOG_TAG.info("Building and Parsing")
    val output = Execute.cmd(Command(components))
    Util.Parser.parseTaskList(output.getOrElse(""))
  }
}
  
  