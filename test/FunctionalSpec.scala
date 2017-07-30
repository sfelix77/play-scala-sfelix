import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
 * Functional tests start a Play application internally, available
 * as `app`.
 */
class FunctionalSpec extends PlaySpec with GuiceOneAppPerSuite {

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

    "send 200 on a good request" in  {
      route(app, FakeRequest(GET, "/")).map(status(_)) mustBe Some(OK)
    }
  }

  "HomeController" should {

    "render the index page on request                     GET   /" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe Status.OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to world airport Dashboard")
    }

    "render the initial query page on request             GET   /query" in {
      val query = route(app, FakeRequest(GET, "/query")).get

      status(query) mustBe Status.OK
      contentType(query) mustBe Some("text/html")
      contentAsString(query) must include("Please enter a country name or code")
      contentAsString(query) mustNot include("Country not found")
    }

    "render the query page without any result on request  GET   /query?country=ERROR" in {
      val query = route(app, FakeRequest(GET, "/query?country=ERROR")).get

      status(query) mustBe Status.OK
      contentType(query) mustBe Some("text/html")
      contentAsString(query) must include("Country not found")
    }

    "render the query page with results on request        GET   /query?country=FRa" in {
      val query = route(app, FakeRequest(GET, "/query?country=fr")).get

      status(query) mustBe Status.OK
      contentType(query) mustBe Some("text/html")
      contentAsString(query) mustNot include("Country not found")
      contentAsString(query) must include("France")
      contentAsString(query) must include("Charles de Gaulle International Airport")
    }

    "render the query page with results on request        POST  /query  [country=gb]" in {
      val query = route(app, FakeRequest(GET, "/query")).get
      status(query) mustBe Status.OK
      val cookies1 = cookies(query)
      val session1 = session(query)

      val queryPost = route(app, FakeRequest(POST, "/query")
        .withFormUrlEncodedBody(("country","gB"))
        .withSession(session1.data.head)
        .withCookies(cookies1.get("PLAY_SESSION").getOrElse(Cookie("PLAY_SESSION","")))
      ).get

      status(queryPost) mustBe Status.OK
      contentType(queryPost) mustBe Some("text/html")
      contentAsString(queryPost) mustNot include("Country not found")
      contentAsString(queryPost) must include("United Kingdom")
      contentAsString(queryPost) must include("London Heathrow Airport")
    }

    "render JSON with results on request                  GET   /autocomplete?country=mo" in {
      val query = route(app, FakeRequest(GET, "/autocomplete?country=mo")).get

      status(query) mustBe Status.OK
      contentType(query) mustBe Some("application/json")

      val json: JsValue = Json.parse(contentAsString(query))
      val value = ( json \ 7 \ "value" ).as[String]

      value mustBe "MZ"
    }

    "render the reports page with results on request      GET   /reports" in {
      val query = route(app, FakeRequest(GET, "/reports")).get

      status(query) mustBe Status.OK
      contentType(query) mustBe Some("text/html")
      contentAsString(query) must include("Top 10 countries with highest number of airports")
      contentAsString(query) must include("Bottom 10 countries with lowest number of airports")
      contentAsString(query) must include("Top 10 most common runway identifications")
      contentAsString(query) must include("Type of runways per country")
    }
  }

}
