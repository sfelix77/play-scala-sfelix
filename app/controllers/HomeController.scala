package controllers

import models._

import javax.inject._
import play.api.i18n._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

/**
  *
  * @param cc
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  private val _countryForm: Form[String] = Form(single("country" -> nonEmptyText(2)))

  private def _countryFilter(country: Country, value: String) : Boolean = {
    val _value = value.toLowerCase

    (value.length == 2 && country.code.toLowerCase == _value) ||
      (value.length > 2 && (country.name.toLowerCase == _value || country.name.toLowerCase.startsWith(_value)))
  }

  /**
    *
    * @return
    */
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  /**
    *
    * @return
    */
  def query = Action { implicit request =>
    Ok(views.html.query(_countryForm))
  }

  /**
    *
    * @return
    */
  def airportsByCountry = Action { implicit request =>
    val formResult = _countryForm.bindFromRequest

    formResult.fold(
      formErrors => BadRequest(views.html.query(formErrors)),
      data => {
        val countries = AppConfig.catalog.countries.filter(_countryFilter(_, data))
        val country = countries.headOption
        val airports = {
          if(country.nonEmpty) {
            country.get.airports.getOrElse(Seq.empty)
          }
          else Seq.empty[Airport]
        }

        Ok(views.html.query(_countryForm, country, airports))
      }
    )
  }

  /**
    *
    * @return
    */
  def reports = Action { implicit request =>

    val countriesSorted = {
      if (AppConfig.catalog.countries.nonEmpty)
        AppConfig.catalog.countries.sortWith(_.airports.getOrElse(Seq.empty).length > _.airports.getOrElse(Seq.empty).length)
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
      if (AppConfig.catalog.countries.nonEmpty)
        Some(AppConfig.catalog.countries.map(country =>
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
      if (AppConfig.catalog.runways.nonEmpty)
        Some(AppConfig.catalog.runways
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
}
