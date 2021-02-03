var $;
var flag;
var form;
var layer;
var connect;

layui.use(['jquery', 'form', 'layer'], function () {
    $ = layui.jquery;
    layer = layui.layer;
    form = layui.form;
    var json = parent.connectRouter.querysConnect(parent.rowDataId);
    connect = JSON.parse(json);
    flag = parent.connectRouter.isopenConnect(parent.rowDataId);
    $("#name").text(connect.text);
    $("#addr").text(connect.rhost + ":" + connect.rport);
    var shutBtn = $("#shutBtn");
    if (connect.isha === "0") {
        $("#isha").text("单机模式");
        changeViewStyle(260);
    }
    if (connect.isha === "1") {
        $("#isha").text("集群模式");
    }
    if (flag === 1) {
        shutBtn.val("断开");
        if (connect.isha === "1") {
            $(".node-hide").css("display", "block");
            getNodeInfo();
        }
    } else {
        shutBtn.val("连接");
        if (connect.isha === "1") {
            $(".node-hide").css("display", "none");
            changeViewStyle(260);
        }
    }

    shutBtn.on("click", function () {
        if (flag === 1) {
            var closeResult = parent.shutConnect(connect.id);
            if (closeResult === 1) {
                flag = 0;
                $("#shutBtn").val("连接");
                if (connect.isha === "1") {
                    $(".node-hide").css("display", "none");
                    changeViewStyle(280);
                }
            }
        } else {
            var openResult = parent.openConnect(connect.id);
            if (openResult === 1) {
                flag = 1;
                $("#shutBtn").val("断开");
                if (connect.isha === "1") {
                    $(".node-hide").css("display", "block");
                    getNodeInfo();
                    changeViewStyle(490);
                }
            }
        }
    });

    $("#backBtn").on("click", function () {
        //关闭弹出层
        var index = parent.layer.getFrameIndex(window.name);
        parent.layer.close(index);
    });
});


function getNodeInfo() {
    var json = parent.dataClusterRouter.nodeInfo();
    var data = JSON.parse(json);
    if (data.code === 200) {
        var html = '';
        for (var i = 0; i < data.data.length; i++) {
            html += '<tr>';
            html += '<td>' + data.data[i].addr + '</td>';
            var role = data.data[i].flag;
            role = role.replace("slave", "从节点");
            role = role.replace("master", "主节点");
            role = role.replace("myself", "当前节点");
            html += '<td>' + role + '</td>';
            html += '</tr>';
        }
        $("#node-body").html(html);
    } else {
        layer.alert(result.msgs, {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
    }
}

function changeViewStyle(height) {
    var index = parent.layer.getFrameIndex(window.name);
    parent.layer.style(index, {
        height: height + 'px'
    });
}


