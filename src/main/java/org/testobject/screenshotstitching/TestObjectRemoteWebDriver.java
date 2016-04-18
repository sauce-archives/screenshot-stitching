package org.testobject.screenshotstitching;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.http.HttpMethod;

import java.net.URL;

class TestObjectRemoteWebDriver extends RemoteWebDriver {
	private static final String SCREENSHOT_STITCH_COMMAND = "stitchedScreenshot";
	private static final String RESET_COMMAND = "reset";
	private static final String CLOSE_COMMAND = "closeApp";

	TestObjectRemoteWebDriver(URL remoteAddress, Capabilities desiredCapabilities) {
		super(new TestObjectHttpCommandExecutor(remoteAddress), desiredCapabilities);
	}

	<X> X getStitchedScreenshotAs(OutputType<X> outputType) throws WebDriverException {
		Response response = execute(SCREENSHOT_STITCH_COMMAND);
		Object result = response.getValue();
		if (result instanceof String) {
			String base64EncodedPng = (String) result;
			return outputType.convertFromBase64Png(base64EncodedPng);
		} else if (result instanceof byte[]) {
			String base64EncodedPng = new String((byte[]) result);
			return outputType.convertFromBase64Png(base64EncodedPng);
		} else {
			throw new RuntimeException(String.format("Unexpected result for %s command: %s",
					SCREENSHOT_STITCH_COMMAND,
					result == null ? "null" : result.getClass().getName() + " instance"));
		}
	}

	public void resetApp() {
		execute(RESET_COMMAND);
	}

	public void closeApp() {
		execute(CLOSE_COMMAND);
	}

	private static class TestObjectHttpCommandExecutor extends HttpCommandExecutor {

		TestObjectHttpCommandExecutor(URL remoteAddress) {
			super(remoteAddress);
			defineCommand(SCREENSHOT_STITCH_COMMAND, new CommandInfo("/session/:sessionId/stitchedScreenshot", HttpMethod.GET));
			defineCommand(RESET_COMMAND, new CommandInfo("/session/:sessionId/appium/app/reset", HttpMethod.POST));
			defineCommand(CLOSE_COMMAND, new CommandInfo("/session/:sessionId/appium/app/close", HttpMethod.POST));
		}
	}
}
