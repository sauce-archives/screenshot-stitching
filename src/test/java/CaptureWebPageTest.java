
import io.appium.java_client.ios.IOSDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Appium test to screen capture an entire website and save it as a PNG.
 */
@RunWith(Parameterized.class)
public class CaptureWebPageTest {
	private static final String APPIUM_SERVER = getEnvOrDefault("APPIUM_SERVER", "https://app.testobject.com:443/api/appium/wd/hub");
	private static final String TESTOBJECT_DEVICE = getEnvOrDefault("TESTOBJECT_DEVICE", "iPad_Air_2_16GB_real");
	private static final String TESTOBJECT_APPIUM_VERSION = getEnvOrDefault("TESTOBJECT_APPIUM_VERSION", "1.3.7");
	private static String TESTOBJECT_API_KEY = getEnvOrDefault("TESTOBJECT_API_KEY", "");
	private static String TESTOBJECT_APP_ID = getEnvOrDefault("TESTOBJECT_APP_ID", "1");

	private static Instant startTime = null; // Used for screenshot folder
	private IOSDriver driver;
	private final String url;

	public CaptureWebPageTest(String url) {
		this.url = url;
	}

	@Parameterized.Parameters
	public static Iterable websites() {
		return Arrays.asList(
			new String[][] {
					{ "http://www.bbc.co.uk" },
					{ "http://www.stuff.co.nz" },
				    { "http://www.yahoo.com" }
			}
		);
	}

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

		if (startTime == null) {
			startTime = Instant.now();
		}
	}

	@Test
	public void openWebPageAndTakeScreenshot() throws Exception {
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.get(url);

		Number headerHeight = (Number)driver.executeScript("return screen.height - window.innerHeight;");
		Number footerHeight = 0;

		takeScreenshot(headerHeight.intValue(), footerHeight.intValue());
	}

	private void takeScreenshot(int top, int bottom) throws Exception {
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
			final File localScreenshot = new File(parentFolder + "/" + getFilename());
			localScreenshot.getParentFile().mkdirs();
			ImageIO.write(screenshot.getImage(), "PNG", localScreenshot);
		} catch (Exception e) {
			System.out.println("Exception while saving screenshot: " + e.getMessage());
			throw e;
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

	public String getFilename() {
		String filename = url;
		filename = filename.replace("https://", "");
		filename = filename.replace("http://", "");
		filename = filename.replaceAll("\\.", "");
		return filename + ".png";
	}
}
