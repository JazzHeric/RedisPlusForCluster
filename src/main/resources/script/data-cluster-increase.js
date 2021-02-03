var $;
var form;
var layer;

layui.use(['jquery', 'form', 'layer'], function () {
    $ = layui.jquery;
    form = layui.form;
    layer = layui.layer;
    form.render();

    form.on('select(type)', function (data) {
        var zsco = $("#zsco");
        var mkey = $("#mkey");
        var mval = $("#mval");
        var vals = $("#vals");
        var zsetData = $("#zset-data");
        var hashData = $("#hash-data");
        var elseData = $("#else-data");
        var index = parent.layer.getFrameIndex(window.name);
        if (data.value === '2') {
            hashData.css('display', 'none');
            elseData.css('display', 'block');
            zsetData.css('display', 'block');
            mkey.attr('lay-verify', '');
            mval.attr('lay-verify', '');
            zsco.attr('lay-verify', 'required');
            vals.attr('lay-verify', 'required');
            parent.layer.style(index, {
                height: '460px'
            });
        } else if (data.value === '4') {
            zsetData.css('display', 'none');
            elseData.css('display', 'none');
            hashData.css('display', 'block');
            zsco.attr('lay-verify', '');
            vals.attr('lay-verify', '');
            mkey.attr('lay-verify', 'required');
            mval.attr('lay-verify', 'required');
            parent.layer.style(index, {
                height: '350px'
            });
        } else {
            hashData.css('display', 'none');
            zsetData.css('display', 'none');
            elseData.css('display', 'block');
            zsco.attr('lay-verify', '');
            mkey.attr('lay-verify', '');
            mval.attr('lay-verify', '');
            vals.attr('lay-verify', 'required');
            parent.layer.style(index, {
                height: '410px'
            });
        }
    });

    //监听提交
    form.on('submit(submitBtn)', function () {
        var zsco = $("#zsco").val();
        var keys = $("#keys").val();
        var type = $("#type").val();
        var time = $("#time").val();
        var checkTimeFlag = /^(0|[1-9][0-9]*)$/.test(time);
        if (time != -1 && !checkTimeFlag) {
            layer.msg('ttl只能输入整数值');
            return;
        }
        var checkZscoFlag = /^(0|[1-9][0-9]*)$/.test(zsco);
        if (type === '2' && !checkZscoFlag) {
            layer.msg('score只能输入整数值');
            return;
        }
        var vals;
        if (type === '4') {
            vals = $("#mkey").val() + ":" + $("#mval").val();
        } else {
            vals = $("#vals").val();
        }
        var score;
        if (type === '2') {
            score = zsco;
        } else {
            score = 0;
        }
        layer.load(2);
        var json = parent.dataClusterRouter.insertKey(type, keys, vals, time, score);
        var data = JSON.parse(json);
        layer.closeAll('loading');
        var index = parent.layer.getFrameIndex(window.name);
        parent.layer.close(index);
        if (data.code === 200) {
            layer.msg(data.msgs);
            parent.loadDbTree();
        } else {
            layer.alert(data.msgs, {
                skin: 'layui-layer-lan',
                closeBtn: 0
            });
        }
        return false;
    });

    //注册监听返回事件
    $("#gobackBtn").on("click", function () {
        var index = parent.layer.getFrameIndex(window.name);
        parent.layer.close(index);
        return false;
    });

});
