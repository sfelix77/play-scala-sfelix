package models

/**
  *
  * @param countries
  * @param airports
  * @param runways
  */
case class Catalog(countries: Seq[Country], airports: Seq[Airport], runways: Seq[Runway])

object Catalog {}