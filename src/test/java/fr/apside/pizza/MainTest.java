package fr.apside.pizza;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.apside.pizza.exception.ArgumentException;

public class MainTest {

    private static Logger log = LoggerFactory.getLogger(MainTest.class);
    private Path result = Paths.get("target/orders.json");

    @Before
    public void setup() throws IOException {
	if (Files.exists(result)) {
	    try {
		Files.delete(result);
	    } catch (IOException e) {
		log.error("Erreur d'init");
		throw e;
	    }
	}
    }

    @Test(expected = NoSuchFileException.class)
    public void should_fail_for_wrong_parameter() throws Exception {
	String[] args = { "incorrect/config/path" };
	Main.main(args);
	fail("Should thrown an exception");
    }

    @Test(expected = ArgumentException.class)
    public void should_fail_for_null_parameter() throws Exception {
	Main.main(null);
	fail("Should thrown an exception");
    }

    @Test
    public void should_run_without_error() throws IOException {
	String[] args = { "src/main/resources/config.properties" };
	try {
	    Main.main(args);
	} catch (Exception e) {
	    fail("Should not thrown any exception");
	}

	assertTrue(Files.exists(result));

	String content = new String(Files.readAllBytes(result));
	assertTrue(content.contains("Morane"));
	assertTrue(content.contains("Lucky"));
	assertTrue(content.contains("rue des oiseaux"));
    }

}
