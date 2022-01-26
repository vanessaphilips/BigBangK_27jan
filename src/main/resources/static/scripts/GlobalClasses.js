/**
 * classes used by MarketPlace
 */
"use strict"

class Asset {
    constructor(asset) {
        this.name = asset.name
        this.code = asset.code
        this.currentPrice = asset.currentPrice
    }
}

class PriceDate {
    constructor(date, price) {
        this.dateTime = date
        this.price = price
    }
}

class PriceHistory {
    constructor(priceDates, asset) {
        this.priceDates = priceDates
        this.asset = asset
    }
}

class WalletOwner {
    static BANK = "BANK"
    static CURRENTCLIENT = "CURRENTCLIENT"
    static OTHERCLIENT ="OTHERCLIENT"


}

class OrderType {

    static BUY = "BUY"
    static SELL = "SELL"
    static TRANSACTION = "TRANSACTION"
    static LIMIT_SELL ="LIMIT_SELL"
    static LIMIT_BUY = "LIMIT_BUY"
    static STOPLOSS_SELL = "STOPLOSS_SELL"

}

class TransactionDTO {
    constructor(transaction) {
        this.asset = new Asset(transaction.asset);
        this.priceExcludingFee = transaction.priceExcludingFee;
        this.assetAmount = transaction.assetAmount;
        this.dateTime = new Date(transaction.dateTime);
        this.fee = transaction.fee;
        this.seller = transaction.seller;
        this.buyer = transaction.buyer;
        this.orderType = OrderType.TRANSACTION;
    }
}

class OrderDTO {
    constructor(order) {
        this.asset = order.asset;
        this.orderType = order.orderType;
        this.limit = order.limit;
        this.assetAmount = order.assetAmount;
        this.walletOwner =  order.walletOwner;
        this.orderID = order.orderID;
        this.dateTime = new Date(order.dateTime);
    }
}