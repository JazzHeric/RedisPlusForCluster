$(function () {

    //获取后台数据
    var json = parent.connectRouter.pickedConnect()
    var data = JSON.parse(json);
    var mark = data.rhost + ":" + data.rport + ">";
    $("#initAddr").text(mark);
    //获得窗口焦点
    window.focus();

    //定义原始欢迎模板
    var templateRoot = '';
    templateRoot += '<div class="output-root output-view">';
    templateRoot += '<p>welcome to redisplus terminal mode</p>';
    templateRoot += '</div>';
    var templateRootOutput = _.template(templateRoot);

    //定义原始模板
    var templateHtml = '';
    templateHtml += '<div class="output-view">';
    templateHtml += '<span class="prompt"><%= separate %></span>';
    templateHtml += '<span class="output<%= error %>"><%= value %></span>';
    templateHtml += '</div>';
    var templateOutput = _.template(templateHtml);

    //命令缓存
    var cmdCache = [];
    var cmdPos = 0;
    var $left = $('.left');
    var $right = $('.right');
    var $cursor = $('.cursor');
    var $shell = $('.shell-view');
    var $input = $('.input');
    var strLeft = '';
    var strRight = '';
    var strCursor = '';
    var strTmpCursor = '';
    var flagEnd = false;

    //光标闪烁效果
    setInterval(function () {
        $cursor.toggleClass('blink');
    }, 1000);

    //监听键盘事件
    $(document).keypress(function (e) {
        if (e.which === 32) {
            //space
            $left.append('&nbsp;&nbsp;');
        }
        if (e.which !== 13) {
            $left.append(String.fromCharCode(e.which));
        }
    });

    // 功能键
    $(document).keydown(function (e) {
        var keyCode = e.which;
        switch (keyCode) {
            case 8:
                //backspace
                e.preventDefault();
                strLeft = $left.text();
                if (strLeft.length === 0) {
                    return;
                }
                strLeft = strLeft.substring(0, strLeft.length - 1);
                $left.text(strLeft);
                break;
            case 13:
                //enter
                var cmd = $.trim($input.text());
                var valOuput = '';
                var errClass = '';
                var isPrint = true;
                if (cmd !== '') {
                    cmdCache.push(cmd);
                    cmdCache = _.uniq(cmdCache);
                }
                if (cmdCache.length > 0) {
                    cmdPos = cmdCache.length - 1;
                }
                switch (cmd) {
                    case 'help':
                        valOuput += '1.clear：清空终端信息<br/>';
                        valOuput += '2.close：关闭终端窗口<br/>';
                        valOuput += '3.other：...<br/>';
                        break;
                    case 'clear':
                        $shell.siblings().remove();
                        $shell.before(templateRootOutput);
                        isPrint = false;
                        break;
                    case 'close':
                        var index = parent.layer.getFrameIndex(window.name);
                        parent.layer.close(index);
                        break;
                    default:
                        valOuput = parent.connectRouter.cmdwinConnect(cmd);
                        break;
                }
                $left.text('');
                $cursor.html('&nbsp;');
                $right.text('');
                if (isPrint) {
                    //显示命令行数据
                    $shell.before(templateOutput({separate: mark, value: cmd, error: ''}));
                    //显示响应行数据
                    $shell.before(templateOutput({separate: '', value: valOuput, error: errClass}));
                }
                goBottom();
                break;
            case 35:
                //end
                strRight = $right.text();
                strCursor = $cursor.text();
                var str_all_01 = $input.text();
                if (strRight.length === 0 && $.trim(strCursor).length === 0) {
                    return;
                }
                $left.text(str_all_01);
                $cursor.html('&nbsp;');
                $right.text('');
                break;
            case 36:
                //home
                strLeft = $left.text();
                var str_all_02 = $input.text();
                if (strLeft.length === 0) {
                    return;
                }
                $left.text('');
                $cursor.text(str_all_02.substring(0, 1));
                $right.text(str_all_02.substring(1, str_all_02.length));
                break;
            case 37:
                //向左方向键
                strLeft = $left.text();
                strRight = $right.text();
                strCursor = $cursor.text();
                strTmpCursor = '';
                if (strLeft.length === 0) {
                    return;
                }
                strTmpCursor = strLeft.substring(strLeft.length - 1, strLeft.length);
                strLeft = strLeft.substring(0, strLeft.length - 1);
                if (!($cursor.html() === '&nbsp;' && strRight.length === 0 && $.trim(strTmpCursor) !== '')) {
                    strRight = strCursor + strRight;
                }
                $left.text(strLeft);
                $cursor.text(strTmpCursor);
                $right.text(strRight);
                break;
            case 38:
                //向上方向键
                e.preventDefault();
                if (cmdPos < 0) {
                    return;
                }
                $left.text(cmdCache[cmdPos]);
                cmdPos--;
                $cursor.html('&nbsp;');
                $right.text('');
                break;
            case 39:
                //向右方向键
                strLeft = $left.text();
                strRight = $right.text();
                strCursor = $cursor.text();
                flagEnd = false;
                if (strRight.length === 0) {
                    if ($cursor.html() === '&nbsp;') {
                        return;
                    }
                    flagEnd = true;
                }
                strLeft += strCursor;
                if (flagEnd) {
                    $cursor.html('&nbsp;');
                    strRight = '';
                } else {
                    $cursor.text(strRight.substring(0, 1));
                    strRight = strRight.substring(1);
                }
                $left.text(strLeft);
                $right.text(strRight);
                break;
            case 40:
                //向下方向键
                e.preventDefault();
                if (cmdPos >= cmdCache.length - 1) {
                    $left.text('');
                } else {
                    cmdPos++;
                    $left.text(cmdCache[cmdPos]);
                }
                $cursor.html('&nbsp;');
                $right.text('');
                break;
            case 46:
                //delete
                strRight = $right.text();
                if (strRight.length === 0) {
                    if ($cursor.html() === '&nbsp;') {
                        return;
                    }
                    flagEnd = true;
                }
                if (flagEnd) {
                    $cursor.html('&nbsp;');
                } else {
                    $cursor.text(strRight.substring(0, 1));
                    $right.text(strRight.substring(1));
                }
                break;
        }
        //组合快捷键
        if (e.which === 76 && e.ctrlKey) {
            // Ctrl + L
            e.preventDefault();
            $shell.siblings().remove();
        }
        if (e.which === 85 && e.ctrlKey) {
            // Ctrl + U
            e.preventDefault();
            $left.text('');
        }
        if (e.which === 86 && e.ctrlKey) {
            // Ctrl + V
            e.preventDefault();
            parent.layer.prompt({
                title: '粘贴复制的内容',
                formType: 3,
                closeBtn: 0
            }, function (text, iindex) {
                $left.text(text);
                $cursor.html('&nbsp;');
                $right.text('');
                //enter
                var cmd = $.trim($input.text());
                var valOuput = '';
                var errClass = '';
                var isPrint = true;
                if (cmd !== '') {
                    cmdCache.push(cmd);
                    cmdCache = _.uniq(cmdCache);
                }
                if (cmdCache.length > 0) {
                    cmdPos = cmdCache.length - 1;
                }
                switch (cmd) {
                    case 'help':
                        valOuput += '1.clear：清空终端信息<br/>';
                        valOuput += '2.close：关闭终端窗口<br/>';
                        valOuput += '3.other：...<br/>';
                        break;
                    case 'clear':
                        $shell.siblings().remove();
                        $shell.before(templateRootOutput);
                        isPrint = false;
                        break;
                    case 'close':
                        var index = parent.layer.getFrameIndex(window.name);
                        parent.layer.close(index);
                        break;
                    default:
                        valOuput = parent.connectRouter.cmdwinConnect(cmd);
                        break;
                }
                $left.text('');
                $cursor.html('&nbsp;');
                $right.text('');
                if (isPrint) {
                    //显示命令行数据
                    $shell.before(templateOutput({separate: mark, value: cmd, error: ''}));
                    //显示响应行数据
                    $shell.before(templateOutput({separate: '', value: valOuput, error: errClass}));
                }
                goBottom();
                parent.layer.close(iindex);
            });
        }

    });
});

//滚动到底部
function goBottom() {
    var win = $("#panel-shell")[0];
    win.scrollTop = win.scrollHeight;
}
