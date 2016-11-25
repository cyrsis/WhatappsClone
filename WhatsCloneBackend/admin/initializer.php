<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/7/16
 * Time: 00:21
 */


if (!file_exists('../config/Config.php')) {
    header('Location: ../config/install/index.php');
    exit;
}

// include the config.php file
include '../config/Config.php';
// include the database connection class
include '../config/DataBase.php';
// include the UsersController class
include '../application/controllers/UsersController.php';
// include the MessagesController class
include '../application/controllers/MessagesController.php';
// include the GroupsController class
include '../application/controllers/GroupsController.php';
// include the pagination class
include '../application/helpers/Pagination.php';
// include the Helper class
include '../application/helpers/Helper.php';
// include the Security class
include '../application/helpers/Security.php';

$_DB = new DataBase($_Config);
$_DB->connect();
$_DB->selectDB();
$Security = new Security($_DB);
$_GB = new Helper($_DB);
$Users = new UsersController($_GB);
$Messages = new MessagesController($_GB,$Users);
$Groups = new GroupsController($_GB);
