const navigation = document.getElementById("navigation")
const currentContentContainer = document.getElementById("currentContentContainer");
const navElements = {}

const stringToHTML = function (str) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(str, 'text/html');
    return doc.body;
}


function fillNavMap() {
    navElements.marketplace = stringToHTML('<object data="MarketPlace.html"  id="currentContentObject"></object>')
    navElements.order = stringToHTML('<object  data="BuySellorder.html"  id="currentContentObject"></object>')
    navElements.register = stringToHTML('<object data="Registration.html"  id="currentContentObject"></object>')
    navElements.login = stringToHTML('<object  data="LoginPage.html"  id="currentContentObject"></object>')

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


fillNavMap()
fillNavigationElement()
setCurrentContent(navElements['login'])