<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/11/16
 * Time: 00:37
 */

ob_start();
include 'initializer.php';

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
            <a class=" mdl-color-text--white ben-cherif--nav-link ">Step 2</a>
        </div>
    </div>
</header>
<div class="ben-cherif--badge--color-white   " style="height: 200px;text-align: center">
</div>
<?php
if (isset($_POST['base_url'], $_POST['app_name'], $_POST['admin_name'], $_POST['admin_password'])) {

    $base_url = $_POST['base_url'];
    $app_name = $_POST['app_name'];
    $admin_name = $_POST['admin_name'];
    $admin_password = $_POST['admin_password'];

    $_GB->updateSettings("base_url", $base_url);
    $_GB->updateSettings("app_name", $app_name);
    $adminData = array(
        'username' => $admin_name,
        'password' => md5($admin_password)
    );
    $result = $_GB->_DB->insert('admins', $adminData);
    if (!$result) {
        echo $_GB->ErrorDisplay('Installation Failed  please insert information', 'no');
    } else {
        echo $_GB->ErrorDisplay('Installation Completed  successfully', 'yes');
        header("Refresh: 1; url=../../admin/login.php");
    }


}
?>


<form class="card-settings mdl-shadow--4dp" action="" method="POST">


    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" name="base_url" id="base_url" required>
        <label class="mdl-textfield__label" for="base_url">Base URL</label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text"
               name="app_name" id="app_name" required>
        <label class="mdl-textfield__label" for="app_name">Application Name Ex:WhatsClone</label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text"
               name="admin_name" id="admin_name" required>
        <label class="mdl-textfield__label" for="admin_name">Admin Name </label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text"
               name="admin_password" id="admin_password" required>
        <label class="mdl-textfield__label" for="admin_password">Password</label>
    </div>
    <center>
        <button type="submit"
                class="mdl-button mdl-js-button mdl-button--fab mdl-js-ripple-effect mdl-button--colored ben-cherif--badge--color-green fab-card-settings">
            <i class="material-icons mdl-color-text--white">arrow_forward</i></button>
    </center>
</form>
<div class="ben-cherif--badge--color-green   mdl-shadow--4dp " style="height: 250px;text-align: center">
    <a class="ben-cherif--badge--rounded">Step 2</a>

    <p class=" mdl-card_installation-description "> Welcome please fill in this information (This information can
        change later when you login to your account) </p>
</div>

</body>

<footer>
    <!--  Scripts-->
    <script src="../../admin/material/material.js"></script>
</footer>
</html>
