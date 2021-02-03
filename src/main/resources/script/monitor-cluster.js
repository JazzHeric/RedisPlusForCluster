var $;
var layer;
var table;
var step = 0;


window.onload = function () {

    function isReady() {
        if (step < 160 && !window.infoClusterRouter) {
            step++;
            setTimeout(isReady, 10);
        } else {
            initPage();
        }
    }

    isReady();
};


/**初始化页面信息*/
function initPage() {
    layui.use(['jquery', 'layer'], function () {
        $ = layui.jquery;
        layer = layui.layer;
        showCharts1();
        showCharts2();
        showCharts3();
        showCharts4();
    });
}


Highcharts.setOptions({
    global: {
        useUTC: false
    }
});

function activeLastPointToolip(chart, index) {
    var points = chart.series[index].points;
    chart.tooltip.refresh(points[points.length - 1]);
}

//内存信息监控
function showCharts1() {
    Highcharts.chart('container1', {
        chart: {
            type: 'spline',
            marginRight: 10,
            events: {
                load: function () {
                    var chart = this;
                    var series01 = this.series[0];
                    var series02 = this.series[1];
                    activeLastPointToolip(chart, 0);
                    setInterval(function () {
                        var json = infoClusterRouter.getMemInfo();
                        var data = JSON.parse(json);
                        var x = (new Date()).getTime();
                        series01.addPoint([x, data.value01], true, true);
                        series02.addPoint([x, data.value02], true, true);
                        activeLastPointToolip(chart, 0);
                    }, 1000);
                }
            }
        },
        credits: {
            enabled: false
        },
        title: {
            text: '内存占用量实时监控'
        },
        xAxis: {
            type: 'datetime',
            tickPixelInterval: 150
        },
        yAxis: {
            title: {
                text: "单位：MB"
            }
        },
        plotOptions: {
            spline: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: true
            }
        },
        tooltip: {
            valueSuffix: ' MB',
            crosshairs: true,
            xDateFormat: '%Y-%m-%d %H:%M:%S'
        },
        legend: {
            enabled: false
        },
        series: [{
            name: '当前占用',
            color: "green",
            data: (function () {
                var pots;
                var data = [];
                var time = (new Date()).getTime();
                for (pots = -10; pots <= 0; pots += 1) {
                    data.push({
                        x: time + pots * 1000,
                        y: 0
                    });
                }
                return data;
            }())
        }, {
            name: '最高占用',
            color: "#7CB5EC",
            data: (function () {
                var pots;
                var data = [];
                var time = (new Date()).getTime();
                for (pots = -10; pots <= 0; pots += 1) {
                    data.push({
                        x: time + pots * 1000,
                        y: 0
                    });
                }
                return data;
            }())
        }]
    });
}

//CPU信息监控
function showCharts2() {
    Highcharts.chart('container2', {
        chart: {
            type: 'spline',
            marginRight: 10,
            events: {
                load: function () {
                    var chart = this;
                    var series01 = this.series[0];
                    var series02 = this.series[1];
                    activeLastPointToolip(chart, 1);
                    setInterval(function () {
                        var json = infoClusterRouter.getCpuInfo();
                        var data = JSON.parse(json);
                        var x = (new Date()).getTime();
                        series01.addPoint([x, data.value01], true, true);
                        series02.addPoint([x, data.value02], true, true);
                        activeLastPointToolip(chart, 1);
                    }, 1000);
                }
            }
        },
        credits: {
            enabled: false
        },
        title: {
            text: '主进程累计CPU耗时监控'
        },
        xAxis: {
            type: 'datetime',
            tickPixelInterval: 150
        },
        yAxis: {
            title: {
                text: "单位：S"
            }
        },
        plotOptions: {
            spline: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: true
            }
        },
        tooltip: {
            valueSuffix: ' S',
            crosshairs: true,
            xDateFormat: '%Y-%m-%d %H:%M:%S'
        },
        legend: {
            enabled: false
        },
        series: [{
            name: '核心态',
            color: "#464646",
            data: (function () {
                var pots;
                var data = [];
                var time = (new Date()).getTime();
                for (pots = -10; pots <= 0; pots += 1) {
                    data.push({
                        x: time + pots * 1000,
                        y: 0
                    });
                }
                return data;
            }())
        }, {
            name: '用户态',
            color: "#7CB5EC",
            data: (function () {
                var pots;
                var data = [];
                var time = (new Date()).getTime();
                for (pots = -10; pots <= 0; pots += 1) {
                    data.push({
                        x: time + pots * 1000,
                        y: 0
                    });
                }
                return data;
            }())
        }]
    });
}

//各数据库KEY数监控
function showCharts3() {
    Highcharts.chart('container3', {
        chart: {
            type: 'column',
            marginRight: 10,
            events: {
                load: function () {
                    var chart = this;
                    setInterval(function () {
                        var json = infoClusterRouter.getKeyInfo();
                        var data = JSON.parse(json);
                        chart.xAxis[0].setCategories(data.xdata);
                        chart.series[0].setData(data.ydata);
                    }, 1000);
                }
            }
        },
        credits: {
            enabled: false
        },
        title: {
            text: '各数据库键数实时监控'
        },
        // xAxis: {
        //     categories: ['DB0', 'DB1', 'DB2', 'DB3', 'DB4', 'DB5',
        //         'DB6', 'DB7', 'DB8', 'DB9', 'DB10', 'DB11',
        //         'DB12', 'DB13', 'DB14', 'DB15']
        // },
        yAxis: {
            labels: {
                x: -15
            },
            title: {
                text: '单位：个'
            }
        },
        tooltip: {
            shared: true,
            crosshairs: true,
            valueSuffix: ' 个'
        },
        plotOptions: {
            column: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: true
            }
        },
        legend: {
            enabled: false
        },
        series: [{
            name: '键'
        }]
    });
}

//网络流速监控
function showCharts4() {
    Highcharts.chart('container4', {
        chart: {
            type: 'areaspline',
            marginRight: 10,
            events: {
                load: function () {
                    var series01 = this.series[0];
                    var series02 = this.series[1];
                    setInterval(function () {
                        var json = infoClusterRouter.getNetInfo();
                        var data = JSON.parse(json);
                        var x = (new Date()).getTime();
                        series01.addPoint([x, data.value01], true, true);
                        series02.addPoint([x, data.value02], true, true);
                    }, 1000);
                }
            }
        },
        credits: {
            enabled: false
        },
        title: {
            text: '网络出入口流速实时监控'
        },
        plotOptions: {
            areaspline: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: true
            }
        },
        legend: {
            enabled: false
        },
        xAxis: {
            type: 'datetime',
            tickPixelInterval: 150
        },
        yAxis: {
            labels: {
                x: -15
            },
            title: {
                text: "单位：Kbps"
            }
        },
        tooltip: {
            shared: true,
            crosshairs: true,
            valueSuffix: ' Kbps',
            xDateFormat: '%Y-%m-%d %H:%M:%S'
        },
        series: [{
            name: '入口流量',
            data: (function () {
                var pots;
                var data = [];
                var time = (new Date()).getTime();
                for (pots = -10; pots <= 0; pots += 1) {
                    data.push({
                        x: time + pots * 1000,
                        y: 0
                    });
                }
                return data;
            }())
        }, {
            name: '出口流量',
            data: (function () {
                var pots;
                var data = [];
                var time = (new Date()).getTime();
                for (pots = -10; pots <= 0; pots += 1) {
                    data.push({
                        x: time + pots * 1000,
                        y: 0
                    });
                }
                return data;
            }())
        }]
    });
}
