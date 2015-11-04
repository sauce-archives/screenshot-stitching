
import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.cutter.FixedCutStrategy;

import javax.imageio.ImageIO;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Appium test to screen capture an entire website and save it as a PNG.
 */
public class CaptureWebPageTest {
	private static final String APPIUM_SERVER = getEnvOrDefault("APPIUM_SERVER", "https://app.testobject.com:443/api/appium/wd/hub");
	private static final String TESTOBJECT_DEVICE = getEnvOrDefault("TESTOBJECT_DEVICE", "iPad_3_16GB_real");
	private static final String TESTOBJECT_APPIUM_VERSION = getEnvOrDefault("TESTOBJECT_APPIUM_VERSION", "1.3.7");
	private static String TESTOBJECT_API_KEY = getEnvOrDefault("TESTOBJECT_API_KEY", "");
	private static String TESTOBJECT_APP_ID = getEnvOrDefault("TESTOBJECT_APP_ID", "1");

	private Instant startTime; // Used for screenshot folder

	private IOSDriver driver;

	@Before
	public void setup() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("testobject_device", TESTOBJECT_DEVICE);
		capabilities.setCapability("testobject_api_key", TESTOBJECT_API_KEY);
		capabilities.setCapability("testobject_app_id", TESTOBJECT_APP_ID);
		capabilities.setCapability("testobject_appium_version", TESTOBJECT_APPIUM_VERSION);

		URL endpoint = new URL(APPIUM_SERVER);

		driver = new IOSDriver(endpoint, capabilities);

		if (driver != null) {
			System.out.println(driver.getCapabilities().getCapability("testobject_test_report_url"));
			System.out.println(driver.getCapabilities().getCapability("testobject_test_live_view_url"));
		}

		startTime = Instant.now();
	}

	@Test
	public void openWebpageAndTakeScreenshot() throws InterruptedException {
		String url = "http://www.bbc.co.uk";
		driver.get(url);

		long headerHeight = (long)driver.executeScript("return screen.height - window.innerHeight;");
		long footerHeight = 0;

		takeScreenshot((int)headerHeight, (int)footerHeight);
	}

	private void takeScreenshot(int top, int bottom) {
		System.out.println("Taking screenshots, and cropping " + top + "px of header and " + bottom + "px of footer");
		try {
			final Screenshot screenshot = new AShot()
					.shootingStrategy(ShootingStrategies.viewportRetina(500, new FixedCutStrategy(top, bottom), 2))
					.takeScreenshot(driver);
			final String parentFolder = DateTimeFormatter
					.ofPattern("yyyy-MM-dd-HH-mm-ss")
					.withLocale(Locale.GERMAN)
					.withZone(ZoneId.systemDefault())
					.format(startTime);
			final File localScreenshot = new File(parentFolder + "/screenshot.png");
			localScreenshot.getParentFile().mkdirs();
			ImageIO.write(screenshot.getImage(), "PNG", localScreenshot);
		} catch (Exception e) {
			System.out.println("Exception while saving screenshot: " + e.getMessage());
		}
	}

	protected static String getEnvOrDefault(String environmentVariable, String defaultValue) {
		String value = System.getenv(environmentVariable);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	class CropArea {
		final int top;
		final int bottom;

		CropArea(int top, int bottom) {
			this.top = top;
			this.bottom = bottom;
		}
	}

}
