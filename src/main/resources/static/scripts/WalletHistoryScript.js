google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(getInfromation);
let token = localStorage.getItem(JWT_KEY);

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

function getInfromation() {
    let dataArray = [['Valuta', 'Price']];

    let valutaData = '['+
        '{"Euro":20},'+
        '{"Cardano":200},'+
        '{"Litecoin":30}]';

    let obj = JSON.parse(valutaData);
    //document.getElementById('tekst').innerHTML = obj.Wallet;
    Object.entries(obj).forEach((entry) => {
        const [key, value] = entry;
        Object.entries(value).forEach((entry) =>{
            const [key1, value1] = entry;
            dataArray.push([key1, value1]);
        });
    });

    drawCharts(dataArray);
}

function drawCharts(dataArray){

    const data = google.visualization.arrayToDataTable(dataArray);

    const options = {
        title: 'My Wallet',
        is3D: true,
        pieSliceTextStyle: {
            color: 'black',
        },
        //sliceVisibilityThreshold: 0.08,
        backgroundColor: 'none',
        //colors: ['Yellow', 'orange', 'red', 'purple', 'green']
    };

    const piechart = new google.visualization.PieChart(document.getElementById('piechart'));
    const linechart = new google.visualization.LineChart(document.getElementById('linechart'));
    const barchart = new google.visualization.BarChart(document.getElementById('barchart'));

    piechart.draw(data, options);
    linechart.draw(data, options);
    barchart.draw(data, options);
}

getWalletHistorie(token);