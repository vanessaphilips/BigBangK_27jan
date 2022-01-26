
class RegistrationDTO{
    constructor(email,password, firstName, insertion, lastName,
    bsn, dateOfBirth, postalCode,street, number, city, country)
{
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.insertion = insertion;
    this.lastName = lastName;
    this.bsn = bsn;
    this.dateOfBirth = dateOfBirth;
    this.postalCode = postalCode;
    this.street = street;
    this.number = number;
    this.city = city;
    this.country = country;
    }
}

const MIN_AGE = 18;
const MAX_AGE = 150;

$( document ).ready(function() {
    document.getElementById('passwordReg').addEventListener('focusout', checkPassword);

    document.getElementById('postalCode').addEventListener('focusout', checkAddress);

    document.getElementById('number').addEventListener('focusout', checkAddress);

    // document.getElementById('submitForm').addEventListener('click', prepareRegistration);
});


//generates minimum and maximum date entry based on current date.
window.onload = function() {
    let inp = document.getElementById('dateOfBirth');
    let maxday = new Date();
    maxday.setFullYear(maxday.getFullYear() - MIN_AGE);
    let minday = new Date();
    minday.setFullYear(minday.getFullYear() -(MAX_AGE));
    inp.max =  dateToString(maxday);
    inp.min =  dateToString(minday);
    inp.defaultValue = dateToString(new Date(2000, 11, 12));
    // Debug
    console.log(inp.outerHTML);
}

function dateToString(date){

    return date.getFullYear()+'-'+(date.getMonth()+1)+'-'+date.getDate();
}

function checkPassword(){
    let regex = new RegExp(/^\S.{8,}$/);
    let password = document.getElementById('passwordReg').value;

    if (regex.test(password)){
        document.getElementById('passwordReg').classList.remove('error');
        document.getElementById('passwordError').style.display = 'none';
        document.getElementById('passwordOK').style.display = 'block';
    }else{
        document.getElementById('passwordReg').classList.add('error');
        document.getElementById('passwordOK').style.display = 'none';
        document.getElementById('passwordError').style.display = 'block';
    }
}

function checkAddress(){
    let regex = new RegExp(/^[1-9][0-9]{3}[\s]?[A-Za-z]{2}$/i);

    let postcode = document.getElementById('postalCode').value
    let huisnummer = document.getElementById('number').value

    console.log('pc is valide: ' + regex.test(postcode))

    if(regex.test(postcode) && huisnummer){
        let formData = `postcode=${postcode}&number=${huisnummer}`

        fetch("https://postcode.tech/api/v1/postcode?" + formData , {
            headers: {
                'Authorization': 'Bearer 9565619a-9760-47d8-8f6d-e789d63b60ca',
            },
        })
            .then(response => response.json())
            .then(json => {
                processAddress(json)
            })
            .catch((error) => { console.error('Error', error) });
    }
}

function processAddress(data) {
    console.log(data)
    let addressPart = data;
    if (addressPart.hasOwnProperty('message')) {
        document.getElementById('postalError').style.display = 'block';
        document.getElementById('postalCode').classList.add('error');
        document.getElementById('number').classList.add('error');
    } else {
        document.getElementById('postalError').style.display = 'none';
        document.getElementById('city').value = addressPart.city; // zonder validatie
        document.getElementById('street').value = addressPart.street; // zonder validatie
        document.getElementById('postalCode').classList.remove('error');
        document.getElementById('number').classList.remove('error');
        }
}

function prepareRegistration() {
        let registration = new RegistrationDTO(
        document.getElementById('emailReg').value.trim(),
        document.getElementById('passwordReg').value,
        document.getElementById('firstName').value.trim(),
        document.getElementById('insertion').value.trim(),
        document.getElementById('lastName').value.trim(),
        document.getElementById('bsn').value.trim(),
        document.getElementById('dateOfBirth').value.trim(),
        document.getElementById('postalCode').value.trim(),
        document.getElementById('street').value,
        parseInt(document.getElementById('number').value),
        document.getElementById('city').value,
        document.getElementById('country').value
    );
    sendRegistrationData(registration);
}

function sendRegistrationData(rData){
    fetch(`${rootURL}register`, {
        method: "POST",
        headers: acceptHeaders(),
        body: JSON.stringify(rData)
    }).then(response => {
        if (response.status === 201) {
            console.log("Succesfull registration user: " + rData.email);
            response.text().then((message) => {showTimedWindow(message, 500)});
            setTimeout(() => {
                window.location.href = "index.html";
            }, 500);
        } else if (response.status === 409) {
            console.log("User already in database: " + rData.email)
            response.text().then((message) => {showWindow(message)});
        } else {
            response.text().then((message) => {showWindow(message)});
            console.log("Registration Failed: Missing or incorrect fields. Check if your BSN is valid.");
        }
    })
}

