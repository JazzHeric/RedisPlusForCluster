var $;
var form;
var layer;
var step = 0;
var colorpicker;


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
    layui.use(['jquery', 'form', 'layer', 'colorpicker'], function () {
        $ = layui.jquery;
        form = layui.form;
        layer = layui.layer;
        colorpicker = layui.colorpicker;
        //表单赋值
        var themeColor = otherRouter.getSetting("theme-color");
        $('#color-inp').val(themeColor);
        var connectTheme = otherRouter.getSetting("connect-theme");
        $('#connect-theme').val(connectTheme);
        //渲染表单
        form.render();
        colorpicker.render({
            elem: '#color-box',
            color: themeColor,
            done: function (color) {
                $('#color-inp').val(color);
                otherRouter.setSetting("theme-color", color);
            }
        });
        //注册事件
        $("#color-btn").on("click", function () {
            otherRouter.setSetting("theme-color", "#D6D6D7");
        });
        //监听连接主题选择
        form.on('select(connect-theme)', function (data) {
            otherRouter.setSetting("connect-theme", data.value);
        });
    });
}

