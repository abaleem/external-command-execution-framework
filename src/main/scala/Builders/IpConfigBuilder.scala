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
object IpConfigBuilder{

  def apply(): IpConfigBuilder[states.empty] = new IpConfigBuilder[states.empty]()

  sealed trait state

  // all possible states. extendable.
  object states {
    // empty state. initial
    sealed trait empty extends state
    // args to add for string search
    sealed trait args extends state
    sealed trait all extends args
    sealed trait dns extends args
    // minimum type required for build to be called
    type bareRequest = empty
  }

}

/**
 * IpConfigBuilder class using phantom types to ensure correct states at compile time.
 * @param components = initial(required) components for the evaluation of task
 * @tparam T = State of the IpConfigBuilder
 */
class IpConfigBuilder[T <: IpConfigBuilder.state](components: Seq[String] = Seq("cmd.exe", "/c", "ipconfig")) {

  import IpConfigBuilder.states._

  val LOG_TAG = Logger(LoggerFactory.getLogger("IpConfigBuilder"))

  // Displays the full TCP/IP configuration for all adapters.
  // Adapters can represent physical interfaces, such as installed network adapters, or logical interfaces, such as dial-up connections
  // Implicitly defined to be only called on an empty type
  def getAll(implicit ev: T =:= empty): IpConfigBuilder[T with all] = {
    LOG_TAG.info("Getting extensive results")
    new IpConfigBuilder(components :+ "/all")
  }

  // Displays the contents of the DNS client resolver cache, which includes both entries preloaded from the local Hosts file and
  // any recently obtained resource records for name queries resolved by the computer. The DNS Client service uses this information
  // to resolve frequently queried names quickly, before querying its configured DNS servers.
  // Implicitly defined to be only called on an empty type
  def getDNS(implicit ev: T =:= empty): IpConfigBuilder[T with dns] = {
    LOG_TAG.info("Setting getDNS to true")
    new IpConfigBuilder(components :+ "/displaydns")
  }

  // Outputs the command to be pipelined to other builders
  def pipeline: Seq[String] = {
    LOG_TAG.info("Piplining to some other command")
    components
  }


  // executes the command if barerequest met.
  def build(implicit ev: T <:< bareRequest)= {
    LOG_TAG.info("Building")
    val output = Execute.cmd(Command(components))
    output.getOrElse("Execution error!")
  }

}

