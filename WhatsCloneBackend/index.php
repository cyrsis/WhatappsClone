<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/20/16
 * Time: 08:10
 */
if (!file_exists('config/Config.php')) {
    header('Location: config/install/index.php');
    exit;
}else{
    header('Location: admin/login.php');
    exit;
}