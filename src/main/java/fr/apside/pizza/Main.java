package fr.apside.pizza;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.apside.pizza.exception.ArgumentException;
import fr.apside.pizza.xml.PizzaOrder;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

	if (args == null || args.length != 1) {
	    String msg = "L'application n'accepte qu'un seul argument. Le fichier de config";
	    log.error(msg);
	    throw new ArgumentException(msg);
	}

	String configFile = args[0];
	Properties prop = new Properties();
	try (InputStream configInputStream = Files.newInputStream(Paths.get(configFile));) {
	    prop.load(configInputStream);
	} catch (IOException e) {
	    log.error("Erreur de lecture de la config", e);
	    throw e;
	}

	List<PizzaOrder> listOrders = new ArrayList<>();

	try (Stream<Path> list = Files.list(Paths.get(prop.getProperty("directory.in")));) {
	    list.forEach(path -> {
		if (isValid(path)) {
		    try (InputStream inputStream = Files.newInputStream(path);
			    OutputStream outputStream = Files.newOutputStream(Paths.get("target/orders.json"));) {
			// Lecture XML
			JAXBContext jc = JAXBContext.newInstance(PizzaOrder.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			PizzaOrder order = (PizzaOrder) unmarshaller.unmarshal(inputStream);

			listOrders.add(order);

			log.info("Commande de " + order.getPizzas().getPizza().size() + " pizzas pour "
				+ order.getCustomer().getName().getFirstName() + " "
				+ order.getCustomer().getName().getLastName());

		    } catch (IOException | JAXBException e) {
			String msg = "Erreur de lecture de l'XML";
			log.error(msg, e);
		    }
		}
	    });
	}

	// Conversion en JSON
	ObjectMapper mapper = new ObjectMapper();

	// Convert object to JSON string and save into a file directly
	try (OutputStream outputStream = Files.newOutputStream(Paths.get("target/orders.json"));) {
	    mapper.writeValue(outputStream, listOrders);
	}

    }

    private static boolean isValid(Path path) {
	try {
	    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	    DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document document = parser.parse(path.toFile());

	    Schema schema = schemaFactory.newSchema(Paths.get("src/main/resources/pizza_order.xsd").toFile());
	    Validator validator = schema.newValidator();
//	    TODO validator.validate(new DOMSource(document));
	    return true;
	} catch (Exception e) {
	    log.error("Invalid order: " + path, e);
	}
	return false;
    }
}
