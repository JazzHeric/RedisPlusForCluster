var $;
var layer;
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
    layui.use(['jquery', 'layer', 'element'], function () {
        $ = layui.jquery;
        layer = layui.layer;
        initNodeInfo();
        initBaseInfo();
    });
}


/**初始化基础信息*/
function initBaseInfo() {
    var json = infoClusterRouter.getBaseInfo();
    var data = JSON.parse(json);
    if (data.code === 200) {
        var redisData = data.data;
        $("#server").html(redisData.server);
        $("#client").html(redisData.client);
        $("#memory").html(redisData.memory);
        //$("#persistence").html(redisData.persistence);
        $("#stats").html(redisData.stats);
        $("#cpu").html(redisData.cpu);
    } else {
        layer.msg(data.msgs);
    }
}


/**初始化节点信息*/
function initNodeInfo() {
    var json = infoClusterRouter.getNodeInfo();
    var data = JSON.parse(json);
    if (data.code === 200) {
        var html = '';
        var info = data.data;
        for (var i = 0; i < info.length; i++) {
            html += '<tr>';
            html += '<td>' + info[i].host + '</td>';
            html += '<td>' + info[i].role + '</td>';
            html += '<td>' + info[i].flag + '</td>';
            // html += '<td>' + info[i].node + '</td>';
            if (info[i].slot) {
                html += '<td>' + info[i].slot + '</td>';
            } else {
                html += '<td>--</td>';
            }
            html += '</tr>';
        }
        $("#tbody").html(html);
    } else {
        layer.msg(data.msgs);
    }
}



