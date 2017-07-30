package controllers

import models._
import javax.inject._

import play.api.Logger
import play.api.i18n._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

/**
  *
  * @param cc
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  private val _countryForm: Form[String] = Form(single("country" -> nonEmptyText(2)))

  private val _catalog: Catalog = {
    val catalog = Try(AppConfig.catalog)
    if(catalog.isFailure) {
      Logger.error("test " + catalog.failed.get.getMessage)
      Catalog()
    } else {
      catalog.get
    }
  }

  private def _countryFilter(country: Country, value: String, isLazy: Boolean) : Boolean = {
    val _value = value.toLowerCase

    (value.length == 2 && country.code.toLowerCase == _value) ||
      ((isLazy || value.length > 2) && (country.name.toLowerCase == _value || country.name.toLowerCase.startsWith(_value)))
  }

  /** Home
    *
    * @return home page
    */
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  /** GET /query?country=
    *
    * @param country
    * @return query page if param country is empty else query page with the country found and this airports
    */
  def query(country: Option[String]) = Action { implicit request =>
    if(country.isEmpty)
      Ok(views.html.query(_countryForm))
    else {
      val countryAirports = _findCountryAirports(country.get)
      Ok(views.html.query(_countryForm, true, countryAirports._1, countryAirports._2))
    }

  }

  /** POST /query
    *
    * @return query page with the country found and this airports
    */
  def airportsByCountry = Action { implicit request =>
    _countryForm.bindFromRequest.fold(
      formErrors => BadRequest(views.html.query(formErrors)),
      country => {
        val countryAirports = _findCountryAirports(country)
        Ok(views.html.query(_countryForm, true, countryAirports._1, countryAirports._2))
      }
    )
  }

  /** GET /reports
    *
    * @return reports page
    */
  def reports = Action { implicit request =>

    val countriesSorted = {
      if (_catalog.countries.nonEmpty)
        _catalog.countries.sortWith(_.airports.getOrElse(Seq.empty).length > _.airports.getOrElse(Seq.empty).length)
      else Seq.empty
    }

    // 10 countries with highest number of airports
    // --> Option[Seq[Country]]
    val countriesTop10 = {
      if(countriesSorted.nonEmpty)
        Some(countriesSorted.take(10))
      else None
    }

    // 10 countries with lowest number of airports
    // --> Option[Seq[Country]]
    val countriesBottom10 = {
      if(countriesSorted.nonEmpty)
        Some(countriesSorted.takeRight(10))
      else None
    }

    // Type of runways (as indicated in "surface" field) per country
    // --> Option[Seq[(Country, Set[String])]]
    val typeOfRunawayPerCountry = {
      if (_catalog.countries.nonEmpty)
        Some(_catalog.countries.map(country =>
          (country,                                     // tuple 1 -->  Country
            country.airports.getOrElse(Seq.empty)       // tuple 2 -->  Seq[Country]
              .flatMap(_.runways.getOrElse(Seq.empty))  //              Seq[Runway]
              .map(_.surface).filter(_.nonEmpty)        //              Seq[String]
              .toSet.toSeq.sortWith(_<_)
          )))
      else None
    }

    // top 10 most common runway identifications (indicated in "lowEnd.ident" field)
    // ---> Option[Seq[(String, Int)]]
    val runwayIdentTop10 = {
      if (_catalog.runways.nonEmpty)
        Some(_catalog.runways
          .filter(_.lowEnd.ident.nonEmpty)
          .groupBy(_.lowEnd.ident)
          .map(gr => (gr._1.get, gr._2.size))
          .toSeq
          .sortWith(_._2 > _._2)
          .take(10))
      else None
    }

    Ok(views.html.reports(countriesTop10, countriesBottom10, typeOfRunawayPerCountry, runwayIdentTop10))
  }

  case class CountryAutoComplete (value: String, label: String)
  implicit val countryAutoComplete = Json.format[CountryAutoComplete]

  /** GET /autocomplete/?country=
    *
    * @param country
    * @return list of country found (JSON format)
    */
  def autocomplete(country: String) = Action.async { implicit request =>
    _countryForm.bindFromRequest.fold(
      formWithErrors => {
        Future(Ok(Json.toJson(List.empty[CountryAutoComplete])))
      },
      data => {
        Future { _catalog.countries
          .filter(_countryFilter(_, data, true))
          .map(c => CountryAutoComplete(c.code, c.name))
          .sortWith((u,v) =>
            if(u.value.toLowerCase == data.toLowerCase) {true}
            else if(v.value.toLowerCase == data.toLowerCase) {false}
            else {u.label < v.label})
        }
        .map(countries => Ok(Json.toJson(countries)))
        .recover {
          case NonFatal(t) =>
            Ok(Json.toJson(List.empty[CountryAutoComplete]))
        }
      }
    )
  }


  private def _findCountryAirports(country: String): (Option[Country], Seq[Airport]) = {
    val countries = _catalog.countries.filter(_countryFilter(_, country, false)).sortWith(_.name < _.name)
    val countryFound = countries.headOption
    val airportsFound = {
      if(countryFound.nonEmpty) {
        countryFound.get.airports.getOrElse(Seq.empty)
      }
      else Seq.empty[Airport]
    }

    (countryFound, airportsFound)
  }
}
