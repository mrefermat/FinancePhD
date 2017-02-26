$(document).ready(function() {    // Create the chart
    Highcharts.stockChart('container', {
        rangeSelector: {
            selected: 1
        },
        series: series,
        title:title,
        credits:{text:'Brought to you by Mark'},
        tooltip: {valueDecimals:2},
        plotOptions:{series:{compare:'percent'}
        }
    })
});

$(document).ready(function() {    // Create the chart
    Highcharts.stockChart('container2', {
        rangeSelector: {
            selected: 1
        },
        series: series1,
        title:title1,
        credits:{text:'Brought to you by Mark'},
        tooltip: {valueDecimals:2}
        // plotOptions:{series:{compare:'percent'}
        
    })
});
