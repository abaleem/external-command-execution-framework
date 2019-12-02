import Builders._
import Util.Functional._
import com.typesafe.config.{Config, ConfigFactory}

object BasicExamples extends App {

  val config: Config = ConfigFactory.load("config.conf")

  val userName = config.getString("test.userName")
  val stringSearch = config.getString("test.stringSearch")
  val srcPath = config.getString("test.srcPath")


  // TaskList
  // tasklist basic
  TaskListBuilder().build

  // tasklist with some additional arguments
  TaskListBuilder().setUser(userName).setRunning.memoryUsageAbove(50000).build

  // tasklist with output parsed into columns
  TaskListBuilder().buildParsed

  // tasklist finds all tasks run by User, parses them into columns and filters the column with title = PID.
  // returns a 2d list as we can filter multiple columns (add to Seq) at once hence we flatten
  TaskListBuilder().setUser(userName).buildParsed.filter(Column(Seq("PID"))).flatten


  // TaskKill
  // Taskkill basic. DON'T RUN THIS IF YOU ARE NOT SURE WHICH PROCESS'S PID IS IT.
  //TaskKillBuilder().pid("1000").killChilds.build

  // DIR
  // lists the items in the directory. Defaults to current directory if setDir as no input
  DirBuilder().setDir(srcPath).build

  // dirbuilder with some filters
  DirBuilder().setDir().sortBy("name").fileType("hidden").makeRecursive.build


  // FindStr
  // findstr minimal. setPath defaults to current if left black
  FindStrBuilder().setString(stringSearch).setPath().build

  // findstr with ignoreCase added. not the case of search string doesnt matter.
  FindStrBuilder().ignoreCase.setString(stringSearch).setPath().build


  // IpConfig
  // Basic ipconfig
  IpConfigBuilder().build

  // ifconfig with args
  IpConfigBuilder().getDNS.build

}
