package models

/**
  *
  * @param id
  * @param code
  * @param name
  * @param continent
  * @param wikipediaLink
  * @param keywords
  * @param airports
  */
case class Country (
          id: Int,
          code: String,
          name: String,
          continent: String,
          wikipediaLink: Option[String] = None,
          keywords: Set[String] = Set.empty,
          var airports: Option[Seq[Airport]] = None,
  ) {
  require(id>=0 && code.nonEmpty && name.nonEmpty && continent.nonEmpty)
}

/**
  *
  */
object Country {
  def apply(csvLine: Seq[String]) : Option[Country] = {
    try {
      if (csvLine.length >= 5)
        Some(Country(
          id = csvLine.head.toInt,
          code = csvLine(1),
          name = csvLine(2),
          continent = csvLine(3),
          wikipediaLink = if(csvLine(4).nonEmpty) Some(csvLine(4)) else None,
          keywords = if(csvLine.length > 5 && csvLine(5).nonEmpty) csvLine(5).split(",").map(_.trim).toSet else Set.empty,
        ))
      else None
    } catch {
      case ex: NumberFormatException => None
      case ex: IllegalArgumentException => None
      case _: Exception => None
    }
  }
}