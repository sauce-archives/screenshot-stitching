package org.testobject.screenshotstitching;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

class Websites {
	static List<String> list() {
		String websitesFileName = Optional.ofNullable(System.getenv("WEBSITE_LIST_FILE")).orElse("websites.txt");
		try {
			return Files.readAllLines(Paths.get(websitesFileName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
