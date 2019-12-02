import Builders.{DirBuilder, FindStrBuilder, IpConfigBuilder}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.scalatest.FunSuite
import org.slf4j.LoggerFactory

/**
 * Compile tests verify that is methods are invoke in the wrong order or
 * Minimum requirements is not met the code should not compile
 */
class CompileTests extends FunSuite{

  val LOG_TAG = Logger(LoggerFactory.getLogger("CompileTests"))


  test("Phantom types checking - should not compile because setPath requires setString"){
    assertDoesNotCompile("FindStrBuilder().setPath()")
  }

  test("Phantom types checking - should compile now becasue setString is set"){
    assertCompiles("FindStrBuilder().setString(\"build\").setPath()")
  }

  test("Phantom types checking - should not compile as order switched! cmd cant execute"){
    assertDoesNotCompile("FindStrBuilder().setPath().setString(\"build\").build")
  }

  test("Phantom types checking - requirments not met FindStr needs string + and input type setPath or Pipelined"){
    assertDoesNotCompile("FindStrBuilder().setString(\"build\").build")
  }

  test("Pipeline compiling - result from IpConfig is fed into findstr which checks Ip whilst ignoring the case"){
    assertCompiles("FindStrBuilder().pipelineFrom(IpConfigBuilder().pipeline).ignoreCase.setString(\"Ip\").build")
  }

}
