

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
            for (let assetEntry of Object.entries(wallet.assets)){
                if(assetEntry[1] > 0) {
                    let assetDiv = document.createElement('div');
                    assetDiv.className = 'asset';
                    assetDiv.innerHTML = assetEntry[0] + " : " + assetEntry[1];
                    let orderButton = document.createElement('button');
                    orderButton.addEventListener("click", function(){
                        orderSelectedAsset(assetEntry[0]);});
                    orderButton.className = "smallButton";
                    orderButton.innerHTML = "Trade";
                    let buttonDiv = document.createElement('div');
                    buttonDiv.className = "contentpanelRight";
                    buttonDiv.appendChild(orderButton);
                    document.getElementById('assetContainer').appendChild(assetDiv);
                    document.getElementById('assetContainer').appendChild(buttonDiv);
                }
            }
        })
        .catch((error) => { console.error('Error', error) });
}


function orderSelectedAsset(assetText){
    let assetCode = assetText.substr(0,assetText.indexOf(' '));
    let assetName = assetText.split('(').pop().split(')')[0];
    asset = {
        name: assetName,
        code: assetCode,
        currentPrice: '0'
    }
    let assetObject = new Asset(asset);
    localStorage.setItem(CURRENT_ASSET_KEY, JSON.stringify(assetObject));

    window.location.href = "PlaceOrder.html"; //blijft deze in menu?
}

