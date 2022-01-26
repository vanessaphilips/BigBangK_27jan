/**
 * Root page for the bigBankgk website
 * this is a global menu for navigating the pages defined in fillNavMap
 *
 */
const navigation = document.getElementById("navigation")
const currentContentContainer = document.getElementById("currentContentContainer");
const navElements = {}

const stringToHTML = function (str) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(str, 'text/html');
    return doc.body;
}

/**
 *  Voeg hier je pagina toe en alles komt goed.
 *  Vorm is
 *  navElements.{jouw link zoals die in het navigatie menu verschijnt} =
 *  stringToHTML('<object data="filenaam pagina incl html extensie" id="currentContentObject"></object>')
 */
function declareSubPages() {
    navElements.marketplace = stringToHTML('<object data="MarketPlace.html"  id="currentContentObject"></object>')
    navElements.wallet = stringToHTML('<object data="wallet.html"  id="currentContentObject"></object>')
    navElements.history = stringToHTML('<object data="TransactionHistory.html"  id="currentContentObject"></object>')
    navElements.order = stringToHTML('<object  data="PlaceOrder.html"  id="currentContentObject"></object>')
    navElements.orderoverview = stringToHTML('<object  data="orderoverview.html"  id="currentContentObject"></object>')
    navElements.wallet = stringToHTML('<object data="wallet.html"  id="currentContentObject"></object>')
}

function setCurrentContent(selectedContent) {
    let height = selectedContent.clientHeight
    if (currentContentContainer.firstChild !== undefined) {
        currentContentContainer.replaceChild(selectedContent, currentContentContainer.firstChild)
    } else {
        currentContentContainer.appendChild(selectedContent)
    }
    console.log(height)
    currentContentContainer.clientHeight = height
}

window.onload = () => {
    let height = document.getElementById("currentContentContainer").firstChild.clientHeight
    console.log(height)
    currentContentContainer.clientHeight = height
}


function fillNavigationElement() {
    for (const navKey in navElements) {
        const navLink = document.createElement("label")
        navLink.innerText = navKey;
        navLink.addEventListener("click", () => {
            setCurrentContent(navElements[navKey])
        })
        navigation.appendChild(navLink)
    }
}


declareSubPages()
fillNavigationElement()
setCurrentContent(navElements['wallet'])