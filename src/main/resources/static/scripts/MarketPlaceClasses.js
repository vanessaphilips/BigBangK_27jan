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