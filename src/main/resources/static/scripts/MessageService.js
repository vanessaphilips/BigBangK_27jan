


/**creates a hidden popup in html*/
let popupDiv = document.createElement('div');
popupDiv.className = "popup";
popupDiv.id = "popup"
popupDiv.style.display = "none";
let messageP = document.createElement('p');
messageP.id = "popupMessage";
let popupButton = document.createElement('button');
popupButton.id = "popupButton";
popupButton.addEventListener("click", function(){
    closeWindow();});
popupButton.innerHTML = "OK";
popupDiv.appendChild(messageP);
popupDiv.appendChild(popupButton);
document.body.appendChild(popupDiv);

/**shows popup with message given as parameter*/
function showWindow(message){
    document.getElementById('popupMessage').innerHTML = message;
    document.getElementById('popupButton').style.display = 'block';
    document.getElementById('popup').style.display = 'block';
}

function closeWindow(){
    document.getElementById('popup').style.display = 'none';
}

/**shows popup without a close button that times out automatically based on millisecond parameter*/
function showTimedWindow(message, milliseconds){
    document.getElementById('popupMessage').innerHTML = message;
    document.getElementById('popupButton').style.display = 'none';
    document.getElementById('popup').style.display = 'block';
    setTimeout(() => {
        closeWindow();
    }, milliseconds);
}