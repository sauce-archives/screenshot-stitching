package org.testobject.screenshotstitching;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Appium test to screen capture an entire website and save it as a PNG.
 */
public class CaptureWebPage {
	private static final String APPIUM_SERVER = getEnvOrDefault("APPIUM_SERVER", "https://app.testobject.com:443/api/appium/wd/hub");
	private static final String TESTOBJECT_DEVICE = getEnvOrDefault("TESTOBJECT_DEVICE", "iPhone_6S_Plus_16GB_real_ABP_off_private");
	private static final String TESTOBJECT_APPIUM_VERSION = getEnvOrDefault("TESTOBJECT_APPIUM_VERSION", "1.4.16");
	private static final String TESTOBJECT_API_KEY = getEnvOrDefault("TESTOBJECT_API_KEY", "");
	private static final String TESTOBJECT_APP_ID = getEnvOrDefault("TESTOBJECT_APP_ID", "1");

	private static final int maxAttempts = 5;
	private static final List<String> websites = Websites.list();

	public static void main(String... args) throws Exception {
		CaptureWebPage test = new CaptureWebPage();
		test.openWebPageAndTakeScreenshot();
	}

	private void openWebPageAndTakeScreenshot() throws Exception {
		Instant beginTime = Instant.now();
		System.out.println(" --- SCREENSHOT STITCHING (" + TESTOBJECT_DEVICE + ") --- \n");

		TestObjectRemoteWebDriver driver = setUpDriver();
		for (int i = 0; i < websites.size(); ++i) { // Take a screenshot of every website
			for (int attempt = 1; attempt <= maxAttempts; ++attempt) { // Attempt each up to 5 times.
				try {
					takeStitchedScreenshot(driver, i);
					driver.manage().deleteAllCookies();
					break;
				} catch (Throwable e) {
					System.out.println("Failed to take screenshot (attempt " + attempt + "), exception: " + e.getMessage());
					Thread.sleep(30 * 1000);
					driver = setUpDriver();
				}
			}
		}

		Duration duration = Duration.between(beginTime, Instant.now());
		System.out.println("\nAll tests completed. Duration: " + duration.toMinutes() + "min");
	}

	private void takeStitchedScreenshot(TestObjectRemoteWebDriver driver, int i) throws IOException {
		String url = websites.get(i);
		System.out.println(" Capturing screenshots of " + url + " (" + (i+1) + "/" + websites.size() + ")");
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
		capabilities.setCapability("testobject_app_id", parseAppId(TESTOBJECT_APP_ID));
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

	private static String[] parseAppId(String appIdString) {
		return appIdString.split(",");
	}

	private static String getEnvOrDefault(String environmentVariable, String defaultValue) {
		String value = System.getenv(environmentVariable);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	private String getScreenshotPath(String url) throws UnsupportedEncodingException {
		String strippedProtocol = url.replace("http://", "http.").replace("https://", "https.");
		String domain = strippedProtocol.substring(0, strippedProtocol.indexOf("/"));
		String path = strippedProtocol.substring(strippedProtocol.indexOf("/") + 1);
		domain = URLEncoder.encode(domain, "UTF-8");
		path = URLEncoder.encode(path, "UTF-8");
		return TESTOBJECT_DEVICE + File.separator + domain + File.separator + path + ".png";
	}
}
