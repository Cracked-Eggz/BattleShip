"use strict";

// Handle back-end server interactions for game UI.
const myAppObj = new Vue({
    el: "#gameApp",
    data: {
        authorName: "(waiting for server...)",
        game: null,
        board: null,
    },

    methods: {
        newGame: makeNewGame,
        info: showInfo,
        rules: showRules,
        cheatShowAll: sendShowAll,

        // Testing error handling
        getBadGame: testGetBadGame,
        getBadBoard: testGetBadBoard,
        doBadCheat: testDoBadCheat,
        doBadMove: testDoBadMove,
        clickCell: sendClick,

        locationMatches: function(loc, x, y) {
            return loc.x === x && loc.y === y;
        },
    }
});

// Have Axios send body as plain text (not JSON) when this config is passed to a POST request.
var plainTextConfig = {
    headers: { 'Content-Type': 'text/plain'},
    responseType: 'text'
};

// Refresh UI at start
$(document).ready(function() {
    loadAbout();
});

function loadAbout() {
    axios.get('/api/about', {})
        .then(function (response) {
            console.log("GET About returned:", response);
            myAppObj.authorName = response.data;

            alertOnWrongStatus("GET about", 200, response.status);
        })
        .catch(function (error) {
            console.log("GET About ERROR:", error);
        });
}

function showInfo() {
    alert("在这个回合制的炮击小游戏里，你是一艘被敌军包围的战列舰，\n\n" +
        "为了生存，你需要在敌方击沉你之前找到并击毁所有敌军！\n\n" +
        "我们得到情报，在这片 10x10 的海域里一共会有五艘敌方战舰出没。\n\n" +
        "它们以五格一艘的规模共占领了二十五个区域，且每一艘敌舰所占据的区域都是相连的。"
    );
}

function showRules() {
    alert("每回合你可以以鼠标点击方式开炮轰击一个区域，每当出现红色标志代表你命中敌舰，\n\n" +
        "而若是出现烟雾的话，根据“有烟无伤”定律，这一炮将会和敌方擦肩而过。\n\n" +
        "紧接着，存活敌舰会趁着你填装的空窗期向你开炮，并且它们未被击毁的船体越多，它们将会对你发射更多炮弹：\n\n" +
        "5格：-20生命值，4格：-10生命值，三格：-5生命值，两格：-2生命值，一格：-1生命值"
    );
}

function makeNewGame() {
    axios.post('/api/games', {})
        .then(function (response) {
            console.log("POST new game returned:", response);
            myAppObj.game = response.data;
            loadGameBoard();

            alertOnWrongStatus("POST games", 201, response.status);
        })
        .catch(function (error) {
            console.log("POST new game ERROR:", error);
        });
}

function loadGame() {
    axios.get('/api/games/' + myAppObj.game.gameNumber, {})
      .then(function (response) {
        console.log("Load game returned:", response);
        myAppObj.game = response.data;

        alertOnWrongStatus("GET Game", 200, response.status);
      })
      .catch(function (error) {
        console.log("Load game ERROR: ", error);
      });
}
function loadGameBoard() {
    axios.get('/api/games/' + myAppObj.game.gameNumber + "/board", {})
      .then(function (response) {
        console.log("Load Board returned: ", response);
        myAppObj.board = response.data;

        alertOnWrongStatus("GET board", 200, response.status);
      })
      .catch(function (error) {
        console.log("Load Board ERROR: ", error);
      });
}

function sendShowAll() {
    axios.post('/api/games/' + myAppObj.game.gameNumber + "/cheatstate", "SHOW_ALL", plainTextConfig)
        .then(function (response) {
            console.log("Cheat returned: ", response);
            loadGameBoard();
            alertOnWrongStatus("POST Show All Cheat", 202, response.status);
        })
        .catch(function (error) {
            console.log("Cheat ERROR: ", error);
        });
}

// Move
function sendClick(rowIdx, colIdx) {
    console.log("Clicked on (" + rowIdx + ", " + colIdx + ")");

    if (myAppObj.game.isGameLost || myAppObj.game.isGameWon) {
        console.log("Unable to make move after game has ended.");
        return;
    }

    let body = {row: rowIdx, col: colIdx}

    axios.post('/api/games/' + myAppObj.game.gameNumber + "/moves", body)
        .then(function (response) {
            console.log("POST /moves:", response);
            loadGameBoard();
            loadGame();
        })
        .catch(function (error) {
            // Did they bump the wall?
            if (error.response.status === 400) {
                console.log(error.response);
                console.log("Move player: hit the wall.");
                playSound();
            } else {
                console.log("Move player ERROR:", error);
            }
        });
}

// Testing Functions
// ----------------------------------------------------------------------------------------
function testGetBadGame() {
    testErrorHandling(
        "Test Get Bad Game",
        "GET",
        '/api/games/' + 2352523,
        "",
        404);
}
function testGetBadBoard() {
    testErrorHandling(
        "Test Get Bad Board",
        "GET",
        '/api/games/' + 2352523 + "/board",
        "",
        404);
}
function testDoBadCheat() {
    testErrorHandling(
        "Test Cheat on bad game",
        "POST",
        '/api/games/' + 2352523 + "/cheatstate",
        "1_CHEESE",
        404);

    testErrorHandling(
        "Test Bad cheat command",
        "POST",
        '/api/games/' + myAppObj.game.gameNumber + "/cheatstate",
        "NoSuchCheat",
        400);
}
function testDoBadMove() {
    testErrorHandlingJsonPost(
        "Test Move on bad game",
        '/api/games/' + 235235 + "/moves",
        3,3,   // row/col
        404);

    testErrorHandlingJsonPost(
        "Test Bad Move (row < 0)",
        '/api/games/' + myAppObj.game.gameNumber + "/moves",
        -1,3,   // row/col
        400);
    testErrorHandlingJsonPost(
        "Test Bad Move (col >9)",
        '/api/games/' + myAppObj.game.gameNumber + "/moves",
        3,10,   // row/col
        400);
}
function testErrorHandling(name, method, url, data, result) {
    axios( {
        method: method,
        url: url,
        data: data,
        config: plainTextConfig
    })
        .then(function () {
            alert(name + ": Did *not* fail when it should have! (expected " + result + ")");
        })
        .catch(function (error) {
            if (error.response.status !== result) {
                console.log(name + ": Returned incorrect error response code (expected " + result + "): ", error.response)
                alert(name + ": Returned incorrect error response code (expected " + result + ")")
            } else {
                console.log(name + ": returned the correct response code: ", error)
            }
        });
}
function testErrorHandlingJsonPost(name, url, rowIdx, colIdx, result) {
    let body = {row: rowIdx, col: colIdx}
    axios.post(url, body)
        .then(function () {
            alert(name + ": Did *not* fail when it should have! (expected " + result + ")");
        })
        .catch(function (error) {
            if (error.response.status !== result) {
                console.log(name + ": Returned incorrect error response code (expected " + result + "): ", error.response)
                alert(name + ": Returned incorrect error response code (expected " + result + ")")
            } else {
                console.log(name + ": returned the correct response code: ", error)
            }
        });
}

function alertOnWrongStatus(description, expectedStatus, actualStatus) {
    if (actualStatus !== expectedStatus) {
        alert("ERROR: Incorrect HTTP status returned for ["
            + description
            + "]; expected " + expectedStatus
            + " but server returned " + actualStatus)
    }
}

showInfo();
showRules();