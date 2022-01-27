let ORDERVIEW_CONTAINER_ID = "orderViewContainer";

let ORDER_COLUMN_TITLE_CLASS = "columnTitle";
let ORDER_COLUMN_CLASS = "orderColumn";

let ORDER_CONTAINER_CLASS = "orderContainer";
let ASSETAMOUNTLABELCLASS = "assetAmount";
let ORDERTYPELABELCLASS = "orderType";
let DATETIMELABELCLASS = "dateTime";
let LIMITLABELCLASS = "limit";
const ORDERCURRENTPRICELABELCLASS = "currentPrice";
const PRICE_EX_FEE_LABEL_CLASS = "PriceExlcudingfee";
const FEE_LABEL_CLASS = "Fee";
const KEY_CLASS = "key"
const VALUE_CLASS = "value"
const ASSETCODELABEL_CLASS = "code"

const DELETE_BUTTON_TEXT  ="Delete";
let ORDER_DELETE_BUTTON_CLASS = "orderDeleteButton";
const ORDER_TO_DELETE_LABEL_ID= "orderToDelete";

const token = localStorage.getItem(JWT_KEY);
const ORDERVIEW_CONTAINER = document.getElementById(ORDERVIEW_CONTAINER_ID)
let currentContent = "ALL"
let orderDTOs;
let transactionDTOs;

function createKeyValueContainer(labelClass, innerHtml) {
    const container = document.createElement("div");
    const labelname = document.createElement("label");
    const labelvalue = document.createElement("label");
    labelname.className = KEY_CLASS;
    labelname.innerHTML = labelClass
    labelvalue.className = VALUE_CLASS
    labelvalue.innerHTML = innerHtml;
    container.appendChild(labelname)
    container.appendChild(labelvalue)
    return container;
}

function deleteOrder(orderID) {
   deleteOrderFetch(token, orderID);
   initializePage();
}

function customizeMessageService() {
    let popupCancelButton = document.createElement('button');
    popupCancelButton.id = "popUpCancelButton"
    let orderID = document.createElement("label");
    orderID.style.display = "none";
    orderID.id = ORDER_TO_DELETE_LABEL_ID;
    popupCancelButton.addEventListener("click", function () {
        closeWindow();
    });
    popupCancelButton.innerHTML = "Cancel";
    popupDiv.appendChild(popupCancelButton);
    popupDiv.appendChild(orderID);
    popupButton.addEventListener("click", function () {
        deleteOrder(orderID.innerText);
    });
}

function deleteOrderPopUp(orderID) {
    document.getElementById(ORDER_TO_DELETE_LABEL_ID).innerText = orderID;
    console.log("delete order: "+ orderID)
    showWindow("Are you sure you want to delete this order?\n" +
        "This action cannot be undone!")

}

function createDeleteButton(order) {
    const deleteButton = document.createElement("button")
    deleteButton.className = ORDER_DELETE_BUTTON_CLASS;

    deleteButton.innerText = DELETE_BUTTON_TEXT;
    deleteButton.addEventListener("click", () => {
        console.log("deleteButtonClicked")
        deleteOrderPopUp(order.orderID);
    })
    return deleteButton;
}

function createOrderContainer(order) {
    const orderContainer = document.createElement("div");
    orderContainer.id = order.orderID;
    orderContainer.className = ORDER_CONTAINER_CLASS;
    orderContainer.appendChild(createKeyValueContainer(ASSETCODELABEL_CLASS, order.asset.code));
    orderContainer.appendChild(createKeyValueContainer(ASSETAMOUNTLABELCLASS, normalizePrice(order.assetAmount)));
    if (order instanceof TransactionDTO) {
        if (order.seller === WalletOwner.CURRENTCLIENT || order.buyer === WalletOwner.CURRENTCLIENT) {
            orderContainer.classList.add(WalletOwner.CURRENTCLIENT);
        }
        orderContainer.appendChild(createKeyValueContainer(FEE_LABEL_CLASS, normalizePrice(order.fee)))

        orderContainer.appendChild(createKeyValueContainer(PRICE_EX_FEE_LABEL_CLASS, normalizePrice(order.priceExcludingFee)))
    } else {
        if (order.walletOwner === WalletOwner.CURRENTCLIENT) {
            orderContainer.classList.add(WalletOwner.CURRENTCLIENT);
        }
        orderContainer.appendChild(createKeyValueContainer(LIMITLABELCLASS, normalizePrice(order.limit)));
        orderContainer.appendChild(createKeyValueContainer(ORDERCURRENTPRICELABELCLASS, normalizePrice(order.asset.currentPrice)));
    }
    orderContainer.appendChild(createKeyValueContainer(ORDERTYPELABELCLASS, order.orderType));
    orderContainer.appendChild(createKeyValueContainer(DATETIMELABELCLASS, order.dateTime.toLocaleDateString()));
    if (order.walletOwner === WalletOwner.CURRENTCLIENT) {
        orderContainer.appendChild(createDeleteButton(order));
    }
    return orderContainer
}

const filterOrders = document.getElementById("filterOrders");

function emptyOrderViewContainer() {
    ORDERVIEW_CONTAINER.innerHTML = "";
}

filterOrders.addEventListener("change", () => {
    currentContent = filterOrders.options[filterOrders.selectedIndex].value;
    fillPage();
})

function createOrderColumn(filteredOrderDTOs) {
    const orderColumn = document.createElement("div")
    if (filteredOrderDTOs.length > 0) {
        orderColumn.className = ORDER_COLUMN_CLASS;
        const columnLabel = document.createElement("label")
        columnLabel.innerHTML = filteredOrderDTOs[0].orderType;
        columnLabel.className = ORDER_COLUMN_TITLE_CLASS;
        orderColumn.appendChild(columnLabel)
        for (const order of filteredOrderDTOs) {
            const orderContainer = createOrderContainer(order)
            orderColumn.appendChild(orderContainer);
        }
    }
    return orderColumn;
}

function fillPage() {
    emptyOrderViewContainer();
    for (const orderType in OrderType) {
        let filteredOrderDTOs;
        let showTranActions = false;
        console.log(currentContent)
        if (currentContent === "ALL") {
            filteredOrderDTOs = orderDTOs.filter(o => o.orderType === orderType)
        } else {
            filteredOrderDTOs = orderDTOs.filter(o => {
                return o.orderType === orderType.toString() && o.walletOwner === WalletOwner.CURRENTCLIENT
            })
            showTranActions = true;
        }
        if (filteredOrderDTOs.length !== 0) {
            ORDERVIEW_CONTAINER.appendChild(createOrderColumn(filteredOrderDTOs));
        }
        if (showTranActions && orderType === OrderType.TRANSACTION) {
            ORDERVIEW_CONTAINER.appendChild(createOrderColumn(transactionDTOs));
        }
    }
}

async function initializePage() {
    const jsonOrders = await ordersFetch(token, "orderoverview")
    const jsonTransActions = await ordersFetch(token, "clienttransactions")
    orderDTOs = convertFetchToOrderDTOs(JSON.parse(jsonOrders.orders))
    transactionDTOs = convertFetchToTrasactionDTOs(JSON.parse(jsonTransActions.transactions))
    fillPage();
}

const ordersFetch = async (token, url) => {
    return await fetch(rootURL + url,
        {
            method: 'GET',
            headers: acceptHeadersWithToken(token)
        }).then(promise => {
        if (promise.ok) {
            return promise.json()
        } else if (promise.status === 400) {
            console.log("Couldn't retrieve pricehistory from the server")
            window.location.href = loginPageURL
        } else if (promise.status === 401) {
            window.location.href = loginPageURL
        }
    }).then(jsObject => jsObject)
        .catch(error => console.log("Somethin went wrong: " + error))
}
const deleteOrderFetch = async (token, orderID) => {
    return await fetch(`${rootURL}deleteorder/${orderID}`,
        {
            method: 'DELETE',
            headers: acceptHeadersWithToken(token)
        }).then(promise => {
        if (promise.ok) {
            console.log("Order deleted")
        } else if (promise.status === 204) {
            console.log("Order could not be found, and is therefore not deleted")
        } else if (promise.status === 401) {
            console.log("Unauthorized")
        }
    }).then(jsObject => jsObject)
        .catch(error => console.log("Somethin went wrong: " + error))
}


function convertFetchToOrderDTOs(orders) {
    const orderDTOs = []
    for (const order of orders) {
        orderDTOs.push(new OrderDTO(order))
    }
    return orderDTOs
}

function convertFetchToTrasactionDTOs(transactions) {
    const transactionDTOs = []
    for (const transaction of transactions) {
        console.log(transaction)
        transactionDTOs.push(new TransactionDTO(transaction))
    }
    console.log(transactionDTOs)
    return transactionDTOs
}
customizeMessageService()
initializePage()
