import org.openqa.selenium.By
import org.scalatest.{FlatSpec, Matchers}
import org.openqa.selenium.chrome.ChromeDriver
import org.scalatest.selenium.WebBrowser
import org.openqa.selenium.remote.DesiredCapabilities

case class IntegrationFixture(
  driver: ChromeDriver,
  host: String
)

class IntegrationTests extends FlatSpec with Matchers with WebBrowser {

  ignore should "add to dashboard" in fixture {fx =>
    import fx._
    driver.get(host)
    driver.findElement(By.linkText("Dashboard")).click()

    val dropdown = driver.findElement(By.name("newCurrency"))
    dropdown.click()
    Thread.sleep(1000)
    driver.findElement(By.xpath("/html/body/app-root/div/div/app-charts/div/div/div[2]/div/select/option[2]")).click()

    driver.findElement(By.cssSelector(".col-1")).click()

    Thread.sleep(1000)
    val charts = driver.findElements(By.xpath("//*[@id=\"cdk-drop-list-0\"]/div"))
    charts.size() shouldEqual 1

    driver.findElement(By.xpath("//*[@id=\"cdk-drop-list-0\"]/div/div[1]/div/button")).click()
    Thread.sleep(1000)
    val chartsAfterDelete = driver.findElements(By.xpath("//*[@id=\"cdk-drop-list-0\"]/div/div[2]/canvas"))
    chartsAfterDelete.size() shouldEqual 0

  }


  ignore should "estimate profit" in fixture { fx =>
    import fx._

    driver.get(host)
    driver.findElement(By.linkText("Profit estimation")).click()

    val dropdown = driver.findElement(By.name("currency"))
    dropdown.click()
    Thread.sleep(1000)
    dropdown.findElement(By.xpath("/html/body/app-root/div/div/app-profit-estimation/div/div[2]/div/div/select/option[2]")).click()

    driver.findElement(By.id("mat-input-0")).sendKeys("06/20/2019")
    driver.findElement(By.id("mat-input-1")).sendKeys("06/25/2019")
    driver.findElement(By.cssSelector(".form-control:nth-child(9)")).sendKeys("10")
    driver.findElement(By.cssSelector(".form-control:nth-child(12)")).sendKeys("10000")
    driver.findElement(By.cssSelector(".btn")).click()
    Thread.sleep(1000)
    val elements = driver.findElements(By.xpath("/html/body/app-root/div/div/app-profit-estimation/div/div[2]/div/div[1]/div/div"))
    elements.size() shouldEqual 1
  }

  def fixture (action: IntegrationFixture => Unit): Any = {
    System.setProperty("webdriver.chrome.driver", "conf/chromedriver74")
    val driver = new ChromeDriver(DesiredCapabilities.chrome())
    val host = "http://localhost:4200/"

    action(IntegrationFixture(driver, host))
  }
}