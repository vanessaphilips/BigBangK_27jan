/**
 * converting the PriceHistories from /marketplace into html
 * @author Pieter Jan Bleichrodt
 */
"use strict"
//Declare constants

const MARKETPLACE_ROOT_CONTAINER = document.getElementById("assetContainers")
const TRADEBUTTON_CLASS = "tradeButton"

const ASSETCODELABEL_CLASS = "code"
const ASSETNAMELABEL_CLASS = "name"
const ASSETCURRENTPRICELABEL_CLASS = "price"

const PRICEHISTORYGRAPH_CLASS = "priceHistoryGraph"
const PRICEHISTORYGRAPH_ID_PREFIX = "priceHistory"

const GRAPHCONTAINER_CLASS = "graphContainer"
const GRAPHCONTAINER_ID_PREFIX = "graphContainer"

const ASSETCONTAINER_CLASS = "assetContainer"
const ASSETCONTAINERS_CLASS = "assetContainers"
const SELECTED_ASSETCONTAINER_CLASS = "selectedAsset"
const DAYSBACKINPUTFIELD_ID = "daysBack"
const ROUNDING_DIGITS = 2

const GRAPHWIDTH = 200;
const GRAPHHEIGHT = 100;
let updateInterval = 300000;
let lastUpdate = Date.now();
let token = localStorage.getItem(JWT_KEY)
let daysBackInputField = document.getElementById(DAYSBACKINPUTFIELD_ID);
let daysBack = 7

let priceHistories = []

//Declare functions
async function initializePage() {
    daysBackInputField.value = daysBack;
    console.log("pageInitialized on " + new Date(Date.now()).toLocaleTimeString())
    await refreshPriceHistories()
     fillPageWithCreateAssets()
}

async function refreshPriceHistories(){
    const json = await getPriceHistoriesByAsset(token)
    updateInterval = Number.parseFloat(json.updateInterval);
    const jsonPriceHistories = JSON.parse(json.priceHistory);
    if (jsonPriceHistories !== undefined) {
        saveAsPriceHistories(jsonPriceHistories)
        lastUpdate = priceHistories[0].priceDates.sort(c => c.dateTime).reverse()[0].dateTime
    }
}
async function refreshPage() {
  await refreshPriceHistories()
    for(const priceHistory of priceHistories){
        const assetContainer = document.getElementById(priceHistory.asset.code)
        assetContainer.getElementsByClassName(ASSETCURRENTPRICELABEL_CLASS)[0].innerHTML = normalizePrice(priceHistory.asset.currentPrice)
        assetContainer.getElementsByClassName(GRAPHCONTAINER_CLASS)
        upDatePriceHistoryGraph(priceHistory, GRAPHWIDTH,GRAPHHEIGHT)
    }
}
function setTimedPageRefresh(){
    const dateNow = new Date()
    const initialTimeOut = updateInterval - (dateNow.getTime() - lastUpdate.getTime()) + 10000
    console.log(initialTimeOut)
    const onPriceHistoryUpdate = () => {
        refreshPage()
        setInterval(refreshPage, updateInterval)
    }
    setTimeout(onPriceHistoryUpdate, initialTimeOut)
}

function upDatePriceHistoryGraph(priceHistory, width, height) {
    const asset = priceHistory.asset
    const graphContainer = document.getElementById(asset.code).getElementsByClassName(GRAPHCONTAINER_CLASS)[0]
    graphContainer.replaceChild(createPriceHistoryGraph(priceHistory, width, height), graphContainer.firstChild)
}

function updateAllPriceHistoryGraphs() {
    for (const priceHistory of priceHistories) {
        upDatePriceHistoryGraph(priceHistory, GRAPHWIDTH, GRAPHHEIGHT)
    }
}

daysBackInputField.addEventListener("input", async () => {
    daysBack = daysBackInputField.value
    updateAllPriceHistoryGraphs();
})



function createPriceHistoryGraph(priceHistory, width, height) {
    const dateInPast = new Date(new Date().valueOf() - daysBack * 86400000)
    const priceDates = priceHistory.priceDates.filter(pd => pd.dateTime > dateInPast)
    let priceHistoryGraph = createGraph(priceDates, width, height)
    priceHistoryGraph.id = PRICEHISTORYGRAPH_ID_PREFIX + priceHistory.asset.code
    priceHistoryGraph.className = PRICEHISTORYGRAPH_CLASS
    return priceHistoryGraph;
}


function createAssetCodeLabel(priceHistory) {
    const codeContainer = document.createElement("div")
    const assetCodeLabel = document.createElement("label")
    assetCodeLabel.className = ASSETCODELABEL_CLASS
    assetCodeLabel.innerHTML = priceHistory.asset.code
    codeContainer.className = "codeContainer"
    codeContainer.appendChild(assetCodeLabel)
    codeContainer.appendChild(createAssetTradeButton(priceHistory))
    return codeContainer
}

function createAssetNameLabel(priceHistory) {
    const assetNameLabel = document.createElement("label")
    assetNameLabel.className = ASSETNAMELABEL_CLASS
    assetNameLabel.innerHTML = priceHistory.asset.name
    return assetNameLabel
}

function createAssetCurrentPriceLabel(priceHistory) {
    const assetCurrentPriceLabel = document.createElement("label")
    assetCurrentPriceLabel.className = ASSETCURRENTPRICELABEL_CLASS
    assetCurrentPriceLabel.innerHTML = normalizePrice(priceHistory.asset.currentPrice)
    return assetCurrentPriceLabel
}

function createAssetGraphContainer(priceHistory) {
    const assetGraphContainer = document.createElement("div")
    assetGraphContainer.className = GRAPHCONTAINER_CLASS
    assetGraphContainer.appendChild(createPriceHistoryGraph(priceHistory, GRAPHWIDTH, GRAPHHEIGHT))
    return assetGraphContainer
}

function createAssetTradeButton(priceHistory) {
    const assetTradeButton = document.createElement("button")
    assetTradeButton.className = TRADEBUTTON_CLASS
    assetTradeButton.innerHTML = "trade"
    assetTradeButton.addEventListener("click", function () {
        localStorage.setItem(CURRENT_ASSET_KEY, JSON.stringify(priceHistory.asset))
        window.location.href = tradeButtonLink;
    })
    return assetTradeButton
}

function createAssetContainer(priceHistory) {
    const assetContainer = document.createElement("div")
    if (priceHistory !== undefined) {
        priceHistory.priceDates.sort(c => c.date)
        assetContainer.id = priceHistory.asset.code;
        assetContainer.className = ASSETCONTAINER_CLASS
        assetContainer.appendChild(createAssetCodeLabel(priceHistory))
        assetContainer.appendChild(createAssetNameLabel(priceHistory))
        assetContainer.appendChild(createAssetCurrentPriceLabel(priceHistory))
        assetContainer.appendChild(createAssetGraphContainer(priceHistory))
       // assetContainer.appendChild(createAssetTradeButton(priceHistory))
        document.getElementsByClassName(ASSETCONTAINERS_CLASS)[0].appendChild(assetContainer)
    }
}

const fillPageWithCreateAssets = () => {
    for (const priceHistory of priceHistories) {
        createAssetContainer(priceHistory)
    }
}

function parseDate(localDateTimeString) {
    const dateTime = localDateTimeString.substring(0, 10) + " " + localDateTimeString.substring(11)
    return new Date(dateTime)
}

function saveAsPriceHistories(jsonPriceHistories) {
    priceHistories = jsonPriceHistories.map(ph => {
            const priceDates = ph.priceDates.map(pd => new PriceDate(parseDate(pd.dateTime), pd.price))
            return new PriceHistory(priceDates, new Asset(ph.asset))
        }
    )
}

function createDateInPast() {
    const date = new Date(new Date().valueOf() - daysBack * 86400000)
    return date.toISOString().substring(0, 23);
}

const getPriceHistoriesByAsset = async (token) => {
    return await fetch(`${rootURL}marketplace`,
        {
            method: 'POST',
            headers: acceptHeadersWithToken(token),
            body: createDateInPast()
        }).then(promise => {
        if (promise.ok) {
            return promise.json()
        } else if(promise.status===400){
            console.log("Couldn't retrieve pricehistory from the server")
            window.location.href = loginPageURL
        }else if(promise.status===401){
                       window.location.href = loginPageURL
        }
    }).then(json => json)
        .catch(error=>console.log("Somethin went wrong: " + error))
}

await initializePage()
setTimedPageRefresh()