
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Appium test to screen capture an entire website and save it as a PNG.
 */
@RunWith(Parameterized.class)
public class CaptureWebPageTest {
	private static final String APPIUM_SERVER = getEnvOrDefault("APPIUM_SERVER", "https://app.testobject.com:443/api/appium/wd/hub");
	private static final String TESTOBJECT_DEVICE = getEnvOrDefault("TESTOBJECT_DEVICE", "iPhone_6S_Plus_16GB_real_2");
	private static final String TESTOBJECT_APPIUM_VERSION = getEnvOrDefault("TESTOBJECT_APPIUM_VERSION", "1.4.16");
	private static String TESTOBJECT_API_KEY = getEnvOrDefault("TESTOBJECT_API_KEY", "");
	private static String TESTOBJECT_APP_ID = getEnvOrDefault("TESTOBJECT_APP_ID", "6");

	private static TestObjectRemoteWebDriver driver;
	private final List<String> websites;

	public CaptureWebPageTest(List<String> websites) {
		this.websites = websites;
	}

	@Parameterized.Parameters
	public static Iterable getWebsiteLists() {
		List<List<String>> websiteLists = new ArrayList<List<String>>();
		List<String> allWebsites = Websites.list();
		int subListSize = 10;
		for (int i = 0; i < allWebsites.size(); i += subListSize) {
			int toIndex = i + subListSize < allWebsites.size() ? i + subListSize : allWebsites.size() - 1;
			websiteLists.add(allWebsites.subList(i, toIndex));
		}
		return websiteLists;
	}

	@Before
	public void setup() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("testobject_device", TESTOBJECT_DEVICE);
		capabilities.setCapability("testobject_api_key", TESTOBJECT_API_KEY);
		capabilities.setCapability("testobject_app_id", TESTOBJECT_APP_ID);
		capabilities.setCapability("testobject_appium_version", TESTOBJECT_APPIUM_VERSION);
		capabilities.setCapability("testobject_test_name", "Screenshot Stitching");

		URL endpoint = new URL(APPIUM_SERVER);

		driver = new TestObjectRemoteWebDriver(endpoint, capabilities);

		System.out.println("Connected to " + TESTOBJECT_DEVICE + " at " + APPIUM_SERVER + ".");

		System.out.println("Report URL: " + driver.getCapabilities().getCapability("testobject_test_report_url"));
		System.out.println("Live view: " + driver.getCapabilities().getCapability("testobject_test_live_view_url"));
		System.out.println("--------------------");
	}

	@After
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void openWebPageAndTakeScreenshot() throws Exception {
		for (int i = 0; i < websites.size(); ++i){
			String url = websites.get(i);
			System.out.println(" Capturing screenshots of " + url + "(" + i + "/" + websites.size() + ")");
			driver.get(url);
			File screenshot = driver.getStitchedScreenshotAs(OutputType.FILE);
			File savedScreenshot = new File(getScreenshotPath(url));
			//noinspection ResultOfMethodCallIgnored
			savedScreenshot.getParentFile().mkdirs();
			FileUtils.copyFile(screenshot, savedScreenshot);
			System.out.println("  Saved screenshot to " + savedScreenshot.getPath());
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

	public String getScreenshotPath(String url) {
		String website = url.replace("https://", "")
				.replace("http://", "");
		website = website.substring(0, website.indexOf("/"));
		website = website.replace(".", "");
		return TESTOBJECT_DEVICE + File.separator + website + File.separator + "screenshot-" + Instant.now() + ".png";
	}
}
