var $;
var form;
var layer;
var step = 0;


window.onload = function () {

    function isReady() {
        if (step < 160 && !window.otherRouter) {
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
    layui.use(['form', 'layer', 'jquery'], function () {
        $ = layui.jquery;
        form = layui.form;
        layer = layui.layer;
        form.on('submit(sendMail)', function () {
            sendMail();
            return false;
        });
    });
}


/**发送邮件*/
function sendMail() {
    layer.load(2);
    var sendBtn = $("#sendMail");
    sendBtn.text("发送中...").attr("disabled", "disabled").addClass("layui-disabled");
    var addr = $("#mailAddr");
    var text = $("#mailText");
    var resultJson = otherRouter.sendMail(addr.val(), text.val());
    var result = JSON.parse(resultJson);
    if (result.code === 200) {
        layer.msg(result.msgs);
        addr.val('');
        text.val('');
    } else {
        layer.alert(result.msgs, {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
    }
    sendBtn.text("发送邮件").removeAttr("disabled").removeClass("layui-disabled");
    layer.closeAll('loading');
}