# Pizza Order

**Objectif:** Lire plusieurs fichiers XML respectant la XSD fourni en entrée (dossier d'entrée paramétrable via un fichier de configuration donné en paramètre de l'application) et afficher le nombre de pizzas commandés par client (nom et prénom) et générer un fichier commun JSON qui contient toutes les commandes.
Penser à réaliser les test unitaires et utiliser les loggers.

	src/main/resources/pizza-order.xsd

**Entree:** Path (indique dans un fichier de properties) qui pointe sur les fichiers XML de commande de pizzas
**Sortie:** Path JSON qui merge les donnees en entree

---

***Keyword***

java.nio.Path

java.util.Properties

Lib JSON ou Jackson

JAXB

---

**Structure**

	src/main/resources/config.properties

qui contient la clé/valeur  :  dossier.in=/un/directory/qui/va/bien

---
***Plugin Maven à ajouter:***

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<build>
		<pluginManagement>
			<plugins>
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-compiler-plugin</artifactId>
					<version>3.7.0</version>
					<configuration>
						<source>${java.version}</source>
						<target>${java.version}</target>
					</configuration>
				</plugin>
			</plugins>
		</pluginManagement>

		<plugins>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>jaxb2-maven-plugin</artifactId>
				<version>2.3.1</version>
				<executions>
					<execution>
						<id>xjc</id>
						<goals>
							<goal>xjc</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<sources>
						<source>src/main/resources/pizza_order.xsd</source>
					</sources>
					<packageName>fr.apside.pizza.xml</packageName>
				</configuration>
			</plugin>
		</plugins>
	</build>
