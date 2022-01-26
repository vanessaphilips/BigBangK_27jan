"use strict"

//Author RayS

class BuySellorderDTO {
    constructor(assetCode, orderType, limit, assetAmount)
    {
    this.assetCode = assetCode;
    this.orderType = orderType;
    this.limit = limit;
    this.assetAmount = assetAmount;
    }
}

let asset = JSON.parse(localStorage.getItem(CURRENT_ASSET_KEY));
let token = localStorage.getItem(JWT_KEY);

document.getElementById("coinName").innerHTML = "Trade " + asset.name;

getPrice();

setTimeout(() => {
    document.getElementById("currentPrice").innerHTML = asset.currentPrice
}, 100);

document.getElementById('orderType').addEventListener('change', checkOrderType);
document.getElementById('assetAmount').addEventListener('change', assetToCash);
document.getElementById('cashAmount').addEventListener('change', cashToAsset);
document.getElementById('limit').addEventListener('change', assetToCash);

function assetToCash(){
    if(document.getElementById("limit").value > 0){
        document.getElementById('cashAmount').value = document.getElementById('assetAmount').value * document.getElementById("limit").value;
    }else{
    document.getElementById('cashAmount').value = document.getElementById('assetAmount').value * asset.currentPrice;
    }
}

function cashToAsset(){
    if(document.getElementById("limit").value > 0){
        document.getElementById('assetAmount').value = document.getElementById('cashAmount').value / document.getElementById("limit").value;
    }else {
        document.getElementById('assetAmount').value = document.getElementById('cashAmount').value / asset.currentPrice;
    }
}


function checkOrderType() {
    let type = document.getElementById('orderType').value;

    if (type === "BUY" || type === "SELL"){
        console.log("buy or sell");
        document.getElementById('limit').value = 0;
        document.getElementById('limit').disabled = true;
    }else{
        console.log(type);
        document.getElementById('limit').value = 0;
        document.getElementById('limit').disabled = false;
    }
}

function submitTransaction(){
    let assetCode = asset.code;
    let orderType = document.getElementById('orderType').value;
    let limit = document.getElementById('limit').value;
    let assetAmount = document.getElementById('assetAmount').value;

    if(!assetCode || !orderType || !assetAmount){
        showWindow("Please fill in all fields.");
    }else {
        const buysellorderDTO = new BuySellorderDTO(assetCode, orderType, limit, assetAmount);
        sendOrder(buysellorderDTO);
    }
}

function sendOrder(tData) {
    //input validatie
    fetch(`${rootURL}placeorder`, {
        method: "POST",
        headers: acceptHeadersWithToken(token),
        body: JSON.stringify(tData)
    })
        .then(async response => {
            if (response.status === 201) {
                response.text().then((message) => {showWindow(message)});
            }else if(response.status === 400) {
                response.text().then((message) => {showWindow(message)});
            }else if (response.status === 401) {
                response.text().then((message) => {showWindow(message)});
            }
        })
}

function getPrice() {
    fetch(`${rootURL}getcurrentprice`, {
        method: "POST",
        headers: acceptHeadersWithToken(token),
        body: asset.code
    })
        .then(async response => {
            if (response.ok) {
                response.json().then((price) => {asset.currentPrice = price});
            }else {
                console.log("token expired");
            }
        });
}



