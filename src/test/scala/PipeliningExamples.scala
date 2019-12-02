import Builders._
import Util.Functional._
import Util.Parser._
import com.typesafe.config.{Config, ConfigFactory}

object PipeliningExamples extends App{

  val config: Config = ConfigFactory.load("config.conf")

  val ifconfigFind = config.getString("test.ifconfigFind")
  val tasklistFind = config.getString("test.tasklistFind")
  val closeAll = config.getString("test.closeAll")


  // Basic pipelining - ipconfig output to findstr and searching for "ip"
  FindStrBuilder().pipelineFrom(IpConfigBuilder().pipeline).ignoreCase.setString(ifconfigFind).build

  // Another example with tasklist. tasklist output to findstr and search for "chrome"
  FindStrBuilder().pipelineFrom(TaskListBuilder().pipeline).setString(tasklistFind).build

  // A bit complex example - Multiple pipelines
  // NOTE: RUNNING THIS MAY KILL IMPORTANT TASKS AND CAUSE UNWANTED CONSEQUENCES IF IMPROPERLY USED. REFER TO DOCUMENTATION BEFORE UNCOMMENTING
  // ALSO NOTE: CHANGE NAME FROM CHROME IF YOU DONT WANT TO CLOSE CHROME TABS (CONFIG FILE)!
  // Lets start from the inside.
  // TaskListBuilder pipelines its output to FindStrBuilder which searches for "Snipping" in the result.
  // The output is then fed to parseTaskList which parses the string into 2d array.
  // We then extract the first column which is PID's and input into TaskKillBuilder pipeline option and execute it.

  //TaskKillBuilder().pipelineFrom(parseTaskList(FindStrBuilder().pipelineFrom(TaskListBuilder().pipeline).setString(closeAll).build)(1)).build

}
