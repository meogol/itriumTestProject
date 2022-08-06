import scala.util.control.Breaks.{break, breakable}

object Main {
  def main(args: Array[String]): Unit = {
    val api = new WikiRequest()

    var resNoRec = api.linqCount("Сахар", "ru", 3)
    println(s"searched linqs no rec $resNoRec")

//    var resRec = api.linqCountRec("Сахар", "ru", 1)
//    println(s"res rec $resRec")


  }
}