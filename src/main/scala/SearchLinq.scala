import java.util
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}
import scala.util.matching.Regex

class SearchLinq {
  def searchLinq(text: String): List[String] ={
    val linkPattern: Regex = "\\[\\[((\\S*?)|((\\S\\s*)*?)\\S)\\]\\]".r
    (linkPattern findAllIn text).toList
  }

  def parsLinq(linqList: List[String]): ArrayBuffer[String] ={
    val formLinqList: ArrayBuffer[String] = new ArrayBuffer()

    for (n <- linqList) {
      val item = n
      if(item.toLowerCase().contains(ParserParam.File)) {
        var res = searchLinq(item.replaceFirst("\\[\\[", ""))
        for(imageLinq <- res)
          formLinqList+=getLinq(imageLinq)
      }
      else if (item.toLowerCase().contains(ParserParam.Category) || item.toLowerCase().contains("file:")
      || item.toLowerCase().contains("image:") || item.toLowerCase().contains("wikt:")) {
        breakable{
          break()
        }
      }
      else
        formLinqList+=getLinq(item)

    }

    def getLinq(item: String): String = {
      val linqPattern = item.split('|')
      val splitPattern = linqPattern(0).split("\\[\\[")
      var linq = splitPattern.last
      linq = linq.replaceAll("]]", "")
      linq = linq.replaceAll(" ", "_")
      linq
    }

    formLinqList
  }
}
