var $;
var layer;
var table;
var step = 0;
var rowDataId;


window.onload = function () {

    function isReady() {
        if (step < 160 && !window.connectRouter) {
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
    layui.use(['jquery', 'table', 'layer'], function () {
        $ = layui.jquery;
        layer = layui.layer;
        table = layui.table;
        initConnectData();
    });
}


/**初始化连接数据*/
function initConnectData() {
    layer.load(2);
    var data = connectRouter.selectConnect();
    table.render({
        id: 'dataList',
        elem: '#dataList',
        height: 'full-70',
        toolbar: '#toolbar',
        data: JSON.parse(data),
        cols: [[
            {field: 'text', title: '名称', event: 'setSign'},
            {field: 'rhost', title: '主机', event: 'setSign'},
            {field: 'rport', title: '端口', event: 'setSign'},
            {
                title: '类型', event: 'setSign',
                templet: function (data) {
                    if (data.isha === '0') {
                        return "单机";
                    } else {
                        return "集群";
                    }
                }
            },
            {
                title: 'SSH', event: 'setSign',
                templet: function (data) {
                    if (data.type === '0') {
                        return "关闭";
                    } else {
                        return "启用";
                    }
                }
            },
            {field: 'time', title: '时间', event: 'setSign'},
        ]],
        page: {
            layout: ['prev', 'page', 'next', 'count', 'skip']
        },
        defaultToolbar: ['filter'],
        done: function (res) {
            loadDataSuccess(res);
            initDataMonitor();
        }
    });
}

/**数据加载完成*/
function loadDataSuccess(res) {
    layer.msg('双击行连接服务!');
    var tbody = $('#tableDiv').find('.layui-table-body').find("table").find("tbody");
    //单击行选中数据
    tbody.children("tr").on('click', function () {
        var id = JSON.stringify(tbody.find(".layui-table-hover").data('index'));
        var obj = res.data[id];
        rowDataId = obj.id;
    });
    //双击行连接服务
    tbody.children("tr").on('dblclick', function () {
        var id = JSON.stringify(tbody.find(".layui-table-hover").data('index'));
        var obj = res.data[id];
        openConnect(obj.id);
    });
    //绑定右击菜单
    tbody.children("tr").bind('mousedown', function (e) {
        if (e.which === 3) {
            $(this).addClass('layui-table-click').siblings().removeClass('layui-table-click');
            var id = JSON.stringify(tbody.find(".layui-table-hover").data('index'));
            var obj = res.data[id];
            rowDataId = obj.id;
            showConnectMenu(e);
        }
    });
    $("body").bind("mousedown", function (e) {
        if (e.which === 1) {
            hideConnectMenu(e);
        }
    });
    rowDataId = '';
    layer.closeAll('loading');
}

/**加载完成监听*/
function initDataMonitor() {
    table.on('toolbar(dataList)', function (obj) {
        switch (obj.event) {
            case 'addConnectData':
                addConnectData();
                break;
            case 'expConnectData':
                expConnectData();
                break;
            case 'impConnectData':
                impConnectData();
                break;
            case 'getConnectData':
                getConnectData();
                break;
        }
    });
}

/**显示右击菜单*/
function showConnectMenu(event) {
    var cx = document.body.clientWidth;
    var cy = document.body.clientHeight;
    var layuiMenu = $("#layui-menu");
    var x = event.originalEvent.x;
    var y = event.originalEvent.y;
    var xlack = cx - x;
    var ylack = cy - y;
    if (xlack < 125) {
        x = cx - 125;
    }
    if (ylack < 150) {
        y = cy - 150;
    }
    layuiMenu.css({
        top: y + "px",
        left: x + "px"
    });
    layuiMenu.show();
}

/**隐藏右击菜单*/
function hideConnectMenu(event) {
    if (!event) {
        $("#layui-menu").hide();
        return;
    }
    if (!(event.target.id === "layui-menu" || $(event.target).parents("#layui-menu").length > 0)) {
        $("#layui-menu").hide();
    }
}

/**打开连接数据*/
function openConnect(id) {
    var result = 0;
    layer.load(2);
    var json = connectRouter.createConnect(true, id);
    var data = JSON.parse(json);
    if (data.code === 200) {
        result = 1;
    } else {
        layer.alert(data.msgs, {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
    }
    layer.closeAll('loading');
    return result;
}

/**断开连接数据*/
function shutConnect(id) {
    var result = 0;
    layer.load(2);
    var json = connectRouter.disconConnect(id);
    var data = JSON.parse(json);
    if (data.code === 200) {
        result = 1;
    }
    layer.closeAll('loading');
    return result;
}

/**打开终端*/
function cmdConnectData() {
    //隐藏右击菜单
    hideConnectMenu();
    layer.load(2);
    var json = connectRouter.createConnect(false, rowDataId);
    var data = JSON.parse(json);
    layer.closeAll('loading');
    if (data.code !== 200) {
        layer.alert(data.msgs, {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
        return false;
    }
    layer.open({
        title: '命令模式',
        type: 2,
        area: ['998px', '536px'],
        fixed: true,
        maxmin: false,
        resize: false,
        skin: 'layui-layer-lan',
        content: '../page/connect-terminal.html'
    });
}

/**添加连接数据*/
function addConnectData() {
    layer.open({
        type: 2,
        fixed: true,
        maxmin: false,
        resize: false,
        title: '新增连接',
        skin: 'layui-layer-lan',
        area: ['455px', '485px'],
        content: '../page/connect-save.html'
    });
}

/**刷新连接列表*/
function getConnectData() {
    //隐藏右击菜单
    hideConnectMenu();
    layer.load(2);
    var data = connectRouter.selectConnect();
    table.reload('dataList', {
        height: 'full-70',
        data: JSON.parse(data),
        page: {curr: 1},
        done: function (res) {
            loadDataSuccess(res);
            initDataMonitor();
        }
    });
}

/**打开连接数据*/
function fitConnectData() {
    //隐藏右击菜单
    hideConnectMenu();
    openConnect(rowDataId);
}

/**测试连接数据*/
function valConnectData() {
    //隐藏右击菜单
    hideConnectMenu();
    var index = layer.load(2, {time: 30 * 1000, shade: 0.5});
    var resultJson = connectRouter.detectConnect(0, rowDataId);
    var result = JSON.parse(resultJson);
    if (result.code) {
        layer.close(index);
    }
    layer.msg(result.msgs);
}

/**编辑连接数据*/
function updConnectData() {
    //隐藏右击菜单
    hideConnectMenu();
    if (rowDataId === "" || rowDataId == null) {
        layer.msg('请选择要操作的数据行！');
        return false;
    }
    layer.open({
        type: 2,
        fixed: true,
        maxmin: false,
        resize: false,
        title: '编辑连接',
        skin: 'layui-layer-lan',
        area: ['455px', '485px'],
        content: '../page/connect-edit.html'
    });
}

/**删除连接数据*/
function delConnectData() {
    //隐藏右击菜单
    hideConnectMenu();
    if (rowDataId === "" || rowDataId == null) {
        layer.msg('请选择要操作的数据行！');
        return false;
    }
    var index = layer.confirm('确认删除连接？', {
        btn: ['确定', '取消'],
        skin: 'layui-layer-lan',
        closeBtn: 0
    }, function () {
        var json = connectRouter.deleteConnect(rowDataId)
        var data = JSON.parse(json);
        layer.close(index);
        if (data.code === 200) {
            getConnectData();
        } else {
            layer.alert(data.msgs, {
                skin: 'layui-layer-lan',
                closeBtn: 0
            });
        }
    });
}

/**操作连接数据*/
function setConnectData() {
    //隐藏右击菜单
    hideConnectMenu();
    if (rowDataId === "" || rowDataId == null) {
        layer.msg('请选择要操作的数据行！');
        return false;
    }
    layer.open({
        title: '操作连接',
        type: 2,
        area: ['455px', '480px'],
        fixed: true,
        maxmin: false,
        skin: 'layui-layer-lan',
        content: '../page/connect-info.html'
    });
}

/**导出连接*/
function expConnectData() {
    layer.msg("备份连接任务正在后台执行...");
    var json = connectRouter.backupConnect();
    var data = JSON.parse(json);
    if (data.code === 200) {
        layer.msg(data.msgs);
    } else {
        layer.alert(data.msgs, {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
    }
}

/**导入连接*/
function impConnectData() {
    layer.msg("还原连接任务正在后台执行...");
    var json = connectRouter.recoveConnect();
    var data = JSON.parse(json);
    layer.msg(data.msgs);
    getConnectData();
}
