import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.http.HttpMethod;

import java.net.URL;

public class TestObjectRemoteWebDriver extends RemoteWebDriver {
	private static final String SCREENSHOT_STITCH_COMMAND = "stitchedScreenshot";

	public TestObjectRemoteWebDriver(URL remoteAddress, Capabilities desiredCapabilities) {
		super(new TestObjectHttpCommandExecutor(remoteAddress), desiredCapabilities);

	}

	public <X> X getStitchedScreenshotAs(OutputType<X> outputType) throws WebDriverException {
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

	private static class TestObjectHttpCommandExecutor extends HttpCommandExecutor {
		public TestObjectHttpCommandExecutor(URL remoteAddress) {
			super(remoteAddress);
			defineCommand(SCREENSHOT_STITCH_COMMAND, new CommandInfo("/session/:sessionId/stitchedScreenshot", HttpMethod.GET));
		}
	}
}
