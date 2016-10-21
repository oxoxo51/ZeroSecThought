import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HomeController" should {

    "render the index page" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Your new application is ready.")
    }

  }

  "CountController" should {

    "return an increasing count" in {
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "0"
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "1"
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "2"
    }

  }

  "LoginController" should {
    "render login page" in {
      val home = route(app, FakeRequest(GET, "/login")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("ログイン画面")
    }
    "login and redirect to MemoList" in {
      val home = route(app, FakeRequest(POST, "/login")).get

      status(home) mustBe SEE_OTHER // redirect
    }
  }

  "ThoughtMemoListController" should {
    "render memo list page" in {
      val home = route(app, FakeRequest(GET, "/list")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("メモ一覧")
    }
  }

  "EditThoughtMemoController" should {
    
  }

}
