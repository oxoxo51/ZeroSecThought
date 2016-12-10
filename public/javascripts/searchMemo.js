$(function(){
    $('#submitCondition').click(function(){
        search();
    });
    $('#clearCondition').click(function(){
        $('#conditionTitle').val("");
        $('#conditionContent').val("");
        $('#conditionDateFrom').val("");
        $('#conditionDateTo').val("");
    });
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
    });
    search();
});
$(document).on('click', '.memoRow', (function(){
    var jsondata = {
        "id": this.id
    };
    var node = this;
    $.ajax({
        url: "/delete",
        type: 'POST',
        data: jsondata,
        success: function(result){
            // 行削除
            $(node).parents("tr").remove();
            var count = Number($('#resArrCount').text()) - 1;

            $('#resArrCount').html(count);
        }
    });
}));
/**
 * 検索.
 *
 * 検索条件をサーバーにpostし、検索結果からhtmlを生成しajaxで描画する.
 *
 *
 */
function search() {
    var conditionTitle = $('#conditionTitle').val();
    var conditionContent = $('#conditionContent').val();
    var conditionDateFrom = $('#conditionDateFrom').val();
    var conditionDateTo = $('#conditionDateTo').val();
    var jsondata = {
        "conditionTitle": conditionTitle,
        "conditionContent": conditionContent,
        "conditionDateFrom": conditionDateFrom,
        "conditionDateTo": conditionDateTo
    };
    $.ajax({
        url: "/search",
        type: 'POST',
        data: jsondata,
        complete: function(result){
            resArr = result.responseJSON;
            resCount = resArr.length
            htmlStr = createHtml(resArr, conditionTitle, conditionContent);
            $('#searchResult').html(htmlStr);
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
        }
    });
}

function createHtml(resArr, conditionTitle, conditionContent) {
    var htmlStr = (
          "<thead><tr><th>削除</th><th>タイトル</th><th>内容</th><th>作成日</th></tr></thead>"
        + "<tbody id='itemContainer'>"
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

    htmlStr += "</tbody>";
    return htmlStr;
}

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
    // 1行分のHtmlを作成して返却
    return (
        "<tr><td>"
        + "<button class='memoRow' id='" + resArrLine.id + "'>×</button></td><td>"
        + "<a href='/edit/" + resArrLine.id + "'>" + title + "</a></td><td>"
        + content + "</td><td>"
        + resArrLine.createDate + "</td></tr>");
}