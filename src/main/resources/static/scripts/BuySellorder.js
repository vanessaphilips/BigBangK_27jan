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

function submitTransaction(){
    let assetCode = document.getElementById('assetCode').value;
    let orderType = document.getElementById('orderType').value;
    let limit = document.getElementById('limit').value;
    let assetAmount = document.getElementById('assetAmount').value;

    if(!assetCode || !orderType || !assetAmount){
        window.alert("Please fill in all fields.")
    }else {
        const buysellorderDTO = new BuySellorderDTO(assetCode, orderType, limit, assetAmount);
        sendTransactionData(buysellorderDTO);
    }
}

function sendTransactionData(tData) {
    // niet helemaal duidelijk wat ik hier voor url moet hanteren
    fetch(`${rootURL}placeorder`, {
        method: "POST",
        headers: acceptHeaders(),
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


