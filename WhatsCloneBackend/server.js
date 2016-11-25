var socket = require('socket.io');
var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = socket.listen(server);
var port = process.env.PORT || 9000;
var mainPort = 80;
var os = require("os");

server.listen(port, function () {
    console.log('Server listening at port %d', port);
    server.on('error', function (e) {
        console.log("Got error server :  " + e.message);
    });
});

var users = []; //array of current users connected

io.on('connection', function (socket) {

    /******************************************** Method for groups  ********************************************************************************
     *
     * **********************************************************************************************************************************************
     */

    /**
     * method to check if  member of group  is start typing
     */
    socket.on('member_typing', function (data) {
        io.sockets.emit('member_typing', {
            recipientId: data.recipientId,
            groupId: data.groupId,
            senderId: data.senderId
        });
    });

    /**
     * method to check if a member of group  is stop typing
     */
    socket.on('member_stop_typing', function (data) {
        io.sockets.emit('member_stop_typing', {
            recipientId: data.recipientId,
            groupId: data.groupId,
            senderId: data.senderId
        });
    });
    /**
     * method to check if u receive a new message
     */
    socket.on('new_group_message', function (data) {
        io.sockets.emit('new_group_message', {
            recipientId: data.recipientId,
            messageId: data.messageId,
            messageBody: data.messageBody,
            senderId: data.senderId,
            phone: data.phone,
            senderName: data.senderName,
            GroupImage: data.GroupImage,
            GroupName: data.GroupName,
            groupID: data.groupID,
            date: data.date,
            isGroup: data.isGroup,
            image: data.image,
            video: data.video,
            audio: data.audio,
            document: data.document,
            thumbnail: data.thumbnail
        });

    });

    /**
     * mehtod to save firstly the message in the database
     */
    socket.on('save_group_message', function (data, callback) {
        var http = require('http');
        var queryString = require("querystring");
        var qs = queryString.stringify(data);
        var qslength = qs.length;
        var path = require('path').basename(__dirname);
        var hostname = os.hostname();
        var options = {
            hostname: hostname,
            port: mainPort,
            path: '/' + path + '/Groups/saveMessage',
            method: 'POST',
            type: "json",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Content-Length': qslength
            }
        };

        var buffer = "";
        var req = http.request(options, function (res) {
            res.on('data', function (chunk) {
                buffer += chunk;
            });
            res.on('end', function () {

                var messageData = {
                    messageId: buffer
                };
                console.log(messageData);
                callback(messageData);
                io.sockets.emit('group_sent', {
                    groupId: data.groupID,
                    senderId: data.senderId
                });
            });

            res.on('error', function (e) {
                console.log("Got error: " + e.message);
            });
        });

        req.write(qs);
        req.end();
    });

    /**
     * method to ping and check if member of group is connected
     */
    socket.on('user_ping_group', function (data) {

        var pingedData;

        pingedData = {
            recipientId: data.recipientId,
            messageId: data.messageId,
            messageBody: data.messageBody,
            senderId: data.senderId,
            phone: data.phone,
            senderName: data.senderName,
            GroupImage: data.GroupImage,
            GroupName: data.GroupName,
            groupID: data.groupID,
            date: data.date,
            isGroup: data.isGroup,
            image: data.image,
            video: data.video,
            audio: data.audio,
            document: data.document,
            thumbnail: data.thumbnail,
            pinged: data.pinged,
            pingedId: data.pingedId
        };
        io.sockets.emit('user_pinged_group', pingedData);
    });

    /**
     * method to send message group
     */
    socket.on('send_group_message', function (dataString) {
        console.log(dataString);
        saveMessageGroupToDataBase(dataString);
        return;
    });

    /**
     * method to save message as waiting
     * @param data
     */
    function saveMessageGroupToDataBase(data) {

        var http = require('http');
        var queryString = require("querystring");
        var qs = queryString.stringify(data);
        var qslength = qs.length;
        var path = require('path').basename(__dirname);
        var hostname = os.hostname();
        var options = {
            hostname: hostname,
            port: mainPort,
            path: '/' + path + '/Groups/send',
            method: 'POST',
            type: "json",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Content-Length': qslength
            }
        };

        var buffer = "";
        var req = http.request(options, function (res) {
            res.on('data', function (chunk) {
                buffer += chunk;
            });
            res.on('end', function () {
                io.sockets.emit('group_delivered', {
                    groupId: data.groupID,
                    senderId: data.senderId

                });
            });

            res.on('error', function (e) {
                console.log("Got error: " + e.message);
            });
        });

        req.write(qs);
        req.end();


    }

    /**
     * method to check if there is messages to sent
     * @param data
     */
    function CheckForUnsentMessages(data) {

        var http = require('http');
        var queryString = require("querystring");
        var qs = queryString.stringify(data);
        var qslength = qs.length;
        var path = require('path').basename(__dirname);
        var hostname = os.hostname();
        var options = {
            hostname: hostname,
            port: mainPort,
            path: '/' + path + '/Groups/checkUnsentMessageGroup',
            method: 'POST',
            type: "json",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Content-Length': qslength
            }
        };

        var body = "";
        var req = http.request(options, function (res) {
            res.on('data', function (chunk) {
                body += chunk;
            });
            res.on('end', function () {
                var obj = JSON.parse(body);
                for (var i = 0; i < obj.length; i++) {
                    var pingedData = {
                        recipientId: obj[i].recipientId,
                        messageId: obj[i].messageId,
                        messageBody: obj[i].messageBody,
                        senderId: obj[i].senderId,
                        phone: obj[i].phone,
                        senderName: obj[i].senderName,
                        GroupImage: obj[i].GroupImage,
                        GroupName: obj[i].GroupName,
                        groupID: obj[i].groupId,
                        date: obj[i].date,
                        isGroup: obj[i].isGroup,
                        image: obj[i].image,
                        video: obj[i].video,
                        audio: obj[i].audio,
                        document: obj[i].document,
                        thumbnail: obj[i].thumbnail,
                        pinged: obj[i].pinged,
                        pingedId: obj[i].pingedId
                    };
                    io.sockets.emit('user_pinged_group', pingedData);
                }
            });
            res.on('error', function (e) {
                console.log("Got error: " + e.message);
            });
        });

        req.write(qs);
        req.end();


    }


    /******************************************** Method for a single user ********************************************************************************
     *
     * ****************************************************************************************************************************************************
     */


    /**
     * method to check if user is connected or not
     */

    socket.on('user_connect', function (data) {
        console.log("user with id " + data.connectedId);
        console.log("user with boolean " + data.connected);
        var user = {
            id: data.connectedId,
            socketID: socket.id
        };
        users.push(user);
        io.sockets.emit('user_connect', {
            connectedId: data.connectedId,
            connected: data.connected,
            socketId: socket.id
        });
        CheckForUnsentMessages(data);//this just for groups
    });


    /**
     * method to get response from recipient to update status (from waiting to sent )
     */
    socket.on('send_message', function (dataString) {
        var messageID = {
            messageId: dataString.messageId,
            senderId: dataString.senderId
        };
        io.sockets.emit('send_message', {
            messageId: messageID.messageId,
            senderId: messageID.senderId
        });
    });

    /**
     * method to check if user disconnected  before send a message to him (do a ping and get a callback)
     */
    socket.on('user_ping', function (data, callback) {
        var pingingData = {
            pinged: data.pinged,
            pingedId: data.pingedId,
            socketId: data.socketId
        };

        var pingedData;

        if (pingingData.pingedId = data.recipientId && pingingData.pinged == true) {
            pingedData = {
                messageId: data.messageId,
                senderImage: data.senderImage,
                pingedId: data.recipientId,
                pinged: pingingData.pinged,
                senderId: data.senderId,
                recipientId: data.recipientId,
                senderName: data.senderName,
                messageBody: data.messageBody,
                date: data.date,
                isGroup: data.isGroup,
                conversationId: data.conversationId,
                image: data.image,
                video: data.video,
                audio: data.audio,
                document: data.document,
                thumbnail: data.thumbnail,
                phone: data.phone
            };
        } else {
            pingedData = {
                messageId: data.messageId,
                senderImage: data.senderImage,
                pingedId: data.senderId,
                pinged: pingingData.pinged,
                senderId: data.senderId,
                recipientId: data.recipientId,
                senderName: data.senderName,
                messageBody: data.messageBody,
                date: data.date,
                isGroup: data.isGroup,
                conversationId: data.conversationId,
                image: data.image,
                video: data.video,
                audio: data.audio,
                document: data.document,
                thumbnail: data.thumbnail,
                phone: data.phone
            };
        }
        callback(pingedData);
       //return;
    });
    /**
     * method to check if u receive a new message
     */
    socket.on('new_message', function (data) {
        io.sockets.emit('new_message', {
            messageId: data.messageId,
            senderImage: data.senderImage,
            senderId: data.senderId,
            recipientId: data.recipientId,
            senderName: data.senderName,
            messageBody: data.messageBody,
            date: data.date,
            isGroup: data.isGroup,
            conversationId: data.conversationId,
            image: data.image,
            video: data.video,
            audio: data.audio,
            document: data.document,
            thumbnail: data.thumbnail,
            phone: data.phone
        });
    });

    /**
     * method to save new message to database
     */
    socket.on('save_new_message', function (data) {
        saveMessageToDataBase(data);
    });
    /**
     * method to save message of user
     * @param data
     */
    function saveMessageToDataBase(data) {
        var http = require('http');
        var queryString = require("querystring");
        var qs = queryString.stringify(data);
        var qslength = qs.length;
        var path = require('path').basename(__dirname);
        var hostname = os.hostname();
        var options = {
            hostname: hostname,
            port: mainPort,
            path: '/' + path + '/Messages/send',
            method: 'POST',
            type: "json",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Content-Length': qslength
            }
        };

        var buffer = "";
        var req = http.request(options, function (res) {
            res.on('data', function (chunk) {
                buffer += chunk;
            });

            res.on('error', function (e) {
                console.log("Got error: " + e.message);
            });
        });

        req.write(qs);
        req.end();

    }


    /**
     * method to check if user is start typing
     */
    socket.on('typing', function (data) {
        io.sockets.emit('typing', {
            recipientId: data.recipientId,
            senderId: data.senderId
        });
    });

    /**
     * method to check if user is stop typing
     */
    socket.on('stop_typing', function (data) {
        io.sockets.emit('stop_typing', {
            recipientId: data.recipientId,
            senderId: data.senderId
        });
    });

    /**
     * method to check status last seen
     */
    socket.on('last_seen', function (data) {
        io.sockets.emit('last_seen', {
            lastSeen: data.lastSeen,
            senderId: data.senderId,
            recipientId: data.recipientId
        });
    });


    /**
     * method to check if user is read (seen) a specific message
     */
    socket.on('seen', function (data) {
        io.sockets.emit('seen', {
            senderId: data.senderId,
            recipientId: data.recipientId
        });
    });

    /**
     * method to check if a message is delivered to the recipient
     */
    socket.on('delivered', function (data) {
        io.sockets.emit('delivered', {
            messageId: data.messageId,
            senderId: data.senderId
        });
    });

    /**
     * method to check if recipient is Online
     */
    socket.on('is_online', function (data) {
        io.sockets.emit('is_online', {
            senderId: data.senderId,
            connected: data.connected
        });
    });

    /**
     * method if a user is disconnect from sockets
     * and then remove him from array of current users connected
     */
    socket.on('disconnect', function () {
        for (var i = 0; i < users.length; ++i) {
            var user = users[i];

            console.log("this user is disconnect" + user.id);
            io.sockets.emit('user_connect', {
                connectedId: user.id,
                connected: false,
                socketId: socket.id
            });
            if (user.socketID == socket.id) {
                users.splice(i, 1);
                break;
            }
        }
    });
});
