

class Wallet {
    constructor(bank, iban, balance, assets, transaction, limitSell, limitBuy, stoplossSell, owner)
    {
        this.bank = bank;
        this.iban = iban;
        this.balance = balance;
        this.assets = assets;
        this.transaction = transaction;
        this.limitSell = limitSell;
        this.limitBuy = limitBuy;
        this.stoplossSell = stoplossSell;
        this.owner = owner;
    }
}

let token = localStorage.getItem(JWT_KEY);

let wallet = getWallet();


function getWallet(){
    fetch(`${rootURL}wallet`, {
        method: "GET",
        headers: acceptHeadersWithToken(token),
    })
        .then(response => response.json())
        .then(json => {
            console.log(json);
            let wallet = json;
            document.getElementById("iban").innerHTML = wallet.iban;
            document.getElementById("balance").innerHTML = wallet.balance;
        })
        .catch((error) => { console.error('Error', error) });
}
