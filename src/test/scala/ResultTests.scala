import Builders.{DirBuilder, FindStrBuilder, IpConfigBuilder}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite

class ResultTests extends FunSuite{

  val config: Config = ConfigFactory.load("config.conf")

  val srcPath = config.getString("test.srcPath")
  val mainStringSearch = config.getString("test.mainStringSearch")


  // Note: ".\src" contains 2 folders, main and test.
  test("Testing number of files in src directory(project)"){
    assert(DirBuilder().setDir(srcPath).build.split("\\n").length == 2)
  }

  test("Testing dir with sort"){
    assert(DirBuilder().setDir(srcPath).sortBy("name").build.split("\\n")(0).trim == mainStringSearch)
  }


}
