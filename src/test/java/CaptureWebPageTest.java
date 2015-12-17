
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

/**
 * Appium test to screen capture an entire website and save it as a PNG.
 */
public class CaptureWebPageTest {
	private static final String APPIUM_SERVER = getEnvOrDefault("APPIUM_SERVER", "https://app.testobject.com:443/api/appium/wd/hub");
	private static final String TESTOBJECT_DEVICE = getEnvOrDefault("TESTOBJECT_DEVICE", "iPhone_6S_Plus_16GB_real_2");
	private static final String TESTOBJECT_APPIUM_VERSION = getEnvOrDefault("TESTOBJECT_APPIUM_VERSION", "1.4.16");
	private static String TESTOBJECT_API_KEY = getEnvOrDefault("TESTOBJECT_API_KEY", "");
	private static String TESTOBJECT_APP_ID = getEnvOrDefault("TESTOBJECT_APP_ID", "6");

	private static int maxAttempts = 5;
	private static List<String> websites = Websites.list();

	@Test
	public void openWebPageAndTakeScreenshot() throws Exception {
		System.out.println(" --- SCREENSHOT STITCHING --- \n");
		TestObjectRemoteWebDriver driver = setUpDriver();
		for (int i = 0; i < websites.size(); ++i) { // Take a screenshot of every website
			for (int attempt = 0; attempt < maxAttempts; ++attempt) { // Attempt each up to 5 times.
				try {
					takeStitchedScreenshot(driver, i);
					break;
				} catch (Exception e) {
					System.out.println("Failed to take screenshot, exception: " + e.getMessage());
					driver = setUpDriver();
				}
			}
		}
	}

	private void takeStitchedScreenshot(TestObjectRemoteWebDriver driver, int i) throws IOException {
		String url = websites.get(i);
		System.out.println(" Capturing screenshots of " + url + " (" + i + "/" + websites.size() + ")");
		driver.get(url);
		File screenshot = driver.getStitchedScreenshotAs(OutputType.FILE);
		File savedScreenshot = new File(getScreenshotPath(url));
		//noinspection ResultOfMethodCallIgnored
		savedScreenshot.getParentFile().mkdirs();
		FileUtils.copyFile(screenshot, savedScreenshot);
		System.out.println("  Saved screenshot to " + savedScreenshot.getPath());
	}

	private static TestObjectRemoteWebDriver setUpDriver() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("testobject_device", TESTOBJECT_DEVICE);
		capabilities.setCapability("testobject_api_key", TESTOBJECT_API_KEY);
		capabilities.setCapability("testobject_app_id", TESTOBJECT_APP_ID);
		capabilities.setCapability("testobject_appium_version", TESTOBJECT_APPIUM_VERSION);
		capabilities.setCapability("testobject_test_name", "Screenshot Stitching");

		URL endpoint = new URL(APPIUM_SERVER);

		TestObjectRemoteWebDriver driver = new TestObjectRemoteWebDriver(endpoint, capabilities);

		System.out.println("Connected to " + TESTOBJECT_DEVICE + " at " + APPIUM_SERVER);
		System.out.println("Report URL: " + driver.getCapabilities().getCapability("testobject_test_report_url"));
		System.out.println("Live view: " + driver.getCapabilities().getCapability("testobject_test_live_view_url"));
		System.out.println("--------------------");

		return driver;
	}

	protected static String getEnvOrDefault(String environmentVariable, String defaultValue) {
		String value = System.getenv(environmentVariable);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public String getScreenshotPath(String url) {
		String website = url.replace("https://", "")
				.replace("http://", "");
		website = website.substring(0, website.indexOf("/"));
		website = website.replace(".", "");
		return TESTOBJECT_DEVICE + File.separator + website + File.separator + "screenshot-" + Instant.now() + ".png";
	}
}
