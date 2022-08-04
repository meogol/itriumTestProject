import requests.{RequestFailedException, TimeoutException, UnknownHostException}

import java.net.{BindException, SocketException}
import java.util.Dictionary
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

class WikiRequest {
  private val searcher = new SearchLinq()
  private val searchedLinqList = new ArrayBuffer[String]()
  private val analyseTimeList = collection.mutable.Map[Int, Long]()

  def getAnalyseTimeList = analyseTimeList

  private def getPage(pageName: String, pageLang: String): String = {
    try {
      val page = requests.get(s"https://$pageLang.wikipedia.org/w/rest.php/v1/page/$pageName")
      page.text()
    }
    catch {
      case e: RequestFailedException => {
        println(s"errorLinq. Page name $pageName")
        "404"
      }
      case hostException: UnknownHostException => {
        println(s"network error")
        "400"
      }
      case timeoutException: TimeoutException =>{
        println(s"timeout request $pageName")
        "410"
      }
      case bindException: BindException =>{
        println(s"Address already in use: no further information in page $pageName")
        "415"
      }
      case socketException: SocketException => {
        println(s"Connection reset in page $pageName. Drop network.")
        "420"
      }
    }
  }

  def linqCountRec(pageName: String, pageLang: String, depthSearch: Int = 1): Int = {
    val listLen = searchedLinqList.length
    val timeStart = System.currentTimeMillis

    analyzePage(pageName, pageLang)

    val timeStop = System.currentTimeMillis
    val time = timeStop - timeStart
    val thisLevelTime = analyseTimeList.getOrElse(depthSearch, 0L)
    val resultLevelTime = thisLevelTime + time
    analyseTimeList += (depthSearch -> resultLevelTime)

    val count = depthSearch - 1
    val arrThreads = ArrayBuffer[Thread]()
    if (count > 0) {
      for (i <- (listLen to (searchedLinqList.length - 1))) {
        val page = searchedLinqList(i)
        val task: Runnable = () => linqCountRec(page, pageLang, count)
        val thread = new Thread(task)

        arrThreads += thread
        thread.start()

      }
    }

    for (i <- arrThreads)
      i.join()

    searchedLinqList.length
  }

  def addLinqsInPageList(page: String): Unit = {
    val searchedLinqPatterns = searcher.searchLinq(page)
    val linqList = searcher.parsLinq(searchedLinqPatterns)

    for (item <- linqList) {
      if (!searchedLinqList.contains(item))
        searchedLinqList += item
    }
  }

  def linqCount(pageName: String, pageLang: String, depthSearch: Int = 1): Int = {
    searchedLinqList.clear()
    var i = 0
    searchedLinqList += pageName
    var count = 0
    val arrThreads = ArrayBuffer[Thread]()

    while (i < depthSearch) {
      var listSize = searchedLinqList.length
      arrThreads.clear()

      val timeStart = System.currentTimeMillis

      val taskBatch = createTaskBatches(count, pageLang)

      for(batch <- taskBatch) {
        for (thread <- batch) {
          thread.start()
        }

        for (thread <- batch) {
          thread.join()
        }
      }

      val timeStop = System.currentTimeMillis
      val time = timeStop - timeStart
      println(s"level $i time $time")

      i += 1
      count = listSize
    }
    searchedLinqList.length
  }


  def analyzePage(pageName: String, pageLang: String): Unit = {
    val page = getPage(pageName, pageLang)
    if (page == "404" ||  page == "400" ||  page == "410" ||  page == "415" ||  page == "420") {
      breakable {
        break
      }
    }

    searchedLinqList.synchronized(
      addLinqsInPageList(page)
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
        arrThreads += threadBatch
        threadBatch = ArrayBuffer[Thread]()
      }

      threadBatch += thread
      threadCount += 1
    }

    if(threadBatch.nonEmpty)
      arrThreads += threadBatch

    arrThreads
  }

}
