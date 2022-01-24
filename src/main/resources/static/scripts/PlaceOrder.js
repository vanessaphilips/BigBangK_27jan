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

let asset = localStorage.getItem(CURRENT_ASSET_KEY);
let token = localStorage.getItem(JWT_KEY);

if (asset == null){
    asset = {
        name: "Bitcoin",
        code: "BTC",
        currentPrice: '3'
    }
}

document.getElementById("coinName").innerHTML = asset.name;
getPrice();
document.getElementById("currentPrice").innerHTML = asset.currentPrice;


document.getElementById('orderType').addEventListener('change', checkOrderType);
document.getElementById('assetAmount').addEventListener('change', assetToCash);
document.getElementById('cashAmount').addEventListener('change', cashToAsset);

function assetToCash(){
    document.getElementById('cashAmount').value = document.getElementById('assetAmount').value * asset.currentPrice;
}

function cashToAsset(){
    document.getElementById('assetAmount').value = document.getElementById('cashAmount').value / asset.currentPrice;
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
        window.alert("Please fill in all fields.")
    }else {
        const buysellorderDTO = new BuySellorderDTO(assetCode, orderType, limit, assetAmount);
        sendOrder(buysellorderDTO);
    }
}

function sendOrder(tData) {
    fetch(`${rootURL}placeorder`, {
        method: "POST",
        headers: acceptHeadersWithToken(token),
        body: JSON.stringify(tData)
    })
        .then(async response => {
            if (response.ok) {
                console.log("transaction successful" + tData.asset);
            }else {
                console.log("transaction failed");
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
                asset.currentPrice = response.body;
                console.log(asset.currentPrice);
            }else {
                console.log("token expired");
            }
        })
}


