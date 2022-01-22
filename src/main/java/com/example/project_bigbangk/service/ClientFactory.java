package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Address;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.RegistrationDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;

/**
 * a random client generator. it generates a client and sends it to
 * registrationService where it is checked and saved to the DB
 * It uses:
 * 10.000 different firstnames
 * 10.000 different lastnames
 * all places in the Netherlands
 * some streetnames and email providers/companys
 * @author Pieter Jan Bleichrodt
 */
@Service
public class ClientFactory {

    private final Logger logger = LoggerFactory.getLogger(ClientFactory.class);
    private Map<String, String> lastNames;
    private List<String> firstNames;
    private Set<String> cities;
    private List<String> streetNames;
    private RegistrationService registrationService;

    private List<String> providers = Arrays.asList("Chello", "xs4All", "Yahoo", "Gmail", "Wanadoo", "T-Mobile", "Samsung", "work", "Pixelpool");
    private List<String> EMAIL_EXTENSIONS = Arrays.asList("nl", "com", "de", "be", "biz", "fr", "vs", "org", "uk");
    private final String[] COUNTRIES = {"NLD", "BEL", "GER", "USA", "GBR", "FRA", "POL", "CHE"};
    private final Random RAND = new Random();
    private final String PASSWORD = "password";

    public ClientFactory(IbanGeneratorService ibanGeneratorService, RegistrationService registrationService) {
        super();
        logger.info("New ClientFactory");
        firstNames = new ArrayList<>();
        lastNames = new HashMap<>();
        this.registrationService = registrationService;
    }

    private RegistrationDTO createRegistrationDTO() {

        String firstName = firstNames.get(RAND.nextInt(firstNames.size()));
        String lastName = (String) (lastNames.keySet().toArray()[RAND.nextInt(lastNames.size())]);
        String insertion = lastNames.get(lastName);
        LocalDate birthDay = LocalDate.parse(createBirthDay());
        String email = createEmail(firstName);
        String BSN = creatBsn();
        Address address = createAdress();
        return new RegistrationDTO(email,
                PASSWORD,
                firstName,
                insertion,
                lastName, BSN,
                birthDay.toString(),
                createAdress().getPostalCode(),
                address.getStreet(),
                address.getNumber(),
                address.getCity(),
                address.getCountry());
    }

    /**
     * seeds the dataBase with a number of dummy clients.
     * @param numberOfClients the number of dummy clients to be generated
     */
    public void seedDataBase(int numberOfClients) {
        logger.info(String.format("Database seeding started for %s clients", numberOfClients));
        initializeNameArrays();
        initializeAdressArrays();
        for (int i = 0; i < numberOfClients; i++) {
            registrationService.registerClient(createRegistrationDTO());
        }
        logger.info("Database seeding end");
    }

    private void initializeAdressArrays() {
        this.cities = initializePlaatsnamen();
        this.streetNames = initializeStreetNames();
    }

    private List<String> initializeStreetNames() {
        File streetNames = new File("src/main/resources/ClientFactoryResources/straatnamen.txt");
        List<String> tempStreetNames = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(streetNames);
            while (scanner.hasNext()) {
                String streetname = scanner.nextLine();
                streetname = streetname.trim();
                tempStreetNames.add(streetname);
            }
        } catch (FileNotFoundException e) {
            logger.info(e.getMessage());
        }
        return tempStreetNames;
    }


    private Address createAdress() {
        String postalCode = createPostalCode();
        String street = streetNames.get(RAND.nextInt(streetNames.size()));
//        number tot 300, omdat er een grens moet zijn
        int number = RAND.nextInt(300) + 1;
        String country = COUNTRIES[RAND.nextInt(COUNTRIES.length)];
        String city = (String) cities.toArray()[RAND.nextInt(cities.size())];
        return new Address(postalCode, street, number, city, country);
    }

    private String createPostalCode() {
        //getal van vier cijfers
        return String.format("%s%s%s", (RAND.nextInt(9000) + 1000),
                Character.toUpperCase((char) (RAND.nextInt(26) + 65)),
                Character.toUpperCase((char) (RAND.nextInt(26) + 65)));
    }

    private String createBirthDay() {
        //vanaf 1 tot 13
        //leeftijd max 88 jaar
        int jaar = RAND.nextInt(70) + 1932;
        // 4, maar niet door 100 – tenzij het jaartal restloos deelbaar door 400 is. 2020
        int maand = RAND.nextInt(12) + 1;
        int dag;
        //bepaal jaar==schrikkeljaar
        if ((jaar % 4 == 0 && jaar % 100 != 0) || jaar % 400 == 0) {
            dag = RAND.nextInt(29) + 1;
        } else {
            dag = RAND.nextInt(28) + 1;
        }
        return String.format("%s-%s-%s", jaar, makeDoubleNumber(maand), makeDoubleNumber(dag));
    }

    private String makeDoubleNumber(int number) {
        if ((String.valueOf(number).length() == 1)) {
            return String.format("0%s", number);
        }
        return String.valueOf(number);
    }

    private String creatBsn() {
        //getal vanaf 1000 tot 10000
        String firstHalf = String.valueOf(RAND.nextInt(9000) + 1000);
        StringBuilder reversed = new StringBuilder();
        for (int i = firstHalf.length() - 1; i >= 0; i--) {
            reversed.append(firstHalf.charAt(i));
        }
        return String.format("%s%s%s", firstHalf, reversed, "0");
    }

    private String createEmail(String firstName) {
        return String.format("%s@%s.%s", firstName,
                providers.get(RAND.nextInt(providers.size())),
                EMAIL_EXTENSIONS.get(RAND.nextInt(EMAIL_EXTENSIONS.size()))
        );
    }

    private void initializeNameArrays() {
        lastNames = initializeLastNames();
        firstNames = initializeFirstNames();

    }

    public Map<String, String> initializeLastNames() {
        File lastNamesFile = new File("src/main/resources/ClientFactoryResources/LastNames.json");
        Map<String, String> tempLastNames = new HashMap<>();
        JsonNode lastNamesNode;
        ObjectMapper mapper = new ObjectMapper();
        try {
            lastNamesNode = mapper.readTree(lastNamesFile);
            lastNamesNode = lastNamesNode.findValue("record");
            for (JsonNode lastNameJN : lastNamesNode) {
                String lastName = lastNameJN.get("naam").asText();
                String nameNorm = Normalizer.normalize(lastName, Normalizer.Form.NFD);
                if (!lastName.equals(nameNorm)) {
                    nameNorm = nameNorm.replaceAll("[^\\p{ASCII}]", "");
                }
                tempLastNames.put(nameNorm, lastNameJN.get("prefix").asText());
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return tempLastNames;
    }

    private Set<String> initializePlaatsnamen() {
        File cityFile = new File("src/main/resources/ClientFactoryResources/metatopos-places.json");
        Set<String> tempCities = new HashSet<>();
        JsonNode cityNodes;
        ObjectMapper mapper = new ObjectMapper();
        try {
            cityNodes = mapper.readTree(cityFile);
            cityNodes = cityNodes.findValue("places");
            for (JsonNode cityname : cityNodes) {
                tempCities.add(cityname.get("municipality").asText());
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return tempCities;
    }

    public List<String> initializeFirstNames() {
        File lastNamesFile = new File("src/main/resources/ClientFactoryResources/FirstNames.json");
        List<String> tempListFirstNames = new ArrayList<>();
        JsonNode lastNamesNode;
        ObjectMapper mapper = new ObjectMapper();
        try {
            lastNamesNode = mapper.readTree(lastNamesFile);
            JsonNode lastNamesNodes = lastNamesNode.findValue("database");
            lastNamesNodes = lastNamesNodes.findValue("record");
            for (JsonNode firstNameJN : lastNamesNodes) {
                String firstName = firstNameJN.get("voornaam").asText();
                String nameNorm = Normalizer.normalize(firstName, Normalizer.Form.NFD);
                if (!firstName.equals(nameNorm)) {
                    nameNorm = nameNorm.replaceAll("[^\\p{ASCII}]", "");
                }
                tempListFirstNames.add(nameNorm);
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return tempListFirstNames;
    }

}