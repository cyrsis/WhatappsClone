<?php
/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 19/02/2016
 * Time: 23:28
 */



ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);


// include the database connection class
include 'config/DataBase.php';
// include the config file
include 'config/Config.php';
// include the SessionsController class
include 'application/controllers/SessionsController.php';
// include the UsersController class
include 'application/controllers/UsersController.php';
// include the MessagesController class
include 'application/controllers/MessagesController.php';
// include the GroupsController class
include 'application/controllers/GroupsController.php';
// include the ProfileController class
include 'application/controllers/ProfileController.php';
// include the Pagination class
include 'application/helpers/Pagination.php';
// include the Helper class
include 'application/helpers/Helper.php';
// include the Security class
include 'application/helpers/Security.php';


$_DB = new DataBase($_Config);
$_DB->connect();
$_DB->selectDB();
$Security = new Security($_DB);
$_GB = new Helper($_DB);
$Users = new UsersController($_GB);
$Messages = new MessagesController($_GB, $Users);
$Groups = new GroupsController($_GB);
$Profile = new ProfileController($_GB);

$cmd = $_GET['cmd'];

if (isset($cmd)) {
    if (isset($_SERVER['HTTP_TOKEN'])) {
        $token = $_SERVER['HTTP_TOKEN'];

    } else {
        $token = null;
    }
    if (isset($_SERVER['HTTP_ACCEPT'])) {
        $Accept = $_SERVER['HTTP_ACCEPT'];
    } else {
        $Accept = null;
    }
    switch ($cmd) {


        case 'DeleteUserAccount':
            $userID = $Users->getUserIdByToken($token);
            $phone = $_POST['phone'];
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $Users->DeleteAccount($userID, $phone);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;
        case 'Join':
            if (isset($_POST['phone'])) {
               $array = $Users->SignIn($_POST['phone'], $_POST['country']);
                $_GB->Json($array);
            } else {
                // failed to insert row
                $array = array(
                    'success' => false,
                    'message' => 'Oops! some params are missing.',
                    'mobile' => null,
                    'code' => null
                );
                $_GB->Json($array);
            }
            break;

        case 'verifyUser':
            if (isset($_POST['code'])) {
                $Users->activateUser($_POST['code']);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Oops! some params are missing.'
                );
                $_GB->Json($array);
            }
            break;


        case 'resend':
            if (isset($_POST['phone'])) {
                $Users->ResendCode($_POST['phone']);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Oops! some params are missing.'
                );
                $_GB->Json($array);
            }
            break;

        case 'SendContacts':
            if (isset($_POST)) {
                $array = file_get_contents('php://input');
                $_POST = json_decode($array, true);
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    $Users->comparePhoneNumbers($_POST);
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Oops! some params are missing.'
                );
                $_GB->Json($array);
            }
            break;
        case 'GetContact':
            $userID = $_GET['userID'];
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $Users->getContactInfo($userID);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;
        case 'GetGroup':
            $userID = $Users->getUserIdByToken($token);
            $groupID = $_GET['groupID'];
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $Groups->getGroupInfo($groupID, $userID);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;
        case 'GetGroupMembers':
            $groupID = $_GET['groupID'];
            $isValidToken = $Users->getSessionToken($token);
            $userID = $Users->getUserIdByToken($token);

            if ($isValidToken) {
                if ($userID != null) {
                    $query = " SELECT GM.id ,GM.role,GM.groupID,U.id AS userId,U.username,U.phone,U.image,U.status,U.status_date,U.is_activated
                             FROM prefix_users U,prefix_groups G,prefix_group_members GM
                             WHERE
                             CASE
                             WHEN GM.userID = U.id
                             THEN GM.groupID = G.id
                              END
                              AND 
                              G.id = {$groupID}
                              AND
                              U.is_activated = 1  ORDER BY GM.id ASC";
                    $query = $_DB->MySQL_Query($query);
                    $Groups->GetGroupMembers($query);

                }
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;

        case 'EditName':
            if (isset($_POST)) {
                $userID = $Users->getUserIdByToken($token);
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    $array = file_get_contents('php://input');
                    $_POST = json_decode($array, true);
                    $newstatus = $_POST['newStatus'];
                    $Users->editName($newstatus, $userID);
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }

            break;

        case 'EditGroupName':
            if (isset($_POST)) {
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    $array = file_get_contents('php://input');
                    $_POST = json_decode($array, true);
                    $newstatus = $_POST['newStatus'];
                    $groupID = $_POST['statusID'];
                    $Groups->EditGroupName($newstatus, $groupID);
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }

            break;

        case 'ExitGroup':
            $userID = $Users->getUserIdByToken($token);
            $groupID = $_GET['groupID'];
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $Groups->exitGroup($userID, $groupID);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;

        case 'DeleteGroup':
            $userID = $Users->getUserIdByToken($token);
            $groupID = $_GET['groupID'];
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $Groups->deleteGroup($userID, $groupID);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;

        case 'GetStatus':
            $userID = $Users->getUserIdByToken($token);
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                if ($userID != null) {
                    $query = "
                            SELECT S.*,U.status AS currentStatus,S.id AS currentStatusID
                           FROM prefix_users U,prefix_status S
                           WHERE
                           CASE
                           WHEN S.userID = {$userID}
                           THEN U.id = {$userID}
                           END
                           AND
                            S.status = U.status
                            AND
                            S.userID = U.id
                             GROUP BY S.id
                             UNION
                           SELECT * FROM (   SELECT S.*,U.is_activated AS currentStatus,S.status AS currentStatusID
                           FROM prefix_users U,prefix_status S
                           WHERE
                           CASE
                           WHEN S.userID = {$userID}
                           THEN U.id = {$userID}
                            END
                            AND
                            S.userID = U.id
                             AND
                            S.current = 0
                           GROUP BY S.id
                              ) t   ORDER BY currentStatusID DESC ";
                    $query = $_DB->MySQL_Query($query);
                    $rows = $_DB->numRows($query);
                    $page = (isset($_GET['page']) && !empty($_GET['page'])) ? $Security->MA_INT($_GET['page']) : 1;
                    $_PAG = new Pagination($page,
                        $rows
                        , 6,
                        'api.php?page=#i#');
                    if ($page > $_PAG->pages) {
                        $_GB->Json(array());
                    } else {
                        $Users->getStatus($query);
                    }
                }
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;
        case 'EditStatus':
            if (isset($_POST)) {
                $userID = $Users->getUserIdByToken($token);
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if ($userID != 0) {
                        $array = file_get_contents('php://input');
                        $_POST = json_decode($array, true);
                        $newstatus = $_POST['newStatus'];
                        $Users->insertStatus($userID, $newstatus);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }

            break;
        case 'UpdateStatus':
            $userID = $Users->getUserIdByToken($token);
            $statusID = $_GET['statusID'];
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $Users->updateStatus($userID, $statusID);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;
        case 'DeleteStatus':
            $userID = $Users->getUserIdByToken($token);
            $statusID = $_GET['statusID'];
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $Users->DeleteStatus($userID, $statusID);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;
        case 'DeleteAllStatus':
            $userID = $Users->getUserIdByToken($token);
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $query = "DELETE S.* FROM  prefix_status S
                           JOIN prefix_users U ON   S.userID = U.id
                           WHERE
                           CASE
                           WHEN S.userID = {$userID}
                           THEN U.id = {$userID}
                            END
                            AND
                            S.status != U.status";
                $query = $_DB->MySQL_Query($query);

                $Users->DeleteAllStatus($query);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }
            break;


            break;
        case 'checkUnsentMessageGroup':
            $Messages->checkUnsentMessageGroup($_POST);
            break;
        case 'saveMessageGroup':
            $Messages->saveMessageGroup($_POST);
            break;
        case 'sendMessageGroup':
            $Messages->sendMessageGroup($_POST);
            break;
        case 'sendMessage':
            $Messages->sendMessage($_POST);
            break;

        case 'createGroup':
            if (isset($_POST)) {
                $userID = $_POST['userID'];
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if ($userID != 0) {
                        $groupID = $_POST['name'];
                        if (isset($_FILES['image'])) {
                            $imageID = $_GB->uploadImage($_FILES['image']);
                        } else {
                            $imageID = null;
                        }
                        $ids = $_POST['ids'];
                        $date = $_POST['date'];
                        $string = substr($date, 1, -1);
                        $Groups->createGroup($groupID, $imageID, $userID, $ids, $string);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }

            break;


        case 'addMembersToGroup':
            if (isset($_POST)) {
                $userID = $Users->getUserIdByToken($token);
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if ($userID != 0) {
                        $groupID = $_POST['groupID'];
                        $ids = $_POST['ids'];
                        $Groups->addMembersToGroup($groupID, $ids);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }


            break;

        case 'makeMemberAdmin':
            if (isset($_POST)) {
                $userID = $Users->getUserIdByToken($token);
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if ($userID != 0) {
                        $groupID = $_POST['groupID'];
                        $id = $_POST['id'];
                        $Groups->makeMemberAdmin($groupID, $id);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }


            break;

        case 'removeMemberFromGroup':
            if (isset($_POST)) {
                $userID = $Users->getUserIdByToken($token);
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if ($userID != 0) {
                        $groupID = $_POST['groupID'];
                        $id = $_POST['id'];
                        $Groups->removeMemberFromGroup($groupID, $id);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }


            break;
        case 'getGroups':
            $userID = $Users->getUserIdByToken($token);
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                $Groups->getGroups($userID);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }

            break;
        case 'getGroupMessages':
            $isValidToken = $Users->getSessionToken($token);
            if ($isValidToken) {
                if (isset($_GET['groupID'])) {
                    $groupID = $_GET['groupID'];
                    $query = " SELECT M.id,
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
                  ORDER BY M.Date ASC";
                    $query = $_DB->MySQL_Query($query);
                    $rows = $_DB->numRows($query);
                    $page = (isset($_GET['page']) && !empty($_GET['page'])) ? $Security->MA_INT($_GET['page']) : 1;
                    $_PAG = new Pagination($page,
                        $rows
                        , 20,
                        'api.php?page=#i#');
                    if ($page > $_PAG->pages) {
                        $_GB->Json(array("messages" => null));
                    } else {
                        $Groups->getGroupMessages($_GET['groupID'], $_PAG->limit);
                    }

                }
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Unauthorized'
                );
                $_GB->Json($array);
            }

            break;


        case 'uploadImage':
            if (isset($_POST)) {
                $userID = $Users->getUserIdByToken($token);
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if ($userID != 0) {
                        if (isset($_FILES['image'])) {
                            $imageHash = $_GB->uploadImage($_FILES['image']);
                        } else {
                            $imageHash = null;
                        }

                        $Profile->uploadProfileImage($imageHash, $userID);
                    }else{
                        $array = array(
                            'success' => false,
                            'message' => 'Oops! Something went wrong'
                        );
                        $_GB->Json($array);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }

            break;


        case 'uploadGroupImage':
            if (isset($_POST)) {
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if (isset($_FILES['image'])) {
                        $imageHash = $_GB->uploadImage($_FILES['image']);
                    } else {
                        $imageHash = null;
                    }
                    $groupID = $_POST['groupID'];
                    $Profile->uploadProfileGroupImage($imageHash, $groupID);
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Unauthorized'
                    );
                    $_GB->Json($array);
                }
            }

            break;

        case 'uploadMessagesImage':
            if (isset($_POST)) {
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if (isset($_FILES['image'])) {
                        $imageHash = $_GB->uploadImage($_FILES['image']);
                    } else {
                        $imageHash = null;
                    }

                    if ($imageHash != null) {
                        $url = $_GB->getSafeImage($imageHash);
                        $array = array(
                            'success' => true,
                            'url' => $url,
                            'videoThumbnail' => null
                        );
                        $_GB->Json($array);

                    } else {
                        $array = array(
                            'success' => false,
                            'url' => null,
                            'videoThumbnail' => null
                        );
                        $_GB->Json($array);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'url' => 'Unauthorized',
                        'videoThumbnail' => null
                    );
                    $_GB->Json($array);
                }
            }

            break;
        case 'uploadMessagesAudio':
            if (isset($_POST)) {
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if (isset($_FILES['audio'])) {
                        $audioHash = $_GB->uploadAudio($_FILES['audio']);
                    } else {
                        $audioHash = null;
                    }

                    if ($audioHash != null) {
                        $url = $_GB->getAudioFileUrl($audioHash);
                        $array = array(
                            'success' => true,
                            'url' => $url,
                            'videoThumbnail' => null
                        );
                        $_GB->Json($array);

                    } else {
                        $array = array(
                            'success' => false,
                            'url' => null,
                            'videoThumbnail' => null
                        );
                        $_GB->Json($array);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'url' => 'Unauthorized',
                        'videoThumbnail' => null
                    );
                    $_GB->Json($array);
                }
            }

            break;

        case 'uploadMessagesDocument':
            if (isset($_POST)) {
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {
                    if (isset($_FILES['document'])) {
                        $documentHash = $_GB->uploadDocument($_FILES['document']);
                    } else {
                        $documentHash = null;
                    }

                    if ($documentHash != null) {
                        $url = $_GB->getDocumentFileUrl($documentHash);
                        $array = array(
                            'success' => true,
                            'url' => $url,
                            'videoThumbnail' => null
                        );
                        $_GB->Json($array);

                    } else {
                        $array = array(
                            'success' => false,
                            'url' => null,
                            'videoThumbnail' => null
                        );
                        $_GB->Json($array);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'url' => 'Unauthorized',
                        'videoThumbnail' => null
                    );
                    $_GB->Json($array);
                }
            }

            break;
        case 'uploadMessagesVideo':
            if (isset($_POST)) {
                $isValidToken = $Users->getSessionToken($token);
                if ($isValidToken) {

                    if (isset($_FILES['video'])) {
                        $videoHash = $_GB->uploadVideo($_FILES['video']);
                    } else {
                        $videoHash = null;
                    }

                    if (isset($_FILES['thumbnail'])) {
                        $VideoThumbnailHash = $_GB->uploadVideoThumbnail($_FILES['thumbnail']);
                    } else {
                        $VideoThumbnailHash = null;
                    }

                    if ($videoHash != null) {
                        $url = $_GB->getVideoFileUrl($videoHash);
                        $urlThumbnail = $_GB->getVideoThumbnailFileUrl($VideoThumbnailHash);

                        $array = array(
                            'success' => true,
                            'url' => $url,
                            'videoThumbnail' => $urlThumbnail
                        );
                        $_GB->Json($array);

                    } else {
                        $array = array(
                            'success' => false,
                            'url' => null,
                            'videoThumbnail' => null
                        );
                        $_GB->Json($array);
                    }
                } else {
                    $array = array(
                        'success' => false,
                        'url' => 'Unauthorized',
                        'videoThumbnail' => null
                    );
                    $_GB->Json($array);
                }
            }

            break;



    }

} else {

    $array = array(
        'success' => false,
        'message' => ' Required field(s) is missing'
    );
    $_GB->Json($array);

}
