object ParsFile extends Enumeration {
  type Field = Value
  val RU = Value("файл")
  val ENG = Value("file")
}

object ParsCategories extends Enumeration {
  type Field = Value
  val RU = Value("категория")
  val ENG = Value("category")
}

object ParserParam{
  var File = ""
  var Category = ""
  var ThreadCount = 1
}