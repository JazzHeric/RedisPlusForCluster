function toPage(flag, page) {
    var isOpen = connectRouter.isopenConnect("");
    if (flag === 1 && isOpen === 0) {
        layer.alert('请先连接一个可用的服务！', {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
        return false;
    }
    layer.load(2);
    otherRouter.changeWebview(page);
    layer.closeAll('loading');
}

function formatDate(time) {
    var format = "YY-MM-DD hh:mm:ss";
    var date = new Date(time);
    var year = date.getFullYear(), month = date.getMonth() + 1, // 月份是从0开始的
        day = date.getDate(), hour = date.getHours(), min = date.getMinutes(), sec = date
            .getSeconds();
    var preArr = Array.apply(null, Array(10)).map(function (elem, index) {
        return '0' + index;
    });// //开个长度为10的数组 格式为 00 01 02 03
    var newTime = format.replace(/YY/g, year).replace(/MM/g,
        preArr[month] || month).replace(/DD/g, preArr[day] || day).replace(
        /hh/g, preArr[hour] || hour).replace(/mm/g, preArr[min] || min)
        .replace(/ss/g, preArr[sec] || sec);
    return newTime;
}

function formatTimestamp(data) {
    var ts = arguments[0] || 0;
    var t, y, m, d, h, i, s;
    t = ts ? new Date(ts * 1000) : new Date();
    y = t.getFullYear();
    m = t.getMonth() + 1;
    d = t.getDate();
    h = t.getHours();
    i = t.getMinutes();
    s = t.getSeconds();
    return y + '-' + (m < 10 ? '0' + m : m) + '-' + (d < 10 ?
        '0' + d : d) + ' ' + (h < 10 ? '0' + h : h) + ':' + (i < 10 ? '0' + i : i) + ':' + (s < 10 ? '0' + s : s);
}