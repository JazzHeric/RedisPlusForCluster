var $;
var form;
var layer;
var step = 0;


window.onload = function () {

    function isReady() {
        if (step < 160 && !window.confRouter) {
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
    layui.use(['form', 'jquery', 'layer'], function () {
        $ = layui.jquery;
        form = layui.form;
        layer = layui.layer;
        //监听提交
        form.on('submit(editBtn)', function () {
            setRedisInfo();
            return false;
        });
        getRedisInfo();
    });
}


/**获取配置信息*/
function getRedisInfo() {
    layer.load(2);
    var json = confRouter.getConfInfo();
    var data = JSON.parse(json);
    layer.closeAll('loading');
    if (data.code === 200) {
        var html = '';
        var tdata = data.data;
        for (var i = 0; i < tdata.length; i++) {
            var conf = tdata[i];
            html += '<tr>';
            html += '<td>' + conf.key + ' </td>';
            html += '<td>' + conf.value + '</td>';
            html += '</tr>';
        }
        $("#tbody").html('');
        $("#tbody").html(html);
        // for (var i = 0; i < tdata.length; i++) {
        //     var conf = tdata[i];
        //     var currObj = $("#" + conf.key + ".outClass")[0];
        //     if (currObj) {
        //         currObj.val(conf.value);
        //     }
        // }
        // form.render();
        // form.render('select');
    } else {
        layer.msg(data.msgs);
    }
}


/**修改配置信息*/
function setRedisInfo() {
    layer.load(2);
    var json = confRouter.setConfInfo($('#confForm').serialize());
    var data = JSON.parse(json);
    layer.closeAll('loading');
    getRedisInfo();
    layer.msg(data.msgs);
}

