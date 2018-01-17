package fr.apside.pizza;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
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
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.apside.pizza.exception.ArgumentException;
import fr.apside.pizza.xml.PizzaOrder;
import fr.apside.pizza.xml.PizzaType;

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
	    list.filter(p -> isValid(p)).forEach(path -> {
		try (InputStream inputStream = Files.newInputStream(path);
			OutputStream outputStream = Files.newOutputStream(Paths.get("target/orders.json"));) {
		    // Lecture XML
		    JAXBContext jc = JAXBContext.newInstance(PizzaOrder.class);
		    Unmarshaller unmarshaller = jc.createUnmarshaller();
		    // Deserialisation
		    PizzaOrder order = (PizzaOrder) unmarshaller.unmarshal(inputStream);

		    listOrders.add(order);

		    // Recuperation de la quantite de la commande
		    BigInteger qte = order.getPizzas().getPizza().stream().map(PizzaType::getQuantity)
			    .reduce(BigInteger.ZERO, BigInteger::add);
		    log.info("Commande de " + qte + " pizzas pour "
			    + order.getCustomer().getName().getFirstName() + " "
			    + order.getCustomer().getName().getLastName());

		} catch (IOException | JAXBException e) {
		    String msg = "Erreur de lecture de l'XML";
		    log.error(msg, e);
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

	    Schema schema = schemaFactory.newSchema(Paths.get("src/main/resources/pizza_order.xsd").toFile());
	    Validator validator = schema.newValidator();
	    validator.validate(new StreamSource(path.toFile()));
	    return true;
	} catch (Exception e) {
	    log.error("Invalid order: " + path, e);
	    return false;
	}
    }
}
