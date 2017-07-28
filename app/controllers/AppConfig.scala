package controllers

import models._

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import scala.io.Source
import com.github.tototoshi.csv.CSVParser

/**
  *
  */
object AppConfig {

  lazy val catalog: Catalog = {

    if (_countriesCsvUri.isEmpty)
      throw new Exception("URI to countries.csv is not defined in config")
    if (_airportsCsvUri.isEmpty)
      throw new Exception("URI to airports.csv is not defined in config")
    if (_runwaysCsvUri.isEmpty)
      throw new Exception("URI to runways.csv is not defined in config")

    val countries = _loadCsv(_countriesCsvUri.get, Country.apply)
    val airports = _loadCsv(_airportsCsvUri.get, Airport.apply)
    val runways = _loadCsv(_runwaysCsvUri.get, Runway.apply)

    if (countries.isEmpty)
      throw new Exception("countries.csv is empty or corrupted")
    if (airports.isEmpty)
      throw new Exception("airports.csv is empty or corrupted")
    if (runways.isEmpty)
      throw new Exception("runways.csv is empty or corrupted")

    val runwaysByAirport: Map[String, Seq[Runway]] = runways.groupBy(_.airportIdent)
    val airportsByCountry: Map[String, Seq[Airport]] = airports.groupBy(_.isoCountry)

    for (country <- countries) {
      country.airports = airportsByCountry.get(country.code)
    }

    for (airport <- airports) {
      airport.runways = runwaysByAirport.get(airport.ident)
    }

    Catalog(countries, airports, runways)
  }

  private val _config: Config = ConfigFactory.load

  private lazy val _countriesCsvUri: Option[String] = _getConfigString("countriesCsvUri")
  private lazy val _airportsCsvUri: Option[String] = _getConfigString("airportsCsvUri")
  private lazy val _runwaysCsvUri: Option[String] = _getConfigString("runwaysCsvUri")

  /**
    *
    * @param uri
    * @param build
    * @tparam T
    * @return
    */
  private def _loadCsv[T](uri: String, build: (Seq[String]) => Option[T]): Seq[T] = {
    try {
      val stream = getClass.getResourceAsStream(uri)
      val lines = Source.fromInputStream(stream).getLines

      lines.drop(1)
        .map(CSVParser.parse(_, '\\', ',', '"'))
        .filter(_.nonEmpty)
        .map(_.get.map(_.trim))
        .map(build(_))
        .filter(_.nonEmpty)
        .map(_.get)
        .toSeq
    } catch {
      case ex: Exception => throw ex //TODO log error
    }
  }

  private val _catchMissingOrWrongType = util.control.Exception.catching(classOf[ConfigException.Missing], classOf[ConfigException.WrongType])

  private def _getConfigString(path: String): Option[String] = _catchMissingOrWrongType opt _config.getString(path)

}
