package Util

object Functional {

  // Recursion using Y combinator
  def Y[A,B](f: (A=>B)=>(A=>B)) = {
    case class W(wf: W=>A=>B) {
      def apply(w: W) = wf(w)
    }
    val g: W=>A=>B = w => f(w(w))(_)
    g(W(g))
  }

  // Lambda funttion for column selection. Input for filter
  val Column: Seq[String] => Array[String] => Boolean = (c:Seq[String]) => (x:Array[String]) => {if(c.contains(x(0))) true else false}


}
