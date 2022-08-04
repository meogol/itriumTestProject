import scala.util.control.Breaks.{break, breakable}

object Main {
  def main(args: Array[String]): Unit = {
    val api = new WikiRequest()
    loadLang("RU")
//    var res = api.linqCountRec("Сахар", "ru", 2)
//    println(s"searched linqs $res")

    var resNoRec = api.linqCount("Сахар", "ru", 3)
    println(s"searched linqs no rec $resNoRec")

//    for(key <- api.getAnalyseTimeList.keys)
//      println(s"level ${2-key} time ${api.getAnalyseTimeList(key)}")

  }

  def loadLang(lang:String): Unit ={
    if(lang=="RU"){
      ParserParam.File=ParsFile.RU.toString
      ParserParam.Category = ParsCategories.RU.toString

    }
  }
}