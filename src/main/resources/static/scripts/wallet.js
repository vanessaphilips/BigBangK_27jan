

class walletDTO {
    constructor(iban, balance, assets, totalWorth, freeBalance)
    {
        this.iban = iban;
        this.balance = balance;
        this.assets = assets;
        this.totalWorth = totalWorth;
        this.freeBalance = freeBalance;
    }
}

let token = localStorage.getItem(JWT_KEY);


getWallet();

/**fetches wallet and shows assets owned by client including buttons to trade with said assets*/
function getWallet(){
    fetch(`${rootURL}wallet`, {
        method: "GET",
        headers: acceptHeadersWithToken(token),
    })
        .then(response => response.json())
        .then(json => {
            console.log(json);
            let walletDTO = json;
            document.getElementById("iban").innerHTML = walletDTO.iban;
            document.getElementById("totalWorth").innerHTML = walletDTO.totalWorth + " €";
            document.getElementById("balance").innerHTML = walletDTO.balance + " €";
            document.getElementById("freeBalance").innerHTML = walletDTO.freeBalance + " €";
            for (let assetEntry of Object.entries(walletDTO.assets)){
                if(assetEntry[1] > 0) {
                    let assetDiv = document.createElement('div');
                    assetDiv.className = 'asset';
                    assetDiv.innerHTML = assetEntry[0] + " : " + assetEntry[1];
                    let orderButton = document.createElement('button');
                    orderButton.addEventListener("click", function(){
                        orderSelectedAsset(assetEntry[0]);});
                    orderButton.className = "smallButton";
                    orderButton.innerHTML = "Trade";
                    assetDiv.appendChild(orderButton);
                    document.getElementById('assetContainer').appendChild(assetDiv);
                }
            }
        })
        .catch((error) => { console.error('Error', error) });
}

/**sets the currently selected asset variable to the asset the button belongs to and goes to order page*/
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

    window.location.href = "PlaceOrder.html";
}

