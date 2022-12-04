package kweb.state.render

import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import kweb.*
import kweb.state.ObservableList
import kweb.state.render
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions

@ExtendWith(SeleniumJupiter::class)
class RenderObservableListTest {
    companion object {
        private lateinit var renderTestApp: RenderObservableListTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            renderTestApp = RenderObservableListTestApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            renderTestApp.server.close()
        }

        //selenium-jupiter will automatically fall back if the first browser it tries doesn't work
        //https://bonigarcia.github.io/selenium-jupiter/#generic-driver
        @Options
        var chromeOptions = ChromeOptions().apply {
            setHeadless(true)
        }

        @Options
        var firefoxOptions = FirefoxOptions().apply {
            setHeadless(true)
        }
    }

    @Test
    fun initialRender(driver: ChromeDriver) {
        driver.get("http://localhost:7659/")

        val h1s = driver.findElements(By.tagName("H1"))
        val h2s = driver.findElements(By.tagName("H2"))
        h1s.size shouldBe 1
        h2s.size shouldBe 1
        h1s.first().text shouldBe "12"
        h2s.first().text shouldBe "123456"
    }

    @Test
    fun testChangeValue(driver: ChromeDriver) {
        driver.get("http://localhost:7659/")

        renderTestApp.outerList.retainAll(listOf(5, 6, 7))
        renderTestApp.innerList.retainAll(listOf(8, 9))

        val h1s = driver.findElements(By.tagName("H1"))
        val h2s = driver.findElements(By.tagName("H2"))
        h1s.size shouldBe 1
        h2s.size shouldBe 1
        h1s.first().text shouldBe "567"
        h2s.first().text shouldBe "56789"
    }
}

class RenderObservableListTestApp {
    val outerList = ObservableList(listOf(1, 2))
    val innerList = ObservableList(listOf(3, 4, 5, 6))

    val server: Kweb = Kweb(port = 7659) {
        doc.body {
            render(outerList) { outer ->
                h1().text(outer.joinToString(""))
                render(innerList) { inner ->
                    h2().text(
                        outer.joinToString("") +
                                inner.joinToString("")
                    )
                }
            }
        }
    }
}
