package models

/** Catalog Model (Countries --> Airports --> Runways
  *
  * @param countries
  * @param airports
  * @param runways
  */
case class Catalog(countries: Seq[Country] = Seq.empty, airports: Seq[Airport] = Seq.empty, runways: Seq[Runway] = Seq.empty)

object Catalog {}