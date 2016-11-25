<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/7/16
 * Time: 15:47
 */

include 'initializer.php';
session_destroy();
header('Location: login.php');
