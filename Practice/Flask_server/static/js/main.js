$(function() {

    // If you need to specify any global settings such as colors or other settings you can do that here

    // Build Chart A
    $('#chart-A').highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: 'Chart A'
        },
        xAxis: {
            categories: ['Jane', 'John', 'Joe', 'Jack', 'jim']
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Apple Consumption'
            }
        },
        legend: {
            enabled: false
        },
        credits: {
            enabled: false
        },
        tooltip: {
            shared: true
        },
        series: [{
            name: 'Apples',
            data: [5, 3, 8, 2, 4]            
        }]
    });

    // Build Chart B
    $('#chart-B').highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: 'Chart B'
        },
        xAxis: {
            categories: ['Jane', 'John', 'Joe', 'Jack', 'jim']
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Miles during Run'
            }
        },
        legend: {
            enabled: false
        },
        credits: {
            enabled: false
        },
        tooltip: {
            shared: true
        },
        series: [{
            name: 'Miles',
            data: [2.4, 3.8, 6.1, 5.3, 4.1]
        }]
    });
});