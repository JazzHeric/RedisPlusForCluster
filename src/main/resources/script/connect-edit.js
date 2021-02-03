var $;
var form;
var layer;
var connect;

layui.use(['jquery', 'form', 'layer'], function () {
    $ = layui.jquery;
    form = layui.form;
    layer = layui.layer;
    //渲染表单
    form.render();
    loadPageData();
    showDataView();
    form.on('checkbox(type)', function (data) {
        if (!data.elem.checked) {
            showConView();
        } else {
            showSshView();
        }
    });
    form.on('checkbox(isha)', function (data) {
        if (!data.elem.checked) {
            $('.div-input05 .layui-form-checkbox[lay-skin="primary"] i').css('background', '#E0E0E2');
        } else {
            $('.div-input05 .layui-form-checkbox[lay-skin="primary"] i').css('background', '#5FB878');
        }
    });
    //注册私钥选择监听事件
    $("#spkey").on("click", function () {
        var path = parent.otherRouter.pickPkey();
        $("#spkey").val(path);
    });
    //注册测试按钮事件
    $("#testBtn").on("click", function () {
        showProView();
    });
    //监听提交
    form.on('submit(saveBtn)', function () {
        saveConnect();
        return false;
    });
    //注册监听事件
    $("#backBtn").on("click", function () {
        //关闭弹出层
        var index = parent.layer.getFrameIndex(window.name);
        parent.layer.close(index);
    });
});

/**加载页面数据*/
function loadPageData() {
    connect = JSON.parse(parent.connectRouter.querysConnect(parent.rowDataId));
    $("#text").val(connect.text);
    $("#rhost").val(connect.rhost);
    $("#rport").val(connect.rport);
    $("#rpass").val(connect.rpass);
    if (connect.type === '1') {
        $("#sname").val(connect.sname);
        $("#shost").val(connect.shost);
        $("#sport").val(connect.sport);
        $("#spass").val(connect.spass);
        $("#spkey").val(connect.spkey);
    }
}

/**显示数据视图*/
function showDataView() {
    var type = $("#type");
    var isha = $("#isha");
    if (connect.isha === '0') {
        isha.attr('checked', false);
        $('.div-input05 .layui-form-checkbox[lay-skin="primary"] i').css('background', '#E0E0E2');
    } else {
        isha.attr('checked', true);
        $('.div-input05 .layui-form-checkbox[lay-skin="primary"] i').css('background', '#5FB878');
    }
    var sshInput = $(".ssh-input");
    if (connect.type === '0') {
        type.attr('checked', false);
        sshInput.val('');
        sshInput.attr("disabled", "disabled");
        sshInput.attr('lay-verify', '');
        sshInput.css('background', '#E0E0E2');
        $('.div-input04 .layui-form-checkbox[lay-skin="primary"] i').css('background', '#E0E0E2');
    } else {
        type.attr('checked', true);
        sshInput.removeAttr("disabled");
        sshInput.attr('lay-verify', 'required');
        sshInput.css('background', 'transparent');
        //排除SSH密码和私钥的校验
        $("#spass").attr('lay-verify', '');
        $("#spkey").attr('lay-verify', '');
        $('.div-input04 .layui-form-checkbox[lay-skin="primary"] i').css('background', '#5FB878');
    }
}


/**显示连接视图*/
function showConView() {
    var sshInput = $(".ssh-input");
    sshInput.attr("disabled", "disabled");
    sshInput.val('');
    sshInput.attr('lay-verify', '');
    sshInput.css('background', '#E0E0E2');
    $('.div-input04 .layui-form-checkbox[lay-skin="primary"] i').css('background', '#E0E0E2');
}


/**显示SSH视图*/
function showSshView() {
    $("#sport").val(22);
    var sshInput = $(".ssh-input");
    sshInput.removeAttr("disabled");
    sshInput.attr('lay-verify', 'required');
    sshInput.css('background', 'transparent');
    //排除SSH密码和私钥的校验
    $("#spass").attr('lay-verify', '');
    $("#spkey").attr('lay-verify', '');
    $('.div-input04 .layui-form-checkbox[lay-skin="primary"] i').css('background', '#5FB878');
}

/**显示测试视图*/
function showProView() {
    var type, isha;
    ($("#type").get(0).checked) ? type = "1" : type = "0";
    ($("#isha").get(0).checked) ? isha = "1" : isha = "0";
    if (type == '1' && isha == '1') {
        layer.msg('集群模式下无法使用SSH通道连接!');
        return false;
    }
    var spkey = $("#spkey").val();
    var spass = $("#spass").val();
    if (type == '1' && spkey === "" && spass === "") {
        layer.msg('密码或私钥必须填写一项!');
        return false;
    }
    var index = layer.load(2, {time: 30 * 1000, shade: 0.5});
    var data = {
        "id": parent.rowDataId,
        "type": type,
        "isha": isha,
        "text": $("#text").val(),
        "rhost": $("#rhost").val(),
        "rport": $("#rport").val(),
        "rpass": $("#rpass").val(),
        "sname": $("#sname").val(),
        "shost": $("#shost").val(),
        "sport": $("#sport").val(),
        "spass": spass,
        "spkey": spkey
    };
    var resultJson = parent.connectRouter.detectConnect(1, JSON.stringify(data));
    var result = JSON.parse(resultJson);
    if (result.code) {
        layer.close(index);
    }
    layer.msg(result.msgs);
}

/**保存连接信息*/
function saveConnect() {
    var type, isha;
    ($("#type").get(0).checked) ? type = "1" : type = "0";
    ($("#isha").get(0).checked) ? isha = "1" : isha = "0";
    if (type == '1' && isha == '1') {
        layer.msg('集群模式下无法使用SSH通道连接!');
        return false;
    }
    var spkey = $("#spkey").val();
    var spass = $("#spass").val();
    if (type == '1' && spkey === "" && spass === "") {
        layer.msg('密码或私钥必须填写一项!');
        return false;
    }
    var data = {
        "id": parent.rowDataId,
        "type": type,
        "isha": isha,
        "text": $("#text").val(),
        "rhost": $("#rhost").val(),
        "rport": $("#rport").val(),
        "rpass": $("#rpass").val(),
        "sname": $("#sname").val(),
        "shost": $("#shost").val(),
        "sport": $("#sport").val(),
        "spass": spass,
        "spkey": spkey
    };
    var json = parent.connectRouter.updateConnect(JSON.stringify(data));
    var result = JSON.parse(json);
    if (result.code === 200) {
        var index = parent.layer.getFrameIndex(window.name);
        parent.layer.close(index);
        parent.getConnectData();
    } else {
        layer.alert(result.msgs, {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
    }
}