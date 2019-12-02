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
object TaskKillBuilder{

  def apply(): TaskKillBuilder[states.empty] = new TaskKillBuilder[states.empty]()

  sealed trait state
  // all possible states. extendable.
  object states {
    // empty state. initial
    sealed trait empty extends state
    // Input style. From pid or pipelined
    sealed trait method extends state
    sealed trait pid extends method
    sealed trait pipelined extends method
    // args to add for string search
    sealed trait args extends state
    sealed trait killChilds extends args
    // minimum type required for build to be called
    type minimumRequest = empty with method
    }
}

/**
 * TaskKillBuilder class using phantom types to ensure correct states at compile time.
 * @param components = initial(required) components for the evaluation of task
 * @tparam T = State of the TaskKillBuilder
 */
class TaskKillBuilder[T <: TaskKillBuilder.state](components: Seq[String] = Seq("taskkill")) {

  import TaskKillBuilder.states._

  val LOG_TAG = Logger(LoggerFactory.getLogger("TaskKillBuilder"))

  // helper function
  private def isAllDigits(x: String) = x forall Character.isDigit

  // takes a number and checks if its all digits and appends to the sequence list
  def pid(value: String): TaskKillBuilder[T with pid]= {
    LOG_TAG.info("PID to kill: " + value)
    isAllDigits(value) match {
      case true => new TaskKillBuilder(components ++ Seq("/pid", value))
      case false => new TaskKillBuilder(components)
    }
  }

  // Allows us to kill all child processes started by the pid
  def killChilds: TaskKillBuilder[T with killChilds] = {
    LOG_TAG.info("Killing process children too")
    new TaskKillBuilder(components :+ "/t")
  }

  // Pipelines from a list of pids. checks to see if all digits.
  // Implictly states that if pipelineFrom implemented it must the first function called.
  def pipelineFrom(value: Array[String])(implicit ev: T =:= empty): TaskKillBuilder[T with pipelined with pid] = {
    LOG_TAG.info("Piplining from some other command")
    new TaskKillBuilder(components ++ value.filter(x => isAllDigits(x)).map(x => "/pid,%s".format(x)).map(x => x.split(",")).flatten.toSeq)
  }

  // executes the command if bare request met.
  def build(implicit ev: T <:< minimumRequest) = {
    LOG_TAG.info("Building")
    val output = Execute.cmd(Command(components))
    output.getOrElse("Execution error!")
  }

}
  
  