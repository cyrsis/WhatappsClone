<?php

/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/7/16
 * Time: 00:21
 */
include 'initializer.php';
?>
<html>
<head>

    <title>Dashboard</title>
    <link rel="shortcut icon" type="image/x-icon" href="../uploads/logo.png"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="material/material.min.css">
    <link rel="stylesheet" href="style.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">

</head>
<body class="body-login">
<div class="ben-cherif-layout-waterfall mdl-layout mdl-js-layout">

    <?php if ($_GB->getSession('admin') != false) {
        ?>

        <header class="mdl-layout__header ben-cherif-color--blue  mdl-layout__header--waterfall">
            <div class="mdl-layout__header-row  ">
                <!-- Title -->
                <span class="mdl-layout-title">Dashboard</span>
                <div class="mdl-layout-spacer"></div>
                <div class="mdl-layout__header-row">
                    <div class="mdl-layout-spacer"></div>
                    <a class=" mdl-color-text--white ben-cherif--nav-link " href="signOut.php">Sign out</a>
                </div>
            </div>
        </header>
        <div class="mdl-layout__drawer ben-cherif-color--light-black mdl-color-text--white">
            <?php
            $userID = $_GB->getSession('admin');
            $query = $_DB->select('admins', '*', '`id`=' . $userID);
            $fetch = $_DB->fetchAssoc($query);
            $username = $fetch['username'];
            if ($fetch['image'] != null) {
                ?>
                <div class="ben-cherif-color--lighter-black"><span
                        class="mdl-layout-title ben-cherif-navigation-drawer--title"><img
                            class="img-rounded ben-cherif-navigation-drawer--image"
                            src="../<?php echo $_GB->getSafeImage($fetch['image']); ?>">
                        <?php echo $username ?>
                        <a  href="editProfile.php"> <i class="material-icons  mdl-color-text--white ben-cherif-icons ">mode_edit</i> </a>
                        </span></div>
                <?php
            } else {
                ?>
                <div class="ben-cherif-color--lighter-black"><span
                        class="mdl-layout-title ben-cherif-navigation-drawer--title"><img
                            class="img-rounded ben-cherif-navigation-drawer--image"
                            src="../uploads/logo.png"> <?php echo $username ?>
                        <a  href="editProfile.php"> <i class="material-icons  mdl-color-text--white ben-cherif-icons ">mode_edit</i> </a>
                    </span>
                </div>
                <?php
            }
            ?>
            <nav class="mdl-navigation ">


                <a class="mdl-navigation__link mdl-color-text--white " href="index.php"><i
                        class="material-icons  mdl-color-text--white ben-cherif-icons ">pie_chart</i>Dashboard </a>
                <a class="mdl-navigation__link mdl-color-text--white" href="users.php?cmd=users"> <i
                        class="material-icons  mdl-color-text--white ben-cherif-icons ">person_outline</i>Users</a>
                <a class="mdl-navigation__link mdl-color-text--white" href="messages.php?cmd=messages"> <i
                        class="material-icons  mdl-color-text--white ben-cherif-icons ">mail_outline</i> Messages</a>
                <a class="mdl-navigation__link mdl-color-text--white" href="groups.php?cmd=groups"> <i
                        class="material-icons  mdl-color-text--white ben-cherif-icons ">people_outline</i>Groups</a>
                <a class="mdl-navigation__link mdl-color-text--white" href="settings.php"> <i
                        class="material-icons  mdl-color-text--white ben-cherif-icons ">settings</i>Settings</a>

            </nav>
        </div>
    <?php } else { ?>

        <header class="mdl-layout__header ben-cherif-color--blue  mdl-layout__header--waterfall">
        </header>
    <?php } ?>

    <main class="mdl-layout__content ">
        <div class="page-content">