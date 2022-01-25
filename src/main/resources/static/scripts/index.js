"use strict"

class LoginDTO {
    constructor(email, password) {
        this.email = email;
        this.password = password;
    }
}

function submitLogin(){
   let email = document.getElementById('email').value;
   let password = document.getElementById('password').value;

   if(!email || !password){
       window.alert("Please fill in both fields.")
   }else {
       const loginDTO = new LoginDTO(email, password);
       sendLoginData(loginDTO);
   }
}

function sendLoginData(lData) {
    fetch(`${rootURL}login`, {
        method: "POST",
        headers: acceptHeaders(),
        body: JSON.stringify(lData)
    })
        .then(async response => {
            if (response.ok) {
                storeToken(await response.json());
                console.log("login successful" + lData.email);
                window.location.href = "menu.html";
            }else {
                console.log("login failed");
            }
        })
}

function storeToken(json) {
    if (json.authorization !== undefined) {
        localStorage.setItem(JWT_KEY, json.authorization);
    }
}
