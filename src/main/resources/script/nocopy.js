document.oncontextmenu = function () {
    return false;
};
document.onselectstart = function () {
    return false;
};

//复制到剪贴板
function copyToClipboard(text) {
    // if (text.indexOf('-') !== -1) {
    //     var arr = text.split('-');
    //     text = arr[0] + arr[1];
    // }
    var textArea = document.createElement("textarea");
    textArea.style.position = 'fixed';
    textArea.style.top = '0';
    textArea.style.left = '0';
    textArea.style.width = '2em';
    textArea.style.height = '2em';
    textArea.style.padding = '0';
    textArea.style.border = 'none';
    textArea.style.outline = 'none';
    textArea.style.boxShadow = 'none';
    textArea.style.background = 'transparent';
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    try {
        document.execCommand('copy');
        layer.msg('成功复制到剪贴板');
    } catch (err) {
        layer.msg('该浏览器不支持点击复制到剪贴板');
    }
    document.body.removeChild(textArea);
}
