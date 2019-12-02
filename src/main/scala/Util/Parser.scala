package Util

object Parser {

  /**
   * Parses the output of tasklist
   * @param value = String result from executing of tasklist
   * @return 2d array which each column represents a value e.g. PID or Image Name
   */
  def parseTaskList(value:String): Array[Array[String]] = {
    value.split("\\n").map(i => i.slice(1, i.length-2).split("\",\"")).transpose
  }

}
