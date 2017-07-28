package models

/**
  *
  * @param id
  * @param airportRef
  * @param airportIdent
  * @param lengthFeet
  * @param widthFeet
  * @param surface
  * @param isLighted
  * @param isClosed
  * @param lowEnd
  * @param highEnd
  */
case class Runway (
          id: Int,
          airportRef: Int,
          airportIdent: String,
          lengthFeet: Option[Int],
          widthFeet: Option[Int],
          surface: String,
          isLighted: Boolean,
          isClosed: Boolean,
          lowEnd: RunwayEnd,
          highEnd: RunwayEnd,
  ) {
  require(id>=0 && airportRef>=0 && airportIdent.nonEmpty && lowEnd != null && highEnd != null)
}

/**
  *
  */
object Runway {
  def apply(csvLine: Seq[String]) : Option[Runway] = {
    try {
      if (csvLine.length == 20)
        Some(Runway(
          id = csvLine.head.toInt,
          airportRef = csvLine(1).toInt,
          airportIdent = csvLine(2),
          lengthFeet = if(csvLine(3).nonEmpty) Some(csvLine(3).toInt) else None,
          widthFeet = if(csvLine(4).nonEmpty) Some(csvLine(4).toInt) else None,
          surface = csvLine(5),
          isLighted = csvLine(6) == "1",
          isClosed = csvLine(7) == " 1",
          lowEnd = RunwayEnd(
            ident = if (csvLine(8).nonEmpty) Some(csvLine(8)) else None,
            latitudeDegree = if (csvLine(9).nonEmpty) Some(BigDecimal(csvLine(9))) else None,
            longitudeDegree = if (csvLine(10).nonEmpty) Some(BigDecimal(csvLine(10))) else None,
            elevationFeet = if (csvLine(11).nonEmpty) Some(csvLine(11).toInt) else None,
            headingDegT = if (csvLine(12).nonEmpty) Some(BigDecimal(csvLine(12))) else None,
            displacedThreshold = if (csvLine(13).nonEmpty) Some(csvLine(13).toInt) else None,
          ),
          highEnd = RunwayEnd(
            ident = if (csvLine(14).nonEmpty) Some(csvLine(14)) else None,
            latitudeDegree = if (csvLine(15).nonEmpty) Some(BigDecimal(csvLine(15))) else None,
            longitudeDegree = if (csvLine(16).nonEmpty) Some(BigDecimal(csvLine(16))) else None,
            elevationFeet = if (csvLine(17).nonEmpty) Some(csvLine(17).toInt) else None,
            headingDegT = if (csvLine(18).nonEmpty) Some(BigDecimal(csvLine(18))) else None,
            displacedThreshold = if (csvLine(19).nonEmpty) Some(csvLine(19).toInt) else None,
          )
        ))
      else None
    } catch {
      case ex: NumberFormatException => None
      case ex: IllegalArgumentException => None
      case _: Exception => None
    }
  }
}

