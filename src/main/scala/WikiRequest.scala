import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.JsonMethods.parse

import java.net.{SocketException, SocketTimeoutException}
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

class WikiRequest {
  private val searchedLinqList = new ArrayBuffer[String]()
  var res = ""
  def getLinksJson(pageName: String, lang: String): List[Page] = {
    try {
      val page = requests.get(s"https://$lang.wikipedia.org/w/api.php", params =
        Map(
          "action" -> RequestParams.action,
          "format" -> RequestParams.format,
          "prop" -> RequestParams.prop,
          "titles" -> pageName,
          "generator" -> RequestParams.generator,
          "formatversion" -> RequestParams.formatversion,
          "gplnamespace" -> RequestParams.gplnamespace,
          "gpllimit" -> RequestParams.gpllimit
        )
      )

      res = page.text()
    }catch {
      case socketException: SocketException => println(s"socket exception in $pageName")
      case socketTimeoutException: SocketTimeoutException => println(s"socket timeout exception in $pageName")
      case timeoutException: requests.TimeoutException => println(s"timeout exception in $pageName")
    }

    var pageLinkList = List[Page]()
    if (res != "") {
      val json = parse(res)
      val jArrayPages = (json \ "query" \ "pages")

      implicit val formats: Formats = DefaultFormats
      pageLinkList = jArrayPages.extract[List[Page]]
    }

    pageLinkList
  }

  def linqCountRec(pageName: String, pageLang: String, depthSearch: Int = 1): Int = {
    searchedLinqList.clear()
    searchedLinqList += pageName
    var timeStart = System.currentTimeMillis

    @tailrec
    def lincCount(pageIndex: Int, pageLang: String, levelIndex: Int, depthSearch: Int = 1): Int = {
      analyzePage(searchedLinqList(pageIndex), pageLang)

      var thisLevelIndex = levelIndex
      var depth = depthSearch
      if (thisLevelIndex == 0 || pageIndex == thisLevelIndex) {
        thisLevelIndex = searchedLinqList.length - 1
        depth = depthSearch - 1

        val timeStop = System.currentTimeMillis
        val time = timeStop - timeStart
        println(s"level $levelIndex time $time")
        timeStart = System.currentTimeMillis
      }

      if (depthSearch <= 0)
        levelIndex
      else
        lincCount(pageIndex + 1, pageLang, thisLevelIndex, depth)
    }

    lincCount(0, pageLang, 0, depthSearch)
  }

  def addLinqsInPageList(pageLinkList: List[Page]): Unit = {
    for (item <- pageLinkList) {
      if (!searchedLinqList.contains(item.title) && !item.missing)
        searchedLinqList += item.title
    }
  }

  def linqCount(pageName: String, pageLang: String, depthSearch: Int = 1): Int = {
    searchedLinqList.clear()
    searchedLinqList += pageName
    var i = 0
    var count = 0
    val arrThreads = ArrayBuffer[Thread]()

    while (i < depthSearch) {
      var listSize = searchedLinqList.length
      arrThreads.clear()

      val timeStart = System.currentTimeMillis

      val taskBatch = createTaskBatches(count, pageLang)

      var length = 0
      for (batch <- taskBatch) {
        for (thread <- batch) {
          thread.start()
        }

        for (thread <- batch) {
          thread.join()
        }
        length+=1
        print(s"\r для эпохи $i выполнено $length из ${taskBatch.length} \t")
      }

      val timeStop = System.currentTimeMillis
      val time = timeStop - timeStart
      println(s"level $i time $time мс")

      i += 1
      count = listSize
    }
    searchedLinqList.length - 1
  }


  def analyzePage(pageName: String, pageLang: String): Unit = {
    val links = getLinksJson(pageName, pageLang)

    searchedLinqList.synchronized(
      addLinqsInPageList(links)
    )
  }

  def createTaskBatches(count: Int, pageLang: String): ArrayBuffer[ArrayBuffer[Thread]] = {
    var threadCount = 0
    var threadBatch = ArrayBuffer[Thread]()
    val arrThreads = ArrayBuffer[ArrayBuffer[Thread]]()

    for (index <- count until searchedLinqList.length) {
      val task: Runnable = () => {
        analyzePage(searchedLinqList(index), pageLang)
      }
      val thread = new Thread(task)

      if (threadCount == ParserParam.ThreadCount) {
        threadCount = 0
        arrThreads += threadBatch
        threadBatch = ArrayBuffer[Thread]()
      }

      threadBatch += thread
      threadCount += 1
    }

    if (threadBatch.nonEmpty)
      arrThreads += threadBatch

    arrThreads
  }

}
