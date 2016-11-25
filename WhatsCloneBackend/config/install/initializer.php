<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/7/16
 * Time: 00:21
 */



// include the config.php file
include '../Config.php';
// include the database connection class
include '../DataBase.php';
// include the Helper class
include '../../application/helpers/Helper.php';

$_DB = new DataBase($_Config);
$_DB->connect();
$_DB->selectDB();
$_GB = new Helper($_DB);
