import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Websites {
	public static List<String> list() {
		String websitesFileName = Optional.ofNullable(System.getenv("website_list_file")).orElse("websites.txt");
		try {
			return Files.readAllLines(Paths.get(websitesFileName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
