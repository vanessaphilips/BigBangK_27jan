package com.example.project_bigbangk.service;
/**
@Author Bigbangk
*/

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.*;
import com.example.project_bigbangk.model.DTO.RegistrationDTO;
import com.example.project_bigbangk.repository.RootRepository;
import com.example.project_bigbangk.service.Security.HashService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes the RegistrationDTO, checks the data within and if correct creates client(with hashed PW), wallet(with generated IBAN) and address objects and sends them to rootrepo for storage.
 *uses HashService and IbanGeneratorService
 *
 */
@Service
public class RegistrationService {

    private RootRepository rootRepository;
    //source:https://howtodoinjava.com/java/regex/java-regex-validate-email-address/
    private static final String EMAIL_REGEX = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    //min 8 tekens, geen spaties of tabs
    private static final String PASSWORD_REGEX = "^(?=\\S+$).{8,}$";
    //alleen cijfers precies 9
    private static final String BSN_REGEX = "^[0-9]{9}$";
    //4 cijfers twee letters, spatie er tussen mag
    private static final String POSTAL_REGEX = "^[1-9]{1}[0-9]{3} ?[A-Z]{2}$";
    //precies 3 letters
    private static final String COUNTRY_REGEX = "^[a-zA-Z]{3}$";
    private final int AGE_LIMIT = 18;
    private  final int UPPER_AGE_LIMIT = 150;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private LocalDate convertedDateOfBirth;
    private HashService hashService;
    private IbanGeneratorService ibanGenerator;
    private String inputErrorMessage = "";


    public RegistrationService(HashService hashService, IbanGeneratorService ibanGenerator, RootRepository rootRepository) {
        this.hashService = hashService;
        this.ibanGenerator = ibanGenerator;
        this.rootRepository = rootRepository;
    }

    public enum Messages
    {
        Email("Duplicate Email"),
        Success("Registration Successful"),
        NoInputErrors("No Errors Found");
        private String body;

        Messages(String envBody) {
            this.body = envBody;
        }
        public String getBody() {
            return body;
        }
    }

    /**
     * Checks various parameters to see if a Client can be registered.
     * First checks if an email already exists.
     * if checkRegistrationInput doesn't return any errors it creates wallet and address objects and finaly a client object
     * this client object is stored in the database
     * @param registrationDTO DTO containing all registration parameters
     * @return string for response entity body.
     */
    public String registerClient(RegistrationDTO registrationDTO){
        Client existingClient = rootRepository.findClientByEmail(registrationDTO.getEmail());
        if(existingClient != null){
            return Messages.Email.getBody();
        }
        String checkRegMessage = (checkRegistrationInput(registrationDTO));
        inputErrorMessage = "";
        if(checkRegMessage.equals(Messages.NoInputErrors.getBody())){
            Address address = new Address(registrationDTO.getPostalCode(),registrationDTO.getStreet(), registrationDTO.getNumber(), registrationDTO.getCity(),
                    registrationDTO.getCountry());
            Wallet wallet = BigBangkApplicatie.prototypeWallet.clone();
            wallet.setIban(ibanGenerator.getIban());
            Client client = new Client(registrationDTO.getEmail(), registrationDTO.getFirstName(), registrationDTO.getInsertion(), registrationDTO.getLastName(), convertedDateOfBirth,
                    registrationDTO.getBsn(), hashService.hash(registrationDTO.getPassword()), address, wallet);

            rootRepository.createNewlyRegisteredClient(client);

            return Messages.Success.getBody();
        }else{
            return checkRegMessage;
        }
    }

    /**
     * checks registrationDTO input.
     * failstates are empty strings(except insertion), not matching specific REGEX for postalcode, bsn, email and password,
     * and also if the difference between dateofbirth and now isn't under the
     * @param registrationDTO DTO containing all registration parameters
     * @return string built up of all errors found
     */
    public String checkRegistrationInput(RegistrationDTO registrationDTO){
        inputErrorMessage = "";
        if (checkForEmptyStrings(registrationDTO)){
            inputErrorMessage += "Empty Field ";}
        if (!matchesRegex(registrationDTO.getEmail(), EMAIL_REGEX)){
            inputErrorMessage += "Invalid Email ";}
        if (!matchesRegex(registrationDTO.getPassword(), PASSWORD_REGEX)){
            inputErrorMessage += "Invalid Password ";}
        if (checkUnderAgeLimit(registrationDTO)) {
            inputErrorMessage += "Invalid Date of Birth ";}
        if(!matchesRegex(registrationDTO.getBsn(), BSN_REGEX) || !bsn11Check(registrationDTO.getBsn())){
            inputErrorMessage += "Invalid Bsn ";}
        if(!matchesRegex(registrationDTO.getPostalCode(), POSTAL_REGEX)){
            inputErrorMessage += "Invalid PostalCode ";}
        if(registrationDTO.getNumber() < 1){
            inputErrorMessage += "Invalid House Number ";}
        if(!matchesRegex(registrationDTO.getCountry(), COUNTRY_REGEX)){
            inputErrorMessage += "Invalid Countrycode ";}
        if (inputErrorMessage.length()<1){
            return Messages.NoInputErrors.getBody();
        }
        System.out.println("Errors Found: " + inputErrorMessage);
        return "Errors Found: " + inputErrorMessage;
    }

    private boolean checkUnderAgeLimit(RegistrationDTO registrationDTO) {
        try {
            convertedDateOfBirth = LocalDate.parse(registrationDTO.getDateOfBirth(), formatter);
        }catch(DateTimeParseException e){
            return true;
        }
        int yearsBetween = Period.between(convertedDateOfBirth, LocalDate.now()).getYears();
        return yearsBetween < AGE_LIMIT || yearsBetween > UPPER_AGE_LIMIT;
    }

    /**
     * Puts all string inputs(except addition) into a list to quickly check if theyre not empty.
     * @param registrationDTO DTO containing all registration parameters
     * @return boolean empties or not
     */
    private boolean checkForEmptyStrings(RegistrationDTO registrationDTO) {
        List<String> inputNotNullList = Arrays.asList(registrationDTO.getFirstName(), registrationDTO.getLastName(),
                registrationDTO.getBsn(), registrationDTO.getDateOfBirth(), registrationDTO.getEmail(), registrationDTO.getPassword(), registrationDTO.getStreet(),
                registrationDTO.getPostalCode(), registrationDTO.getCountry(), registrationDTO.getCity());
        boolean foundEmpty = false;

        for (String s : inputNotNullList) {
            if(s == null || s.isEmpty()){
                s = "";
                foundEmpty = true;
            }}
        return  foundEmpty;
    }


    public boolean matchesRegex(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    /**
     * @Author Kelly Speelman - de Jonge
     */
    // doet de 11 check op het bsn nummer
    public boolean bsn11Check(String bsnNummer){
        char[] bsn = bsnNummer.toCharArray();
        int keerGetal = 9;
        int bsnEindGetal = 0;
        for (char c : bsn) {
            bsnEindGetal += (Character.getNumericValue(c) * keerGetal);
            keerGetal--;
            if (keerGetal == 1) {
                keerGetal = -1;
            }
        }
        return (bsnEindGetal % 11) == 0;
    }

}
