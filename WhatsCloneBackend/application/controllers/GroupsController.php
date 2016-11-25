<?php

/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 05/03/2016
 * Time: 23:28
 */
class GroupsController
{


    public $_GB;

    public function __construct($_GB)
    {
        $this->_GB = $_GB;
    }

    /**
     * Function to get  conversations
     * @param $userId
     */
    public function getGroups($userId)
    {
        $query = " SELECT G.id , G.date AS CreatedDate,
                                  G.name AS GroupName ,
                                  G.image AS GroupImage
                          FROM prefix_users U,prefix_groups G,prefix_group_members GM
                          WHERE 
                          CASE
                          WHEN GM.userID = U.id
                          THEN GM.userID = {$userId} 
                          END
                          AND GM.groupID = G.id
                           AND U.is_activated = '1'
                           AND (GM.role = 'admin' OR GM.role = 'member')
                          GROUP BY G.id  ORDER BY G.id ASC ";
        $query = $this->_GB->_DB->MySQL_Query($query);

        if ($this->_GB->_DB->numRows($query) != 0) {
            $conversations = array();
            while ($fetch = $this->_GB->_DB->fetchAssoc($query)) {
                $fetch['id'] = (empty($fetch['id'])) ? null : $fetch['id'];
                $fetch['GroupImage'] = $this->_GB->getSafeImage($fetch['GroupImage']);
                $fetch['CreatorID'] = $this->getUserIDByGroupID($fetch['id']);
                $fetch['Creator'] = $this->getCreatorName($fetch['id']);
                $fetch['Members'] = $this->GetFirstGroupMembers($fetch['id']);
                $conversations[] = $fetch;
            }
            $this->_GB->Json($conversations);
        } else {
            $this->_GB->Json(array('groups' => null));
        }
    }



    public function GetFirstGroupMembers($groupID)
    {
        $query = " SELECT  GM.id ,GM.role,GM.groupID,GM.isLeft,U.id AS userId,U.username,U.phone,U.image,U.status,U.status_date,U.is_activated 

                             FROM prefix_users U,prefix_groups G,prefix_group_members GM
                             WHERE
                             CASE
                             WHEN GM.userID = U.id
                             THEN GM.groupID = G.id
                              END
                              AND
                              G.id = {$groupID}
                              AND
                              U.is_activated = 1
                             GROUP BY U.id   ORDER BY U.id ASC";
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $GroupMembers = array();
            while ($fetch = $this->_GB->_DB->fetchAssoc($query)) {
                $fetch['username'] = (empty($fetch['username'])) ? null : $fetch['username'];
                $fetch['Linked'] = true;
                $fetch['image'] = $this->_GB->getSafeImage($fetch['image']);
                $fetch['isLeft'] = $this->isLeft($fetch['groupID'], $fetch['userId']);
                $fetch['isAdmin'] = $this->isAdmin($fetch['groupID'], $fetch['userId']);
                $fetch['status_date'] = (empty($fetch['status_date'])) ? null : $this->_GB->Date($fetch['status_date']);
                unset ($fetch['created_at'], $fetch['apikey'], $fetch['is_activated']);
                $GroupMembers[] = $fetch;
            }
            return $GroupMembers;
        } else {
            return  $this->_GB->Json(array('groupsMembers' => null));
        }
    }



    public function isLeft($groupID, $userID)
    {
        $query = $this->_GB->_DB->select('group_members', 'isLeft', "  `groupID`= '{$groupID}' AND `userID`= '{$userID}' AND `isLeft`= '1' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            return true;
        } else {
            return false;
        }

    }

    public function isAdmin($groupID, $userID)
    {
        $query = $this->_GB->_DB->select('group_members', 'userID', "  `groupID`= '{$groupID}' AND `userID`= '{$userID}' AND `role`= 'admin' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            return true;
        } else {
            return false;
        }

    }


    public function getUserIDByGroupID($groupID)
    {
        $query = $this->_GB->_DB->select('groups', 'userID', "`id`= '{$groupID}'");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['userID'];
        } else {
            return 0;
        }
    }

    public function getCreatorName($groupID)
    {
        $id = $this->getUserIDByGroupID($groupID);
        $query = $this->_GB->_DB->select('users', '*', "`id`= '{$id}'");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            if ($fetch['username'] != null)
                return $fetch['username'];
            else
                return $fetch['phone'];
        } else {
            return null;
        }
    }



    /**
     * Function to get list messages
     * @param $groupID
     * @param $limit
     */
    public function getGroupMessages($groupID, $limit)
    {

        $groupID = (int)$groupID;
        $status = 1;
        if ($groupID != 0) {
            $this->_GB->_DB->update('messages', "`status` = {$status} ", "`groupID`={$groupID} ");
            $query = "SELECT M.id,
                            M.Date AS date,
                            M.message ,
                            M.image AS imageFile,
                            M.video AS videoFile,
                            M.status ,
                            M.groupID,
                            U.id AS recipientID,
                            U.username AS username,
                            U.phone AS phone,
                            G.id AS isGroup
                  FROM prefix_messages M



                  LEFT JOIN prefix_users AS U
                  ON U.id = M.UserID

                  LEFT JOIN prefix_groups G
                  ON G.id = M.groupID



                  WHERE G.id = M.groupID
                  AND  M.groupID = {$groupID}
                   AND G.id = {$groupID}
                  ORDER BY M.Date ASC LIMIT {$limit}";
            $query = $this->_GB->_DB->MySQL_Query($query);
            $messages = array();
            if ($this->_GB->_DB->numRows($query)) {
                while ($fetch = $this->_GB->_DB->fetchAssoc($query)) {
                    $fetch['date'] = $this->_GB->Date($fetch['date']);
                    $fetch['imageFile'] = $this->_GB->getSafeImage($fetch['imageFile']);
                    $fetch['isGroup'] = ($fetch['isGroup'] != 0) ? true : false;
                    //  $fetch['videoFile'] = $this->_GB->getSafeImage($fetch['videoFile']);
                    $messages[] = $fetch;
                }
                $this->_GB->Json($messages);
            } else {
                $this->_GB->Json(array('messages' => null));
            }
        }

    }


    public function createGroup($name, $image, $userId, $ids, $date)
    {
        if (strpos($name, '\'') !== false) {
            $name = str_replace('\'', "\\'", $name);
        }
        $createGroup = array(
            'name' => $name,
            'image' => $image,
            'userID' => $userId,
            'date' => $date
        );
        $insert = $this->_GB->_DB->insert('groups', $createGroup);
        $groupID = $this->_GB->_DB->last_Id();
        if ($insert) {
            $array = split(",", $ids);
            $in = true;
            foreach ($array as $id) {
                if ($userId == $id) {
                    $addMembersToGroup = array(
                        'groupID' => $groupID,
                        'userID' => $userId,
                        'role' => "admin"

                    );
                } else {
                    $addMembersToGroup = array(
                        'groupID' => $groupID,
                        'userID' => $id,
                        'role' => "member"

                    );
                }

                $insertMembers = $this->_GB->_DB->insert('group_members', $addMembersToGroup);
                if (!$insertMembers) {
                    $in = false;
                    break;
                }
            }
            if ($in) {
                $this->_GB->Json(array(
                    'success' => true,
                    'message' => 'group has been created successfully',
                    'groupID' => $groupID,
                    'groupImage' => $this->_GB->getSafeImage($image)));
            } else {
                $this->_GB->Json(array('success' => false,
                    'message' => 'Oops ? something went wrong',
                    'groupID' => null,
                    'groupImage' => null));
            }
        } else {
            $this->_GB->Json(array('success' => false,
                'message' => 'Failed to create this group',
                'groupID' => null,
                'groupImage' => null));
        }
    }

    public function UserExist($userID, $groupID)
    {
        $query = $this->_GB->_DB->select('group_members', '`userID`', "`userID` = '{$userID}' AND `groupID` = '{$groupID}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public function addMembersToGroup($groupID, $id)
    {
        if ($this->UserExist($id, $groupID)) {
            $this->_GB->Json(array('success' => false,
                'message' => 'This member is already exist',
                'groupID' => null,
                'groupImage' => null));
        } else {
            $addMembersToGroup = array(
                'groupID' => $groupID,
                'userID' => $id,
                'role' => "member"

            );
            $insert = $this->_GB->_DB->insert('group_members', $addMembersToGroup);
            if (!$insert) {
                // $in = false;
                //break;
                $this->_GB->Json(array('success' => false,
                    'message' => 'Failed Something went wrong',
                    'groupID' => null,
                    'groupImage' => null));
            } else {
                $this->_GB->Json(array('success' => true,
                    'message' => 'Member(s) has  been added successfully',
                    'groupID' => $groupID,
                    'groupImage' => null));

            }
        }
    }


    public function removeMemberFromGroup($groupID, $id)
    {

        $delete = $this->_GB->_DB->delete('group_members', "`groupID`= '{$groupID}' AND `userID`= '{$id}'");
        if ($delete) {
            $this->_GB->Json(array('success' => true,
                'message' => 'This member is deleted successfully',
                'groupID' => $groupID,
                'groupImage' => null));
        } else {
            $this->_GB->Json(array('success' => false,
                'message' => 'Failed to delete this member please try again later',
                'groupID' => null,
                'groupImage' => null));
        }

    }

    public function makeMemberAdmin($groupID, $id)
    {
        $role = "admin";
        $fields = "`role` = '" . $role . "'";
        $update = $this->_GB->_DB->update('group_members', $fields, " `groupID` = '{$groupID}' AND `userID` = {$id}");
        if (!$update) {
            $this->_GB->Json(array('success' => false,
                'message' => 'Failed Something went wrong',
                'groupID' => null,
                'groupImage' => null));
        } else {
            $this->_GB->Json(array('success' => true,
                'message' => 'Member(s) has  been making as admin successfully',
                'groupID' => $groupID,
                'groupImage' => null));

        }

    }

    public function getGroupInfo($groupID)
    {
        $query = $this->_GB->_DB->select('groups', '*', "`id` = '{$groupID}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);

            $fetch['id'] = (empty($fetch['id'])) ? null : $fetch['id'];
            $fetch['GroupName'] = (empty($fetch['image'])) ? null : $fetch['name'];
            $fetch['GroupImage'] = (empty($fetch['image'])) ? null : $this->_GB->getSafeImage($fetch['image']);
            $fetch['CreatorID'] = $this->getUserIDByGroupID($fetch['id']);
            $fetch['Creator'] = $this->getCreatorName($fetch['id']);
            $fetch['Members'] = $this->GetFirstGroupMembers($fetch['id']);
            unset ($fetch['date'], $fetch['userID'], $fetch['image'], $fetch['name']);
            $this->_GB->Json($fetch);

        } else {
            $this->_GB->Json(null);
        }
    }

    public function GetGroupMembers($query)
    {
        if ($this->_GB->_DB->numRows($query) != 0) {
            $GroupMembers = array();
            while ($fetch = $this->_GB->_DB->fetchAssoc($query)) {
                $fetch['username'] = (empty($fetch['username'])) ? null : $fetch['username'];
                $fetch['Linked'] = true;
                $fetch['image'] = $this->_GB->getSafeImage($fetch['image']);
                $fetch['isLeft'] = $this->isLeft($fetch['groupID'], $fetch['userId']);
                $fetch['isAdmin'] = $this->isAdmin($fetch['groupID'], $fetch['userId']);
                $fetch['status_date'] = (empty($fetch['status_date'])) ? null : $this->_GB->Date($fetch['status_date']);
                unset ($fetch['created_at'], $fetch['apikey']);
                $GroupMembers[] = $fetch;
            }
            $this->_GB->Json($GroupMembers);
        } else {
            $this->_GB->Json(null);
        }
    }

    public function exitGroup($userID, $groupID)
    {

        $query = " SELECT U.*,GM.role,GM.groupID
                             FROM prefix_users U,prefix_groups G,prefix_group_members GM
                             WHERE
                             CASE
                             WHEN GM.userID = U.id
                             THEN GM.groupID = G.id
                              END
                              AND
                              G.id = {$groupID}
                              AND
                              U.is_activated = 1
                             GROUP BY U.id   ORDER BY U.id ASC";
        $query = $this->_GB->_DB->MySQL_Query($query);
        $GroupMembers = array();
        while ($fetch = $this->_GB->_DB->fetchAssoc($query)) {
            $GroupMembers[] = $fetch;
        }
        $generatedID = $this->generateRandomID($GroupMembers, $userID);

        if ($generatedID != 0) {
            $role1 = "member";
            $fields = "`isLeft` = '" . 1 . "'";
            $fields .= ",`role` = '" . $role1 . "'";
            $this->_GB->_DB->update('group_members', $fields, " `groupID` = '{$groupID}' AND `userID` = {$userID}");
            $role = "admin";
            $fields = "`role` = '" . $role . "'";
            $this->_GB->_DB->update('group_members', $fields, " `groupID` = '{$groupID}' AND `userID` = {$generatedID}");
            $createMessage = array(
                'message' => 'LT',
                'ConversationID' => 0,
                'UserID' => $userID,
                'groupID' => $groupID,
                'status' => 0,
                'Date' => time()
            );
            $this->_GB->_DB->insert('messages', $createMessage);
            $array = array(
                'success' => true,
                'message' => 'You left  this group'
            );
            $this->_GB->Json($array);

        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to left this group try again later'
            );
            $this->_GB->Json($array);
        }

    }


    public function EditGroupName($name, $groupID)
    {
        if (strpos($name, '\'') !== false) {
            $name = str_replace('\'', "\\'", $name);
        }
        $fields = "`name` = '" . $name . "'";
        $result = $this->_GB->_DB->update('groups', $fields, "`id` = '{$groupID}' ");

        // check if row inserted or not
        if ($result) {
            $array = array(
                'success' => true,
                'message' => 'group Name  is updated successfully '
            );
            $this->_GB->Json($array);
        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to update the group name '
            );
            $this->_GB->Json($array);
        }
    }

    public function getUserNameByID($userID)
    {
        $query = $this->_GB->_DB->select('users', '*', "`id`= '{$userID} '");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            if ($fetch['username'] != null) {
                return $fetch['username'];
            } else {
                return $fetch['phone'];
            }

        } else {
            return null;
        }
    }

    public function deleteGroup($userID, $groupID)
    {
        $delete = $this->_GB->_DB->delete('group_members', "`groupID`= '{$groupID}' AND `userID`= '{$userID}'");
        if ($delete) {
            $array = array(
                'success' => true,
                'message' => 'This group is deleted successfully '
            );
            $this->_GB->Json($array);
        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to delete this group '
            );
            $this->_GB->Json($array);
        }

    }

    public function generateRandomID($GroupMembers, $userID)
    {
        $randIndex = array_rand($GroupMembers);
        $current = current($GroupMembers[$randIndex]);
        if ($current == $userID) {
            return 0;
        } else {
            return $current;
        }
    }



}
