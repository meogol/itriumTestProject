object ParserParam {
  var File = ""
  var Category = ""
  var ThreadCount = 10
}

case class Page(pageid: Int,
                title: String,
                missing: Boolean,
                contentmodel: String) {
  def this(title: String,
           missing: Boolean,
           contentmodel: String)
  = this(-1, title, missing, contentmodel)

  def this(pageid: Int,
           title: String,
           contentmodel: String)
  = this(pageid, title, false, contentmodel)

}
