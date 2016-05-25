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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import static java.time.LocalDateTime.now;

/**
 * Appium test to screen capture an entire website and save it as a PNG.
 */
public class CaptureWebPage {
	private static final String APPIUM_SERVER = getEnvOrDefault("APPIUM_SERVER", "https://app.testobject.com:443/api/appium/wd/hub");
	private static final String TESTOBJECT_DEVICE = getEnvOrDefault("TESTOBJECT_DEVICE", "iPhone_6S_Plus_16GB_real_ABP_off_private");
	private static final String TESTOBJECT_APPIUM_VERSION = getEnvOrDefault("TESTOBJECT_APPIUM_VERSION", "1.4.16");
	private static final String TESTOBJECT_API_KEY = getEnvOrDefault("TESTOBJECT_API_KEY", "");
	private static final String TESTOBJECT_APP_ID = getEnvOrDefault("TESTOBJECT_APP_ID", "1");

	private static final int maxScreenshotAttempts = 10;
	private static final int maxConnectionAttempts = 10;
	private static final List<String> websites = Websites.list();
	private static final Map<String, String> failedWebsites = new HashMap<>();

	private static boolean skipExistingScreenshots = false;

	public static void main(String... args) throws Exception {
		skipExistingScreenshots = Arrays.asList(args).contains("skipExistingScreenshots");
		CaptureWebPage test = new CaptureWebPage();
		test.openWebPageAndTakeScreenshot();
	}

	private void openWebPageAndTakeScreenshot() throws Exception {
		Instant beginTime = Instant.now();
		log(" --- SCREENSHOT STITCHING (" + TESTOBJECT_DEVICE + ") --- \n");

		TestObjectRemoteWebDriver driver = setUpDriver();
		try {
			takeScreenshots(driver);
		} catch (Throwable e) {
			log("An uncaught exception occurred.");
			e.printStackTrace();
			throw e;
		}

		Duration duration = Duration.between(beginTime, Instant.now());
		log("\nAll tests completed. Duration: " + duration.toMinutes() + "min");
		log("Failed to take screenshots of: " + Collections.singletonList(failedWebsites.entrySet()));
	}

	private void takeScreenshots(TestObjectRemoteWebDriver driver) throws InterruptedException, MalformedURLException {
		for (int websiteIndex = 0; websiteIndex < websites.size(); ++websiteIndex) { // Take a screenshot of every website
			boolean screenshotSucceeded = false;
			for (int attempt = 1; attempt <= maxScreenshotAttempts; ++attempt) { // Attempt each up to 5 times.
				try {
					takeStitchedScreenshot(driver, websiteIndex);
					driver.manage().deleteAllCookies();
					screenshotSucceeded = true;
					break;
				} catch (Throwable e) {
					log("Failed to take screenshot (attempt " + attempt + "), exception: " + e.getMessage());
					if (e.getMessage().contains("DOM Exception 18")) {
						failedWebsites.put(websites.get(websiteIndex), "Website inaccessible");
						screenshotSucceeded = true;
						break;
					}
					Thread.sleep(30 * 1000);
					driver = setUpDriver();
				}
			}
			if (!screenshotSucceeded) {
				failedWebsites.put(websites.get(websiteIndex), "UNKNOWN");
			}
		}
	}

	private void takeStitchedScreenshot(TestObjectRemoteWebDriver driver, int index) throws IOException {
		String url = websites.get(index);
		String filename = getScreenshotPath(url);
		log(" Capturing screenshots of " + url + " (" + (index+1) + "/" + websites.size() + ")");
		if (skipExistingScreenshots() && Files.exists(Paths.get(filename))) {
			log("   File " + filename + " already exists, skipping");
			return;
		}

		driver.get(url);
		File screenshot = driver.getStitchedScreenshotAs(OutputType.FILE);
		File savedScreenshot = new File(getScreenshotPath(url));

		try {
			//noinspection ResultOfMethodCallIgnored
			savedScreenshot.getParentFile().mkdirs();
			FileUtils.copyFile(screenshot, savedScreenshot);
			log("  Saved screenshot to " + savedScreenshot.getPath());
		} catch (IOException e) {
			log("  Took screenshot but failed to save to " + savedScreenshot.getAbsolutePath() + ", " + e.getMessage());
		}
	}

	private static void log(String s) {
		System.out.println(now(ZoneId.of("Europe/Berlin")) + " " + s);
	}

	private static TestObjectRemoteWebDriver setUpDriver() throws MalformedURLException {
		Throwable lastException = null;
		for (int attempt = 1; attempt <= maxConnectionAttempts; ++attempt) {
			try {
				DesiredCapabilities capabilities = new DesiredCapabilities();
				capabilities.setCapability("testobject_device", TESTOBJECT_DEVICE);
				capabilities.setCapability("testobject_api_key", TESTOBJECT_API_KEY);
				capabilities.setCapability("testobject_app_id", parseAppId(TESTOBJECT_APP_ID));
				capabilities.setCapability("testobject_appium_version", TESTOBJECT_APPIUM_VERSION);
				capabilities.setCapability("testobject_test_name", "Screenshot Stitching");

				URL endpoint = new URL(APPIUM_SERVER);

				TestObjectRemoteWebDriver driver = new TestObjectRemoteWebDriver(endpoint, capabilities);

				log("Connected to " + TESTOBJECT_DEVICE + " at " + APPIUM_SERVER);
				log("Report URL: " + driver.getCapabilities().getCapability("testobject_test_report_url"));
				log("Live view: " + driver.getCapabilities().getCapability("testobject_test_live_view_url"));
				log("--------------------");

				return driver;
			} catch (Throwable e) {
				log("Failed to set up TestObject driver, attempt " + attempt);
				lastException = e;
			}
		}
		throw new RuntimeException("Failed to set up TestObject driver too many times. Perhaps device is unavailable?", lastException);
	}

	private static String[] parseAppId(String appIdString) {
		return appIdString.split(",");
	}

	private static boolean skipExistingScreenshots() {
		String skipEnvironmentVariable = getEnvOrDefault("SKIP_EXISTING_SCREENSHOTS", "false").toLowerCase();
		return skipExistingScreenshots || Boolean.parseBoolean(skipEnvironmentVariable);
	}

	private static String getEnvOrDefault(String environmentVariable, String defaultValue) {
		String value = System.getenv(environmentVariable);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	private static String getScreenshotPath(String url) throws UnsupportedEncodingException {
		String strippedProtocol = url.replace("http://", "http.").replace("https://", "https.");
		String domain = strippedProtocol.substring(0,
				strippedProtocol.contains("/") ? strippedProtocol.indexOf("/") : strippedProtocol.length());
		String path = strippedProtocol.substring(strippedProtocol.indexOf("/") + 1);
		domain = URLEncoder.encode(domain, "UTF-8");
		path = URLEncoder.encode(path, "UTF-8");
		if (path.length() > 128) {
			path = path.substring(0, 128) + "...";
		}
		return TESTOBJECT_DEVICE + File.separator + domain + File.separator + path + ".png";
	}
}
