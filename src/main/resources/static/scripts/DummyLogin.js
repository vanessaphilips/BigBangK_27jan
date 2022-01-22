/**
 * registrates a user and uses the registration to login
 * use this class for development purpose only
 */
class LoginDTO {
    constructor(email, password) {
        this.email = email;
        this.password = password;
    }
}

class RegistrationDTO {
    constructor(email, password, firstName, insertion, lastName,
                bsn, dateOfBirth, postalCode, street, number, city, country) {
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

const loginDTO = new LoginDTO("henk@uncom.nl", "password1234345")

async function register() {
    let registration = new RegistrationDTO(
        "henk@uncom.nl",
        "password1234345",
        "Henk",
        "de",
        "Kort",
        "123434546",
        "1950-01-01",
        "1111BN",
        "Straatie",
        9,
        "Muiden",
        "NLD"
    );
    await fetch(`${rootURL}register`, {
        method: "POST",
        headers: acceptHeaders(),
        body: JSON.stringify(registration)
    }).then(response => {
        if (response.status === 201) {
            console.log("Succesfull registration user" + registration)
        } else if (response.status === 409) {
            console.log("User already in database" + registration.email)
        } else {
            console.log("Bad registration" + response.body)
        }
    })
}

async function getToken() {
    await register()
    await fetch(`${rootURL}login`, {
        method: "POST",
        headers: acceptHeaders(),
        body: JSON.stringify(loginDTO)
    })
        .then(response => {
            if (response.ok) {
                return response.json()
            }
        }).then((json) => {
            if (json.authorization !== undefined) {
                localStorage.setItem(JWT_KEY, json.authorization)
                console.log("login succes" + loginDTO.email);
            } else {
                console.log("login failed");
            }
        })
}



