<?php

/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 27/02/2016
 * Time: 22:07
 */
class MessagesController
{


    public $_GB;
    public $Users;

    public function __construct($_GB, $Users)
    {
        $this->_GB = $_GB;
        $this->_Users = $Users;
    }



    /**
     * Function to send a new message
     * @param $array
     */
    public function sendMessage($array)
    {

        foreach ($array as $key => $value) {
            $array[$key] = $this->_GB->_DB->escapeString(trim($value));
        }
        $userId = $array["senderId"];
        if ($userId != $array['recipientId']) {
            if ($array['conversationId'] == 0) {
                $query = "SELECT id
                      FROM prefix_conversations
                      WHERE (sender={$userId} 
                      AND recipient={$array['recipientId']} )
                       OR (sender={$array['recipientId']} 
                        AND recipient={$userId})";
                $query = $this->_GB->_DB->MySQL_Query($query);
                if ($this->_GB->_DB->numRows($query) != 0) {
                    $fetch = $this->_GB->_DB->fetchAssoc($query);
                    $array['conversationId'] = $fetch['id'];
                } else {
                    $data = array(
                        'sender' => $userId,
                        'recipient' => $array['recipientId'],
                        'Date' => $array['date']);

                    $insert = $this->_GB->_DB->insert('conversations', $data);
                    if ($insert) {
                        $array['conversationId'] = $this->_GB->_DB->last_Id();
                    }
                }
                $arrayData = array(
                    'userID' => $userId,
                    'message' => $array['messageBody'],
                    'image' => $array['image'],
                    'video' => $array['video'],
                    'audio' => $array['audio'],
                    'thumbnail' => $array['thumbnail'],
                    'document' => $array['document'],
                    'Date' => $array['date'],
                    'ConversationID' => $array['conversationId']
                );

                $insert = $this->_GB->_DB->insert('messages', $arrayData);
                if ($insert) {
                    $arrayMessageData = array('success' => true,
                        'message' => 'saved successfully',
                        'messageId' => $this->_GB->_DB->last_Id());
                    $this->_GB->Json($arrayMessageData);
                } else {
                    $arrayMessageData = array('success' => false,
                        'message' => 'save failed',
                        'messageId' => 0);
                    $this->_GB->Json($arrayMessageData);
                }
            } else {
                $query = "SELECT id
                      FROM prefix_conversations
                      WHERE (sender={$userId} 
                      AND recipient={$array['recipientId']} )
                       OR (sender={$array['recipientId']} 
                        AND recipient={$userId})";
                $query = $this->_GB->_DB->MySQL_Query($query);
                if ($this->_GB->_DB->numRows($query) != 0) {
                    $fetch = $this->_GB->_DB->fetchAssoc($query);
                    $array['conversationId'] = $fetch['id'];
                } else {
                    $data = array(
                        'sender' => $userId,
                        'recipient' => $array['recipientId'],
                        'Date' => $array['date']);

                    $insert = $this->_GB->_DB->insert('conversations', $data);
                    if ($insert) {
                        $array['conversationId'] = $this->_GB->_DB->last_Id();
                    }
                }


                $arrayData = array(
                    'userID' => $userId,
                    'message' => $array['messageBody'],
                    'image' => $array['image'],
                    'video' => $array['video'],
                    'audio' => $array['audio'],
                    'thumbnail' => $array['thumbnail'],
                    'document' => $array['document'],
                    'Date' => $array['date'],
                    'ConversationID' => $array['conversationId']
                );

                $insert = $this->_GB->_DB->insert('messages', $arrayData);
                if ($insert) {
                    $arrayMessageData = array('success' => true,
                        'message' => 'saved successfully',
                        'messageId' => $this->_GB->_DB->last_Id());
                    $this->_GB->Json($arrayMessageData);
                } else {
                    $arrayMessageData = array('success' => false,
                        'message' => 'save failed',
                        'messageId' => 0);
                    $this->_GB->Json($arrayMessageData);
                }
            }

        }
    }
    
    /******************************************** Groups methods ********************************************
     *
     ******************************************* Groups methods *********************************************/

    /**
     *  save the new group messages
     * @param $array
     */
    public function saveMessageGroup($array)
    {

        foreach ($array as $key => $value) {
            $array[$key] = $this->_GB->_DB->escapeString(trim($value));
        }
        $groupID = $array['groupID'];


        $arrayData = array(
            'groupID' => $groupID,
            'message' => $array['messageBody'],
            'image' => $array['image'],
            'video' => $array['video'],
            'audio' => $array['audio'],
            'thumbnail' => $array['thumbnail'],
            'document' => $array['document'],
            'Date' => $array['date']
        );
        $insert = $this->_GB->_DB->insert('messages', $arrayData);
        $messageId = $this->_GB->_DB->last_Id();
        if ($insert) {
            $this->_GB->Json($messageId);
        } else {
            $this->_GB->Json(0);
        }
    }

    /**
     * check if unsent group messages exist
     * @param $recipientId
     * @return bool
     */
    public function existUnsentMessageGroup($recipientId)
    {
        $status = 0;
        $query = $this->_GB->_DB->select('messages_groups_status', 'id', " `status`= '{$status}'  AND `recipientId`= '{$recipientId}'");

        if ($this->_GB->_DB->numRows($query) > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * get single group message
     * @param $messageId
     * @param $groupId
     * @return null
     */
    public function getGroupMessage($messageId, $groupId)
    {
        $query = "SELECT *
                  FROM prefix_messages M
                  WHERE  M.id = {$messageId} AND  M.groupID = {$groupId}
                  ORDER BY M.Date DESC LIMIT 1";
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['message'];
        } else {
            return null;
        }
    }

    /**
     * get video of group message
     * @param $messageId
     * @param $groupId
     * @return null
     */
    public function getGroupMessageVideo($messageId, $groupId)
    {
        $query = "SELECT *
                  FROM prefix_messages M
                  WHERE  M.id = {$messageId} AND  M.groupID = {$groupId}
                  ORDER BY M.Date DESC LIMIT 1";
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['video'];
        } else {
            return null;
        }
    }

    /**
     * get Thumbnail of group message
     * @param $messageId
     * @param $groupId
     * @return null
     */
    public function getGroupMessageVideoThumbnail($messageId, $groupId)
    {
        $query = "SELECT *
                  FROM prefix_messages M
                  WHERE  M.id = {$messageId} AND  M.groupID = {$groupId}
                  ORDER BY M.Date DESC LIMIT 1";
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['thumbnail'];
        } else {
            return null;
        }
    }

    /**
     * get image of group message
     * @param $messageId
     * @param $groupId
     * @return null
     */
    public function getGroupMessageImage($messageId, $groupId)
    {
        $query = "SELECT *
                  FROM prefix_messages M
                  WHERE  M.id = {$messageId} AND  M.groupID = {$groupId}
                  ORDER BY M.Date DESC LIMIT 1";
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['image'];
        } else {
            return null;
        }
    }

    /**
     * get audio of group message
     * @param $messageId
     * @param $groupId
     * @return null
     */
    public function getGroupMessageAudio($messageId, $groupId)
    {
        $query = "SELECT *
                  FROM prefix_messages M
                  WHERE  M.id = {$messageId} AND  M.groupID = {$groupId}
                  ORDER BY M.Date DESC LIMIT 1";
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['audio'];
        } else {
            return null;
        }
    }

    /**
     * get document of group message
     * @param $messageId
     * @param $groupId
     * @return null
     */
    public function getGroupMessageDocument($messageId, $groupId)
    {
        $query = "SELECT *
                  FROM prefix_messages M
                  WHERE  M.id = {$messageId} AND  M.groupID = {$groupId}
                  ORDER BY M.Date DESC LIMIT 1";
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['document'];
        } else {
            return null;
        }
    }

    /**
     * get date of group message
     * @param $messageId
     * @param $groupId
     * @return null
     */
    public function getGroupMessageDate($messageId, $groupId)
    {
        $query = " SELECT *
                  FROM prefix_messages M
                  WHERE  M.id = {$messageId} AND  M.groupID = {$groupId}
                  ORDER BY M.Date DESC LIMIT 1";
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['Date'];
        } else {
            return null;
        }
    }

    /**
     * get sender name
     * @param $senderId
     * @return null
     */
    public function getGroupSenderName($senderId)
    {

        $query = $this->_GB->_DB->select('users', 'username', " `id`= '{$senderId}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['username'];
        } else {
            return null;
        }
    }

    /**
     * get sender phone
     * @param $senderId
     * @return null
     */
    public function getGroupSenderPhone($senderId)
    {
        $query = $this->_GB->_DB->select('users', 'phone', " `id`= '{$senderId}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['phone'];
        } else {
            return null;
        }
    }

    /**
     * get group name
     * @param $groupId
     * @return null
     */
    public function getGroupGroupName($groupId)
    {
        $query = $this->_GB->_DB->select('groups', 'name', " `id`= '{$groupId}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['name'];
        } else {
            return null;
        }
    }

    /**
     * get group image
     * @param $groupId
     * @return null
     */
    public function getGroupGroupImage($groupId)
    {

        $query = $this->_GB->_DB->select('groups', 'image', " `id`= '{$groupId}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['image'];
        } else {
            return null;
        }
    }

    /**
     * check for unsent group messages
     * @param $array
     */
    public function checkUnsentMessageGroup($array)
    {
        foreach ($array as $key => $value) {
            $array[$key] = $this->_GB->_DB->escapeString(trim($value));
        }
        $recipientId = $array['connectedId'];

        if ($this->existUnsentMessageGroup($recipientId)) {
            $status = 0;
            $query = $this->_GB->_DB->select('messages_groups_status', '*', " `status`= '{$status}'  AND `recipientId`= '{$recipientId}'");
            $messageGroup = array();
            while ($fetch = $this->_GB->_DB->fetchAssoc($query)) {
                $fetch['groupId'] = (empty($fetch['groupId'])) ? null : $fetch['groupId'];
                $fetch['recipientId'] = (empty($fetch['recipientId'])) ? null : $fetch['recipientId'];
                $fetch['senderId'] = (empty($fetch['senderId'])) ? null : $fetch['senderId'];
                $fetch['messageId'] = (empty($fetch['messageId'])) ? null : $fetch['messageId'];
                $fetch['messageBody'] = $this->getGroupMessage($fetch['messageId'], $fetch['groupId']);
                $fetch['video'] = $this->getGroupMessageVideo($fetch['messageId'], $fetch['groupId']);
                $fetch['thumbnail'] = $this->getGroupMessageVideoThumbnail($fetch['messageId'], $fetch['groupId']);
                $fetch['image'] = $this->getGroupMessageImage($fetch['messageId'], $fetch['groupId']);
                $fetch['audio'] = $this->getGroupMessageAudio($fetch['messageId'], $fetch['groupId']);
                $fetch['document'] = $this->getGroupMessageDocument($fetch['messageId'], $fetch['groupId']);
                $fetch['phone'] = $this->getGroupSenderPhone($fetch['senderId']);
                $fetch['senderName'] = $this->getGroupSenderName($fetch['senderId']);
                $fetch['GroupName'] = $this->getGroupGroupName($fetch['groupId']);
                $fetch['GroupImage'] = $this->getGroupGroupImage($fetch['groupId']);
                $fetch['date'] = $this->getGroupMessageDate($fetch['messageId'], $fetch['groupId']);
                $fetch['isGroup'] = true;
                $fetch['pinged'] = true;
                $fetch['pingedId'] = $recipientId;
                $messageGroup [] = $fetch;
            }
            $this->_GB->Json($messageGroup);

        } else {
            $this->_GB->Json(array('null object ' => null));

        }

    }

    /**
     * check existing of a specific message
     * @param $groupID
     * @param $messageId
     * @param $senderId
     * @param $recipientId
     * @return bool
     */
    public function existMessage($groupID, $messageId, $senderId, $recipientId)
    {
        $query = $this->_GB->_DB->select('messages_groups_status', 'id', "`groupId`= '{$groupID}' AND `messageId`= '{$messageId}' AND `senderId`= '{$senderId}'  AND `recipientId`= '{$recipientId}'");
        if ($this->_GB->_DB->numRows($query) > 0) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Function to send a new message
     * @param $array
     */
    public function sendMessageGroup($array)
    {

        foreach ($array as $key => $value) {
            $array[$key] = $this->_GB->_DB->escapeString(trim($value));
        }
        $groupID = $array['groupID'];
        $messageId = $array['messageId'];
        $senderId = $array['senderId'];
        $recipientId = $array['recipientId'];
        $isSent = $array['isSent'];

        if ($this->existMessage($groupID, $messageId, $senderId, $recipientId) == true) {
            if ($isSent == 0) return;
            $fields = "`status` = '" . 1 . "'";
            $this->_GB->_DB->update('messages_groups_status', $fields, " `groupId`= '{$groupID}' AND `messageId`= '{$messageId}' AND `senderId`= '$senderId'  AND `recipientId`= '$recipientId'");
        } else {
            $arrayDataGroupMessages = array(
                'groupId' => $groupID,
                'messageId' => $messageId,
                'recipientId' => $recipientId,
                'senderId' => $senderId,
                'status' => $isSent
            );
            $this->_GB->_DB->insert('messages_groups_status', $arrayDataGroupMessages);
        }


    }


}
