<?php

/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 19/02/2016
 * Time: 22:47
 */

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

class UsersController
{

    public $_GB;

    public function __construct($_GB)
    {
        $this->_GB = $_GB;
    }


    public function SignIn($phone, $countryName)
    {
        $code = rand(100000, 999999);
        $res = $this->createUser($phone, $code, $countryName);
        return $res;
    }


    public function sendMessageThroughGCM($mobile, $code)
    {

        $otp_prefix = ':';
        $app_name = $this->_GB->getSettings('app_name');


        //Your authentication key
        $authKey = $this->_GB->getSettings('sms_authentication_key');

        //Multiple mobiles numbers separated by comma
        $mobileNumber = $mobile;

        //Sender ID,While using route4 sender id should be 6 characters long.
        $senderId = $this->_GB->getSettings('sms_sender');

        //Your message to send, Add URL encoding here.
        $message = urlencode("Hello,Welcome to $app_name. Your Verification code is $otp_prefix $code ");
        $response_type = 'json';
        //Define route
        $route = "4";
        //Define route
        $country = "0";
        //Prepare you post parameters
        $postData = array(
            'authkey' => $authKey,
            'user' => $this->_GB->getSettings('sms_username'),
            'password' => $this->_GB->getSettings('sms_password'),
            'mobiles' => $mobileNumber,
            'message' => $message,
            'sender' => $senderId,
            'route' => $route,
            'country' => $country,
            'response' => $response_type
        );

        //API URL
        $url = $this->_GB->getSettings('sms_api_url');

        // init the resource
        $ch = curl_init();
        curl_setopt_array($ch, array(
            CURLOPT_URL => $url,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => $postData
        ));


        //Ignore SSL certificate verification
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);


        //get response
        $output = curl_exec($ch);

        //Print error if any
        if (curl_errno($ch)) {
            echo 'error:' . curl_error($ch);
        }

        curl_close($ch);
        return $output;


    }


    public function createUser($phone, $code, $countryName)
    {
        $app_name = $this->_GB->getSettings('app_name');
        if (!$this->UserExist($phone)) {
            // Generating API key
            $api_key = $this->generateApiKey();


            $arrayData = array(
                'phone' => $phone,
                'apikey' => $api_key,
                'status' => 'Hey i am using ' . $app_name . ' enjoy it',
                'status_date' => time(),
                'country' => $countryName,
                'is_activated' => 0
            );
            $result = $this->_GB->_DB->insert('users', $arrayData);
            $newUserID = $this->_GB->_DB->last_Id();
            $this->insertDefaultStatus($newUserID);
            // check if row inserted or not
            if ($result) {
                $IDResult = $this->_GB->_DB->select('users', '*', "  `phone` = '{$phone}'");
                if ($this->_GB->_DB->numRows($IDResult) > 0) {
                    $fetch = $this->_GB->_DB->fetchAssoc($IDResult);
                    $res = $this->createCode($fetch['id'], $code);
                    if ($res) {
                        // successfully inserted into database
                        // send sms
                        $this->sendMessageThroughGCM($phone, $code);
                        $array = array(
                            'success' => true,
                            'message' => 'SMS request is initiated! You will be receiving it shortly.',
                            'mobile' => $phone,
                            'code' => $code
                        );
                        return $array;
                    } else {
                        // Failed to create user
                        $array = array(
                            'success' => false,
                            'message' => 'Sorry! Error occurred in registration.',
                            'mobile' => null,
                            'code' => null
                        );
                        return $array;
                    }
                }

            } else {
                // Failed to create user
                $array = array(
                    'success' => false,
                    'message' => 'Sorry! Error occurred in registration.',
                    'mobile' => null,
                    'code' => null
                );
                return $array;

            }
        } else if ($this->UserExist($phone)) {
            // User with same phone already existed in the database

            // Generating API key
            $api_key = $this->generateApiKey();

            $fields = "`apikey` = '" . $api_key . "'";
            $fields .= ",`is_activated` = '" . 0 . "'";
            $result = $this->_GB->_DB->update('users', $fields, "`phone` = {$phone}");

            // check if row inserted or not
            if ($result) {
                $IDResult = $this->_GB->_DB->select('users', '*', "  `phone` = '{$phone}'");
                if ($this->_GB->_DB->numRows($IDResult) > 0) {
                    $fetch = $this->_GB->_DB->fetchAssoc($IDResult);
                    $this->createCode($fetch['id'], $code);
                    $res = $this->createCode($fetch['id'], $code);
                    if ($res) {
                        // successfully inserted into database
                        // send sms
                        $this->sendMessageThroughGCM($phone, $code);
                        $array = array(
                            'success' => true,
                            'message' => 'SMS request is initiated! You will be receiving it shortly.',
                            'mobile' => $phone,
                            'code' => $code
                        );
                        return $array;

                    } else {
                        // Failed to create user
                        $array = array(
                            'success' => false,
                            'message' => 'Sorry! Error occurred in registration.',
                            'mobile' => null,
                            'code' => null
                        );
                        return $array;

                    }
                }

            } else {
                // Failed to create user
                $array = array(
                    'success' => false,
                    'message' => 'Sorry! Error occurred in registration.',
                    'mobile' => null,
                    'code' => null
                );
                return $array;

            }
        } else {
            $array = array(
                'success' => false,
                'message' => 'Sorry! mobile number is not valid or missing.',
                'mobile' => null,
                'code' => null
            );
            return $array;
        }
    }

    /**
     * Function to  check if th user is already exist.
     * @param $phone
     * @return bool
     * @internal param $UserName
     */
    public function UserExist($phone)
    {
        $query = $this->_GB->_DB->select('users', '`id`', "`phone` = '{$phone}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            return true;
        } else {
            return false;
        }
        $this->_GB->_DB->free();
    }


    public function insertDefaultStatus($userID)
    {
        $app_name = $this->_GB->getSettings('app_name');
        $arrayStatus = array("Only Emergency calls", "Busy", "At work", "in a meeting", "Available", "Playing football", "Hey i am using $app_name enjoy it");
        $lastElement = end($arrayStatus);
        foreach ($arrayStatus as $status) {
            if ($status == $lastElement) {
                $addDefaultStatus = array(
                    'status' => $status,
                    'userID' => $userID,
                    'current' => 1
                );
            } else {
                $addDefaultStatus = array(
                    'status' => $status,
                    'userID' => $userID,
                    'current' => 0
                );
            }
            $this->_GB->_DB->insert('status', $addDefaultStatus);
        }
    }

    /**
     * Function to  check if th user is already exist.
     * @param $phone
     * @return bool
     * @internal param $UserName
     */
    public function UserLinked($phone)
    {
        $activated = 1;
        $query = $this->_GB->_DB->select('users', '`id`', "`phone` = '{$phone}' AND `is_activated` = '{$activated}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            return true;
        } else {
            return false;
        }
        $this->_GB->_DB->free();
    }

    public function ResendCode($phone)
    {
        $code = rand(100000, 999999);

        $IDResult = $this->_GB->_DB->select('users', '*', "  `phone` = '{$phone}'");
        if ($this->_GB->_DB->numRows($IDResult) > 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($IDResult);
            $res = $this->createCode($fetch['id'], $code);
            if ($res) {
                // successfully inserted into database
                // send sms
                $this->sendMessageThroughGCM($phone, $code);
                $array = array(
                    'success' => true,
                    'message' => 'SMS request is Resend ! You will be receiving it shortly.',
                );
                $this->_GB->Json($array);
            } else {

                $array = array(
                    'success' => false,
                    'message' => 'Sorry! Error occurred .',
                );
                $this->_GB->Json($array);
            }

        }


    }

    public function createCode($UserID, $code)
    {
        // delete the old otp if exists
        $this->_GB->_DB->delete('sms_codes', "`UserID`= '{$UserID}'");
        $array = array(
            'UserID' => $UserID,
            'code' => $code,
            'status' => 0
        );
        $result = $this->_GB->_DB->insert('sms_codes', $array);
        return $result;
    }


    public function activateUser($code)
    {
        $query = (" SELECT  U.id,
                           U.username,
                           U.phone,
                           U.apikey,
                           U.is_activated
                           FROM prefix_users U, prefix_sms_codes S
                           WHERE S.code = {$code}
                           AND S.UserID = U.id ");
        $query = $this->_GB->_DB->MySQL_Query($query);
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);

            $is_activated = 1;
            $this->_GB->_DB->update('users', "`is_activated` = '{$is_activated}' ", "`id`='{$fetch['id']}'");
            $this->_GB->_DB->update('sms_codes', "`status` = '{$is_activated}' ", "`UserID`='{$fetch['id']}'");

            $array = array(
                'success' => true,
                'message' => 'Your account has been created successfully.',
                'userID' => $fetch['id'],
                'token' => $fetch['apikey']
            );
            $this->_GB->Json($array);
        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to activate your account try again or resend sms to get new code.',
                'userID' => null,
                'token' => null
            );
            $this->_GB->Json($array);
        }

    }


    /**
     * Generating random Unique MD5 String for user Api key
     */
    private function generateApiKey()
    {
        return md5(uniqid(rand(), true));
    }


    public function comparePhoneNumbers($array)
    {
        $contactsModelList = $array['contactsModelList'];
        $resultFinal = array();
        for ($i = 0; $i < count($contactsModelList); $i++) {
            $phone = $contactsModelList[$i]['phone'];
            $username = $contactsModelList[$i]['username'];
            $contactID = $contactsModelList[$i]['contactID'];
            if ($this->UserLinked($phone)) {
                $result = $this->_GB->_DB->select('users', '*', "  `phone` = '{$phone}'");
                if ($this->_GB->_DB->numRows($result) != 0) {
                    $fetch = $this->_GB->_DB->fetchAssoc($result);
                    $fetch['contactID'] = $contactID;
                    $fetch['username'] = (empty($fetch['username'])) ? $username : $fetch['username'];
                    $fetch['Linked'] = true;
                    $fetch['Exist'] = true;
                    $fetch['phone'] = (empty($fetch['phone'])) ? $phone : $fetch['phone'];
                    $fetch['image'] = $this->_GB->getSafeImage($fetch['image']);
                    $fetch['status_date'] = (empty($fetch['status_date'])) ? null : $this->_GB->Date($fetch['status_date']);
                    unset ( $fetch['apikey']);
                    $resultFinal [] = $fetch;
                }

            } else {
                $fetch = array('id' => $contactID,
                    'contactID' => $contactID,
                    'Linked' => false,
                    'Exist' => true,
                    'status' => $phone,
                    'phone' => $phone,
                    'username' => $username);
                $resultFinal [] = $fetch;
            }
        }
        $this->_GB->Json($resultFinal);

    }


    public function getSessionToken($apikey)
    {
        $apikey = $this->_GB->_DB->escapeString($apikey);
        $query = $this->_GB->_DB->select('users', 'apikey', "`apikey`= '{$apikey} '  ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public function getUserIdByToken($apikey)
    {
        $apikey = $this->_GB->_DB->escapeString($apikey);
        $query = $this->_GB->_DB->select('users', 'id', "`apikey`= '{$apikey} '");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            return $fetch['id'];
        } else {
            return 0;
        }
    }

    public function getContactInfo($userID)
    {
        $query = $this->_GB->_DB->select('users', '*', "`id` = '{$userID}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            $fetch['id'] = (empty($fetch['id'])) ? null : $fetch['id'];
            $fetch['username'] = (empty($fetch['username'])) ? null : $fetch['username'];
            $fetch['phone'] = (empty($fetch['phone'])) ? null : $fetch['phone'];
            $fetch['image'] = (empty($fetch['image'])) ? null : $this->_GB->getSafeImage($fetch['image']);
            $fetch['Linked'] = $this->UserLinked($fetch['phone']);
            $fetch['status'] = (empty($fetch['status'])) ? null : $fetch['status'];
            $fetch['status_date'] = (empty($fetch['status_date'])) ? null : $this->_GB->Date($fetch['status_date']);
            unset($fetch['apikey'], $fetch['is_activated'], $fetch['created_at'], $fetch['country']);
            $this->_GB->Json($fetch);

        } else {
            $this->_GB->Json(null);
        }
    }

    public function getRecipientInfo($userID)
    {
        $query = $this->_GB->_DB->select('users', '*', "`id` = '{$userID}' ");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            $fetch['id'] = (empty($fetch['id'])) ? null : $fetch['id'];
            $fetch['username'] = (empty($fetch['username'])) ? null : $fetch['username'];
            $fetch['phone'] = (empty($fetch['phone'])) ? null : $fetch['phone'];
            $fetch['image'] = (empty($fetch['image'])) ? null : $this->_GB->getSafeImage($fetch['image']);
            $fetch['Linked'] = $this->UserLinked($fetch['phone']);
            $fetch['status'] = (empty($fetch['status'])) ? null : $fetch['status'];
            $fetch['status_date'] = (empty($fetch['status_date'])) ? null : $this->_GB->Date($fetch['status_date']);
            unset($fetch['apikey'], $fetch['is_activated'], $fetch['created_at']);
            return $fetch;

        } else {
            return null;
        }
    }

    public function getStatus($query)
    {
        if ($this->_GB->_DB->numRows($query) != 0) {
            $status = array();
            while ($fetch = $this->_GB->_DB->fetchAssoc($query)) {

                $fetch['currentStatusID'] = is_numeric($fetch['currentStatus']) ? null : $fetch['currentStatusID'];
                $fetch['currentStatus'] = is_numeric($fetch['currentStatus']) ? null : $fetch['currentStatus'];
                unset($fetch['userid'], $fetch['username'], $fetch['image'], $fetch['phone'], $fetch['apikey'], $fetch['is_activated'], $fetch['created_at']);
                $status[] = $fetch;

            }
            $this->_GB->Json($status);
        } else {
            $this->_GB->Json(null);
        }
    }

    public function editStatus($newStatus, $userID, $statusID)
    {
        $fields = "`status` = '" . $newStatus . "'";
        $result = $this->_GB->_DB->update('status', $fields, "`id` = '{$statusID}' AND `userID` = {$userID}");


        // check if row inserted or not
        if ($result) {

            $fields .= ",`status_date` = '" . time() . "'";
            $this->_GB->_DB->update('users', $fields, "`id` = {$userID}");
            $array = array(
                'success' => true,
                'message' => 'Status is updated successfully '
            );
            $this->_GB->Json($array);
        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to update status '
            );
            $this->_GB->Json($array);
        }
    }

    public function existStatus($userID, $status)
    {
        $query = $this->_GB->_DB->select('status', '*', "`status` = '{$status}' AND `userID` = '{$userID}'");
        if ($this->_GB->_DB->numRows($query) != 0) {
            return true;
        } else {
            return false;
        }

    }

    public function insertStatus($userID, $status)
    {
        if (strpos($status, '\'') !== false) {
            $status = str_replace('\'', "\\'", $status);
        }

        if ($this->existStatus($userID, $status)) {
            $array = array(
                'success' => true,
                'message' => 'Status already exist '
            );
            $this->_GB->Json($array);
        } else {
            $fields = "`current` = '" . 0 . "'";
            $this->_GB->_DB->update('status', $fields, "`userID` = {$userID}");

            $addNewStatus = array(
                'status' => $status,
                'userID' => $userID,
                'current' => 1
            );

            $insert = $this->_GB->_DB->insert('status', $addNewStatus);


            if ($insert) {
                $fields = "`status` = '" . $status . "'";
                $fields .= ",`status_date` = '" . time() . "'";
                $result = $this->_GB->_DB->update('users', $fields, "`id` = {$userID}");

                // check if row inserted or not
                if ($result) {
                    $array = array(
                        'success' => true,
                        'message' => 'Status is updated successfully '
                    );
                    $this->_GB->Json($array);
                } else {
                    $array = array(
                        'success' => false,
                        'message' => 'Failed to update status '
                    );
                    $this->_GB->Json($array);
                }
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Failed to insert status '
                );
                $this->_GB->Json($array);
            }
        }
    }

    public function updateStatus($userID, $statusID)
    {

        $status = null;
        $query = $this->_GB->_DB->select('status', '*', "`id` = '{$statusID}' AND `userID` = '{$userID}'");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            $status = $fetch['status'];
            $field1 = "`current` = '" . 0 . "'";
            $field2 = "`current` = '" . 1 . "'";
            $this->_GB->_DB->update('status', $field1, "`userID` = {$userID}");
            $this->_GB->_DB->update('status', $field2, "`id` = '{$statusID}' AND `userID` = {$userID}");
        }
        if (strpos($status, '\'') !== false) {
            $status = str_replace('\'', "\\'", $status);
        }
        $fields = "`status` = '" . $status . "'";
        $fields .= ",`status_date` = '" . time() . "'";
        $result = $this->_GB->_DB->update('users', $fields, "`id` = {$userID}");

        // check if row inserted or not
        if ($result) {
            $array = array(
                'success' => true,
                'message' => 'Status is updated successfully '
            );
            $this->_GB->Json($array);
        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to update status '
            );
            $this->_GB->Json($array);
        }

    }

    public function DeleteStatus($userID, $statusID)
    {
        $delete = $this->_GB->_DB->delete('status', "`id`= '{$statusID}' AND `userID`= '{$userID}'");
        if ($delete) {
            $array = array(
                'success' => true,
                'message' => 'Status is deleted successfully '
            );
            $this->_GB->Json($array);
        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to delete status '
            );
            $this->_GB->Json($array);
        }
    }


    public function DeleteAllStatus($delete)
    {
        if ($delete) {
            $array = array(
                'success' => true,
                'message' => 'All Status are deleted successfully '
            );
            $this->_GB->Json($array);
        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to delete status '
            );
            $this->_GB->Json($array);
        }
    }


    public function editName($name, $userID)
    {
        $fields = "`username` = '" . $name . "'";
        $result = $this->_GB->_DB->update('users', $fields, "`id` = '{$userID}' ");

        // check if row inserted or not
        if ($result) {
            $array = array(
                'success' => true,
                'message' => 'Name  is updated successfully '
            );
            $this->_GB->Json($array);
        } else {
            $array = array(
                'success' => false,
                'message' => 'Failed to update name '
            );
            $this->_GB->Json($array);
        }
    }


    public function DeleteAccount($userID, $phone)
    {
        $phone = $this->_GB->_DB->escapeString($phone);
        $userID = $this->_GB->_DB->escapeString($userID);

        if ($this->UserExist($phone)) {
            $delete = $this->_GB->_DB->delete('users', " `id` =  {$userID}  AND `phone` = {$phone} ");
            if ($delete) {
                $array = array(
                    'success' => true,
                    'message' => 'Your account is deleted successfully'
                );
                $this->_GB->Json($array);
            } else {
                $array = array(
                    'success' => false,
                    'message' => 'Failed to delete your account'
                );
                $this->_GB->Json($array);
            }
        }else{
            $array = array(
                'success' => false,
                'message' => 'Failed to delete your account'
            );
            $this->_GB->Json($array);
        }
    }



    /****************************
     * functions for admins
     ****************************/

    /**
     * Function for admin login
     * @param $username
     * @param $password
     */
    public
    function adminLogin($username, $password)
    {

        $username = trim($this->_GB->_DB->escapeString($username));
        $password = trim($password);
        $adminPassword = md5($password);
        $query = $this->_GB->_DB->select('admins', '*', "`username` = '{$username}' AND `password` = '{$adminPassword}'");
        $fetch = $this->_GB->_DB->fetchAssoc($query);
        if (empty($username) || empty($password)) {
            echo $this->_GB->ErrorDisplay('All fields are required');
        } else if ($this->_GB->_DB->numRows($query) <= 0) {
            echo $this->_GB->ErrorDisplay('Login failed please try again later');
        } else {
            $this->_GB->setSession('admin', $fetch['id']);
            $this->_GB->setSession('adminName', $fetch['username']);
            header("Refresh: 1; url=index.php");
            echo $this->_GB->ErrorDisplay('Logged in successfully.', 'yes');
        }
        $this->_GB->_DB->free($query);
    }

}
