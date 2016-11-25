<?php
/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 19/02/2016
 * Time: 23:18
 */
/*
 * All database connection variables
 */

ob_start();
session_start();
error_reporting(0);
return $_Config = array(
    'DB_SERVER' => ':SERVER_NAME:',// db server
    'DB_USER' => ':USER_NAME:',// db user
    'DB_PASSWORD' => ':USER_PASSWORD:',// db password (mention your db password here)
    'DB_NAME' => ':DB_NAME:',// database name
    'DB_TABLE_PREFIX' => ':DB_TABLE_PREFIX:'//database prefix
);