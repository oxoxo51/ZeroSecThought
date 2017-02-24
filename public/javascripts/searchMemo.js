/**
 * 画面起動時の初期設定.
 */
$(function(){
    var tmpConditionTitle = "";
    var tmpConditionContent = "";
    var tmpConditionDateFrom = "";
    var tmpConditionDateTo = "";

    /**
     * 「テンプレートを元に作成」ボタン押下イベント.
     *  選択時のactive化、メニュー閉じ
     */
    $('#basedTemplate,#openDispMenu').click(function(){
        if($(this).parents("li").attr('class')) {
            $(this).parents("li").removeClass();
        } else {
            $(this).parents("li").addClass("active");
        }
        $("#navbarMenu").removeClass("in");
    });
     /**
     * クリアボタン押下イベント.
     * 検索条件をクリアして検索する.
     */
    $('#clearCondition').click(function(){
        $('#conditionTitle').val("");
        $('#conditionContent').val("");
        $('#conditionDateFrom').val("");
        $('#conditionDateTo').val("");
        $('input[name="sortKey"]:radio')[0].checked = true;
        $('input[name="sortOrder"]:radio')[0].checked = true;
        $('#favCheck').prop('checked', false);
        search();
    });
    /**
     * 「今日」ボタン押下イベント.
     *  検索条件の日付from/toに今日の日付をセットして検索する.
     */
    $('#setToday').click(function(){
        var today = new Date();
        var dd = today.getDate();
        var mm = today.getMonth() + 1;
        var yyyy = today.getFullYear();
        if (dd < 10) {dd = '0' + dd}
        if (mm < 10) {mm = '0' + mm}
        today = yyyy + '-' + mm + '-' + dd;
        $('#conditionDateFrom').val(today);
        $('#conditionDateTo').val(today);
        search();
    });
    /* focusout時、focusin時点と値が変わっている場合のみ検索 */
    /* 検索条件：タイトル */
    $('#conditionTitle').focusin(function(){
        tmpConditionTitle = this.value;
    });
    $('#conditionTitle').focusout(function(){
        if (tmpConditionTitle !== this.value) {
            search();
        }
    });
    /* 検索条件：本文 */
    $('#conditionContent').focusin(function(){
        tmpConditionContent = this.value;
    });
    $('#conditionContent').focusout(function(){
        if (tmpConditionContent !== this.value) {
            search();
        }
    });
    /* 検索条件：日付from/to */
    $('#conditionDateFrom').focusin(function(){
        tmpConditionDateFrom = this.value;
    });
    $('#conditionDateFrom').focusout(function(){
        if(tmpConditionDateFrom !== this.value) {
            search();
        }
    });
    $('#conditionDateTo').focusin(function(){
        tmpConditionDateTo = this.value;
    });
    $('#conditionDateTo').focusout(function(){
        if(tmpConditionDateTo !== this.value) {
            search();
        }
    });
    /* チェックボックス・ラジオボタン：変更と同時に検索 */
    $('#favCheck').change(function(){
        search();
    });
    $('input[name="sortKey"]:radio').change(function(){
        search();
    });
    $('input[name="sortOrder"]:radio').change(function(){
        search();
    });
    /**
     * 画面起動時にラジオボタン・チェックボックスの値設定し検索.
     * (サーバーでhtml生成時にキャッシュから検索条件が設定される)
     */
    setRadioVal();
    setFavCheck();
    search();
});

/**
 * 削除確認ダイアログ表示.
 */
$(document).on('show.bs.modal', '#modalFade', function(){
    var button = $(event.target);
    var msg = "「" + String(button.data('name')).replace('<mark>', '').replace('</mark>', '') + "」を削除します。";
    var modal = $(this);
    var delId = button.attr('id');
    // 削除ボタン押下後にIDを渡すため、メッセージのクラスにIDを設定
    modal.find('#delMsg').removeClass();
    modal.find('#delMsg').addClass(delId);
    modal.find('#delMsg').html(msg);
    modal.find('#delOk').focus();
});

/**
 * 削除ダイアログ「削除」ボタン押下イベント.
 */
$(document).on('click', '#delOk', function(){
    // 削除対象IDを取得しjsonにセット
    var id = $('#delMsg').attr('class')
    var jsondata = {
        "id": id
    };
    var node = this;
    $.ajax({
        url: "/delete",
        type: 'POST',
        data: jsondata,
        success: function(result){
            // 削除するメモの日付を取得
            var memoDate = $('button#' + id).parents('td').nextAll('td.memo_date').text();
            var dateCount = Number($('#cnt_' + memoDate.replace(/\//g, "\\/")).text()) - 1;
            $('#cnt_' + memoDate.replace(/\//g, "\\/")).html(dateCount);
            // 行削除
            $('#resArrCount').html(removeLine(id));
        }
    });
});

/**
 * テンプレート削除確認ダイアログ表示.
 */
$(document).on('show.bs.modal', '#modalTemplateFade', function(){
    var button = $(event.target);
    var msg = "「テンプレート：" + button.data('name') + "」を削除します。";
    var modal = $(this);
    var delId = button.attr('id').split("template_")[1];
    // 削除ボタン押下後にIDを渡すため、メッセージのクラスにIDを設定
    modal.find('#delTemplateMsg').removeClass();
    modal.find('#delTemplateMsg').addClass(delId);
    modal.find('#delTemplateMsg').html(msg);
    modal.find('#delTemplateOk').focus();
});

/**
 * テンプレート削除ダイアログ「削除」ボタン押下イベント.
 */
$(document).on('click', '#delTemplateOk', function(){
    // 削除対象IDを取得しjsonにセット
    var id = $('#delTemplateMsg').attr('class')
    var jsondata = {
        "id": id
    };
    var node = this;
    $.ajax({
        url: "/deleteTemplate",
        type: 'POST',
        data: jsondata,
        success: function(result){
            // 行削除
            // 消すライン：削除ボタンのIDから要素特定
            $('button#template_' + id).parents("li").remove();
        }
    });
});
/**
 * 過去日付クリック時イベント.
 * クリックした日付を検索条件の日付from/toに設定して検索する.
 */
$(document).on('click', '.memoCnt', function(){
    var date = $(this).text().replace(/\//g, '-');
    $('#conditionDateFrom').val(date);
    $('#conditionDateTo').val(date);
    search();
});

/**
 * favクリック時イベント.
 * favの状態と対象のメモIDを取得し、favの状態を更新する.
 */
$(document).on('click', '.fav-star', function(){
    var class_arr = $(this).attr('Class').split(" ");
    // メモid取得
    var memo_id = $(this).attr('id').split("fav_")[1];
    var fav_flg = "0";
    // fav on/off
    for (var i = 0; i < class_arr.length; i++) {
        if (class_arr[i] == 'glyphicon-star-empty') {
            $(this).removeClass('glyphicon-star-empty');
            $(this).addClass('glyphicon-star');
            fav_flg = "1";
        } else if (class_arr[i] == 'glyphicon-star') {
            $(this).removeClass('glyphicon-star');
            $(this).addClass('glyphicon-star-empty');
            fav_flg = "0";
        }
    }
    upd_fav(memo_id, fav_flg);
});

/**
 * 検索.
 * 検索条件をサーバーにpostし、検索結果からhtmlを生成しajaxで描画する.
 */
function search() {
    // サーバーから検索結果が返されるまで「検索中」イメージ表示
    dispLoading("検索中...");
    var conditionTitle = $('#conditionTitle').val();
    var conditionContent = $('#conditionContent').val();
    var conditionDateFrom = $('#conditionDateFrom').val();
    var conditionDateTo = $('#conditionDateTo').val();
    var sortKey = getRadioVal('sortKey');
    var sortOrder = getRadioVal('sortOrder');
    var favChecked = getFavCheck();
    // 検索条件をjsonオブジェクトに設定
    var jsondata = {
        "conditionTitle": conditionTitle,
        "conditionContent": conditionContent,
        "conditionDateFrom": conditionDateFrom,
        "conditionDateTo": conditionDateTo,
        "sortKey": sortKey,
        "sortOrder": sortOrder,
        "favChecked": favChecked
    };
    $.ajax({
        url: "/search",
        type: 'POST',
        data: jsondata,
        complete: function(result){
            resArr = result.responseJSON;
            resCount = resArr.length
            // レスポンスのJSONから検索結果のHTMLを生成し、描画
            htmlStr = createHtml(resArr, conditionTitle, conditionContent);
            $('#searchResult').html(htmlStr);
            // 結果件数を画面表示
            $('#resultCount').html("<span id='resArrCount'>" + resCount + "</span><span>件</span>");
            // ページネーションの設定
            $("span.holder").jPages({
                containerID  : "itemContainer",
                previous     : "＜",
                next         : "＞",
                perPage      : 10,
                startPage    : 1,
                startRange   : 1,
                midRange     : 5,
                endRange     : 1
            });
            // 「検索中」イメージを消す
            removeLoading();
        }
    });
}

/**
 * fav更新.
 * 検索結果一覧のfav on/offをサーバーに投げる
 */
function upd_fav(id, flag) {
    // メモID、ON/OFFを渡す
    var jsondata = {
        "memoId": id,
        "favFlg": flag
    };
    $.ajax({
        url: "/updfav",
        type: 'POST',
        data: jsondata,
        complete: function(result){
            // 「favのみ」がチェックされている場合、on/off切替で抽出条件から外れるため、
            // 一覧から削除および件数-1する
            if (getFavCheck() === "1") {
                removeLine(id);
            }
        }
    });
}

/**
 * 「検索中」イメージ表示.
 */
function dispLoading(msg) {
    // 画面表示メッセージ
    var dispMsg = "";
    // 引数が空の場合は画像のみ表示
    if (msg != "") {
        dispMsg = "<div class='loadingMsg'>" + msg + "</div>"
    }
    // ローディング画像が表示されていない場合のみ表示
    if ($('#loading').size() == 0) {
        $('#searchResult').html("<div id='loading'>" + dispMsg + "</div>");
    }
}

/**
 * 「検索中」イメージ削除.
 */
function removeLoading() {
    $('#loading').remove();
}

/**
 * 検索結果一覧HTML作成.
 * 検索結果のJSONをもとに検索結果一覧のHTMLを作成.
 * 検索条件タイトル・本文を一覧上でハイライト表示する.
 */
function createHtml(resArr, conditionTitle, conditionContent) {
    // 検索結果一覧ヘッダ部
    var htmlStr = (
            "<thead><tr><th>fav</th><th>タイトル</th><th>内容</th><th>作成日</th><th>削除</th></tr></thead>"
        +   "<tbody id='itemContainer'>"
    );
    // 親子関係反映後の配列を初期化
    var treeArr = [];
    for (var i = 0; i < resArr.length; i++) {
        var keyId = resArr[i].parentId;
        var treeHaveParentFlag = false;
        // 配列(resArr)を親子関係を反映した配列(treeArr)に詰め替える
        for (var j = 0; j < treeArr.length; j++) {
            // 親なしが明らかな場合はスキップ
            if (resArr[i].parentId === 0) {
                break;
            }
            // 1. treeArrに親がいるか探す。
            if (resArr[i].parentId === treeArr[j].id) {
                // 1-1. 1=true:親の後に要素追加、resArrからは要素削除
                treeArr = treeArr.slice(0,j+1).concat(resArr.splice(i,1), treeArr.slice(j+1, treeArr.length));
                // 追加した子要素をインデント
                // 親のインデント数を確認し、+1だけインデントする
                var indent = treeArr[j].title.split("→").length;
                treeArr[j+1].title = Array(indent+1).join("→") + " " + treeArr[j+1].title;
                // 削除した分再度同じインデックスでループを回すために戻す
                i--;
                // 追加した分インデックスをカウントアップしておく
                j++;
                // 同じ親IDを持つ要素が他にresArrにいないか探す。いれば1で追加した要素の後に要素追加、resArrからは要素削除
                for (var k = 0; k < resArr.length; k++) {
                    if (keyId === resArr[k].parentId) {
                        treeArr = treeArr.slice(0,j+1).concat(resArr.splice(k,1), treeArr.slice(j+1, treeArr.length));
                        // 追加した子要素をインデント
                        treeArr[j+1].title = Array(indent+1).join("→") + " " + treeArr[j+1].title;
                        // 追加した分インデックスをカウントアップしておく
                        j++;
                    }
                }
                // 親は複数いないのでresArrループの次のインデックスへ
                treeHaveParentFlag = true;
                break;
            }
        }
        if (!treeHaveParentFlag) {
            // 1-2. 1=false:resArrに親がいないか探す。いれば一旦とばす(親の後に移動)
            var resHaveParentFlag = false;
            for (var l = i+1; l < resArr.length; l++) {
                // 親なしが明らかな場合はスキップ
                if (resArr[i].parentId === 0) {
                    break;
                }
                if (resArr[i].parentId === resArr[l].id) {
                    // i番目をl+1番目に移動：[0~i-1]+[i+1~l]+[i]+[l+1~末尾]
                    resArr = resArr.slice(0,i).concat(resArr.slice(i+1,l+1), resArr.slice(i,i+1), resArr.slice(l+1, resArr.length));
                    // 後ろに移動した分再度同じインデックスでループを回すために戻す
                    i--;
                    // 親は複数いないのでfor文を抜ける
                    resHaveParentFlag = true;
                    break;
                }
            }
            if (!resHaveParentFlag) {
                // 1-2-1. 1-2=false:いなければtreeArrの末尾に追加してresArrから削除
                treeArr = treeArr.concat(resArr.splice(i,1));
                // 削除した分再度同じインデックスでループを回すために戻す
                i--;
                // 同じ親IDを持つ要素が他にresArrにいないか探す。いれば1-2で追加した要素の後に要素追加、resArrからは要素削除
                for (var m = i+1; m < resArr.length; m++) {
                    if (keyId === resArr[m].parentId) {
                        treeArr = treeArr.concat(resArr.splice(m,1));
                        m--;
                    }
                }
            }
        }
    }
    // 出来上がったtreeArrからHTML作成
    for (var n = 0; n < treeArr.length; n++) {
        htmlStr += createHtmlLine(treeArr[n], conditionTitle, conditionContent);
    }

    // 検索結果一覧終了タグ
    htmlStr += "</tbody>";
    return htmlStr;
}

/**
 * 検索結果一覧：ライン作成.
 * ライン単位でHTML作成.
 * 検索条件タイトル・本文をハイライト表示する.
 */
function createHtmlLine(resArrLine, conditionTitle, conditionContent) {
    // 検索条件のタイトル・本文に合致する箇所をmarkする
    var titleRegExp = new RegExp(conditionTitle, "g");
    var contentRegExp = new RegExp(conditionContent, "g");
    var title = resArrLine.title;
    if (conditionTitle !== "") {
        title = title.replace(titleRegExp, '<mark>' + conditionTitle + '</mark>');
    }
    var content = resArrLine.content.replace(/\r\n/g,'<br>');
    if (conditionContent !== "") {
        content = content.replace(contentRegExp, '<mark>' + conditionContent + '</mark>');
    }
    // fav on/off
    var starClass = "glyphicon-star-empty"; // off
    if (resArrLine.fav == "1") {
        starClass = "glyphicon-star"; // on
    }
    // 1行分のHtmlを作成して返却
    return (
        "<tr>"
        + "<td><button id='fav_" + resArrLine.id + "' class='btn btn-link btn-xs fav-star glyphicon " + starClass + "' aria-hidden='true'></button></td>"
        + "<td><a href='/edit/" + resArrLine.id + "'>" + title + "</a></td>"
        + "<td>" + content + "</td>"
        + "<td class='memo_date'>" + resArrLine.createDate + "</td>"
        + "<td><button type='button' class='memoRow btn btn-default btn-sm' id='" + resArrLine.id + "' data-toggle='modal' data-target='#modalFade' data-name='" + title + "'>×</button></td>"
        + "</tr>"
    );
}

/**
 * 行削除.
 * 渡されたIDの行を検索結果一覧から削除する.
 */
function removeLine(id) {
    // 消すライン：削除ボタンのIDから要素特定
    $('button#' + id).parents("tr").remove();
    // 一覧の件数を1件減らす
    $('#resArrCount').html(Number($('#resArrCount').text()) - 1);
}
