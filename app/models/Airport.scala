package models

/** Airport Model
  *
  * @param id
  * @param ident
  * @param category
  * @param name
  * @param latitudeDegree
  * @param longitudeDegree
  * @param elevationFeet
  * @param continent
  * @param isoCountry
  * @param isoRegion
  * @param municipality
  * @param isScheduledService
  * @param gpsCode
  * @param iataCode
  * @param localCode
  * @param homeLink
  * @param wikipediaLink
  * @param keywords
  * @param runways
  */
case class Airport (
          id: Int,
          ident: String,
          category: String,
          name: String,
          latitudeDegree: Double,
          longitudeDegree: Double,
          elevationFeet: Option[Int],
          continent: String,
          isoCountry: String,
          isoRegion: String,
          municipality: Option[String],
          isScheduledService: Option[Boolean],
          gpsCode: Option[String],
          iataCode: Option[String],
          localCode: Option[String],
          homeLink: Option[String],
          wikipediaLink: Option[String],
          keywords: Set[String],
          var runways: Option[Seq[Runway]] = None,
  ) {
  require(id>=0 && ident.nonEmpty && category.nonEmpty && name.nonEmpty && continent.nonEmpty && isoCountry.nonEmpty && isoRegion.nonEmpty)
}

/** Airport Companion
  * Define apply from csv line parsed
  */
object Airport {
  def apply(csvLine: Seq[String]) : Option[Airport] = {
    try {
      if (csvLine.length >= 17)
        Some(Airport(
          id = csvLine.head.toInt,
          ident = csvLine(1),
          category = csvLine(2),
          name = csvLine(3),
          latitudeDegree = csvLine(4).toDouble,
          longitudeDegree = csvLine(5).toDouble,
          elevationFeet = if(csvLine(6).nonEmpty) Some(csvLine(6).toInt) else None,
          continent = csvLine(7),
          isoCountry = csvLine(8),
          isoRegion = csvLine(9),
          municipality = if (csvLine(10).nonEmpty) Some(csvLine(10)) else None,
          isScheduledService = if (csvLine(11) == "yes") Some(true) else if (csvLine(11) == "no") Some(false) else None,
          gpsCode = if (csvLine(12).nonEmpty) Some(csvLine(12)) else None,
          iataCode = if (csvLine(13).nonEmpty) Some(csvLine(13)) else None,
          localCode = if (csvLine(14).nonEmpty) Some(csvLine(14)) else None,
          homeLink = if (csvLine(15).nonEmpty) Some(csvLine(15)) else None,
          wikipediaLink = if (csvLine(16).nonEmpty) Some(csvLine(16)) else None,
          keywords = if (csvLine.length > 17 && csvLine(17).nonEmpty) csvLine(17).split(",").map(_.trim).toSet else Set.empty,
        ))
      else None
    } catch {
      case ex: NumberFormatException => None
      case ex: IllegalArgumentException => None
      case _: Exception => None
    }
  }
}
