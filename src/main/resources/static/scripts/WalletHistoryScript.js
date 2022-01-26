/**
 *
 * @Author Kelly Speelman - de Jonge
 */

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawCharts);
let token = localStorage.getItem(JWT_KEY);

function drawCharts(){
    /*let token = "hoi@hotmail.nl"; // test token voor het testen van de back met de frond end.
    getWalletHistorie(token);*/
    let valutaData = '{"piechart": ['+
        '{"Valuta":"Euro","Price":20},'+
        '{"Valuta":"Cardano","Price":200},'+
        '{"Valuta":"Cardano","Price":70},'+
        '{"Valuta":"Litecoin","Price":30}' +
        '],"linechart": ['+
    '{"dateTime":"2022-01-01T14:53:41","Euro":20, "All crypto":200},'+
    '{"dateTime":"2022-01-01T14:53:41","Euro":20, "All crypto":210},'+
    '{"dateTime":"2022-01-01T14:53:41","Euro":100, "All crypto":100}' +
        '],"barchart": ['+
    '{"Valuta":"Euro","old":20,"nieuw":20},'+
    '{"Valuta":"Cardano","old":200,"nieuw":210},'+
    '{"Valuta":"Litecoin","old":20,"nieuw":30}]}';
    let obj = JSON.parse(valutaData);
    //document.getElementById("test").innerHTML = obj["piechart"];

    const dataPie = google.visualization.arrayToDataTable(getInfromationPieChart(obj["piechart"]));
    const dataLine = google.visualization.arrayToDataTable(getInfromationLineChart(obj["linechart"]));
    const dataBar = google.visualization.arrayToDataTable(getInfromationBarChart(obj["barchart"]));

    const options = {
        //title: 'My Wallet',
        is3D: true,
        pieSliceTextStyle: {
            color: 'black',
        },
        //colors: ['#b0120a', '#ffab91'],
        sliceVisibilityThreshold: 0.08,
        backgroundColor: 'none',
    };

    const piechart = new google.visualization.PieChart(document.getElementById('piechart'));
    const linechart = new google.visualization.LineChart(document.getElementById('linechart'));
    const barchart = new google.visualization.BarChart(document.getElementById('barchart'));

    piechart.draw(dataPie, options);
    linechart.draw(dataLine, options);
    barchart.draw(dataBar, options);
}

const getWalletHistorie = async (token) => {
    return await fetch(`${rootURL}walletHistory`,
        {
            method: 'GET',
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

function getInfromationPieChart(obj) {
    let dataArray = [['Valuta', 'Price']];
    for (const x in obj) {
        dataArray.push([obj[x]["Valuta"], obj[x]["Price"]]);
    }
    return dataArray;
}

function getInfromationLineChart(obj) {
    let dataArray = [['Date', 'Euro', 'All Crypto', 'Total']];
    for (const x in obj) {
        let totaal = parseInt(obj[x]["Euro"]) + parseInt(obj[x]["All crypto"]);
        dataArray.push([obj[x]["dateTime"], obj[x]["Euro"], obj[x]["All crypto"], totaal]);
    }
    return dataArray;
}

function getInfromationBarChart(myobj) {
    let dataArray = [['Valuta', 'Purchase price', 'Current price']];
    for (const x in myobj) {
        dataArray.push([myobj[x]["Valuta"], myobj[x]["old"], myobj[x]["nieuw"]])
    }
    return dataArray;
}
