<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/10/16
 * Time: 17:13
 */
error_reporting(0);
ob_start();
if (file_exists('../Config.php')) {
    header('Location: ../../admin/login.php');
    exit;
}
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Install Wizard</title>
    <link rel="shortcut icon" type="image/x-icon" href="../../uploads/logo.png"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../../admin/material/material.min.css">
    <link rel="stylesheet" href="../../admin/style.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
</head>
<body>


<header class="mdl-layout__header ben-cherif-color--blue  mdl-layout__header--waterfall">
    <div class="mdl-layout__header-row  ">
        <!-- Title -->
        <span class="mdl-layout-title">Installation Process</span>
        <div class="mdl-layout-spacer"></div>
        <div class="mdl-layout__header-row">
            <div class="mdl-layout-spacer"></div>
            <a class=" mdl-color-text--white ben-cherif--nav-link ">Step 1</a>
        </div>
    </div>
</header>
<div class="ben-cherif--badge--color-white   " style="height: 200px;text-align: center">
</div>
<?php

if (isset($_POST['db_server'], $_POST['db_name'], $_POST['db_user_name'], $_POST['db_user_password'], $_POST['db_prefix'])) {
    $connect = mysqli_connect($_POST['db_server'], $_POST['db_user_name'], $_POST['db_user_password']);
    if ($connect) {
        $selectDB = mysqli_select_db($connect, $_POST['db_name']);
        if ($selectDB) {
            $tmp_config = file_get_contents('Config.tmp.php');
            $replaceConfig = str_replace(
                array(':SERVER_NAME:',
                    ':USER_NAME:',
                    ':USER_PASSWORD:',
                    ':DB_NAME:',
                    ':DB_TABLE_PREFIX:'),
                array($_POST['db_server'],
                    $_POST['db_user_name'],
                    $_POST['db_user_password'],
                    $_POST['db_name'],
                    rtrim($_POST['db_prefix'], '_') . '_'), $tmp_config);
            $createConfigFile = file_put_contents('../Config.php', $replaceConfig);
            if (!$createConfigFile) {
                echo '<div class="card-display-error  mdl-shadow--2dp "> <div class="mdl-card__title mdl-color-text--white">Can\'t Create Config File</div></div> ';

            } else {
                // @unlink('config.tmp.php');
                $lines = file('dataBase.sql');
                // Loop through each line
                foreach ($lines as $line) {
                    // Add this line to the current segment
                    if (substr($line, 0, 2) == '--' || $line == '') {
                        continue;
                    }
                    // Add this line to the current segment
                    $templine .= $line;
                    // If it has a semicolon at the end, it's the end of the query
                    if (substr(trim($line), -1, 1) == ';') {
                        // Perform the query
                        mysqli_query($connect, str_replace('wa_', rtrim($_POST['db_prefix'], '_') . '_', $templine)) or print('<div class="card-display-error  mdl-shadow--2dp "> <div class="mdl-card__title mdl-color-text--white">Error performing query \'<strong>' . $templine . '\': ' . mysqli_error($connect) . '</div></div> ');
                        // Reset temp variable to empty
                        $templine = '';
                    }
                }
                header("Refresh: 1; url=../install/information.php");
                echo '<div class="card-display-error-success  mdl-shadow--2dp "> <div class="mdl-card__title mdl-color-text--white">Tables imported successfully</div></div> ';
            }

        } else {
            echo '<div class="card-display-error  mdl-shadow--2dp "> <div class="mdl-card__title mdl-color-text--white">Can\'t Connect To Database (' . $_POST['db_name'] . ')</div></div> ';
        }
    } else {
        echo '<div class="card-display-error  mdl-shadow--2dp "> <div class="mdl-card__title mdl-color-text--white">Can\'t Connect The Server</div></div> ';
    }
}
?>


<form class="card-settings mdl-shadow--4dp" action="" method="POST">

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" name="db_server" id="db_server"
               value="<?php if (isset($_POST['db_server'])) {
                   echo $_POST['db_server'];
               } ?>">
        <label class="mdl-textfield__label" for="db_server">Host Name</label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" value="<?php if (isset($_POST['db_name'])) {
            echo $_POST['db_name'];
        } ?>"
               name="db_name" id="db_name">
        <label class="mdl-textfield__label" for="db_name">Database Name</label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text"
               value="<?php if (isset($_POST['db_user_name'])) {
                   echo $_POST['db_user_name'];
               } ?>" name="db_user_name"
               id="db_user_name">
        <label class="mdl-textfield__label" for="db_user_name">Database Username</label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input"  type="password"
               value="<?php if (isset($_POST['db_user_password'])) {
                   echo $_POST['db_user_password'];
               } ?>"
               name="db_user_password" id="db_user_password">
        <label class="mdl-textfield__label" for="db_user_password">Database Password</label>
    </div>
    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" value="<?php if (isset($_POST['db_prefix'])) {
            echo $_POST['db_prefix'];
        } ?>"
               name="db_prefix" id="db_prefix">
        <label class="mdl-textfield__label" for="db_prefix">Prefix of tables Ex: wa_</label>
    </div>
    <center>
        <button type="submit"
                class="mdl-button mdl-js-button mdl-button--fab mdl-js-ripple-effect mdl-button--colored ben-cherif--badge--color-green fab-card-settings">
            <i class="material-icons mdl-color-text--white">arrow_forward</i></button>
    </center>
</form>
<div class="ben-cherif--badge--color-green   mdl-shadow--4dp " style="height: 250px;text-align: center">
    <a class="ben-cherif--badge--rounded">Step 1</a>
    <p class=" mdl-card_installation-description "> Welcome please fill in the information </p>
</div>

</body>

<footer>
    <!--  Scripts-->
    <script src="../../admin/material/material.js"></script>
</footer>
</html>
