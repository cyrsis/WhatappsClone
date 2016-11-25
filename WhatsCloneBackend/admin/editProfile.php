<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/11/16
 * Time: 03:08
 */
include 'header.php';

if ($_GB->getSession('admin') == false) {
    header("location:login.php");
}
?>
<?php

$userID = $_GB->getSession('admin');
$query = $_DB->select('admins', '*', '`id`=' . $userID);
$fetch = $_DB->fetchAssoc($query);
$admin_name = $fetch['username'];
$admin_image = $fetch['image'];
$oldPassword = $fetch['password'];


if (isset($_POST['admin_name'])) {

    foreach ($_POST as $key => $value) {
        $_POST[$key] = $_DB->escapeString(trim($value));
    }

    if (md5($_POST['old_admin_password']) != $oldPassword) {
        echo $_GB->ErrorDisplay('Your old password is not correct');
    } else {

        if (isset($_FILES['input_admin_image'])) {
            $imageHash = $_GB->uploadAdminImage($_FILES['input_admin_image']);
        } else {
            $imageHash = null;
        }

        $fields = "`username` = '" . $_POST['admin_name'] . "'";
        if (!empty($_POST['admin_password'])) {
            $fields .= ",`password` = '" . md5($_POST['admin_password']) . "'";
        }
        $fields .= ",`image` ='" . $imageHash . "'";
        $update = $_DB->update('admins', $fields, "`id` = {$userID}");
        if ($update) {
            echo $_GB->ErrorDisplay('Your information are updated successfully', 'yes');
            echo $_GB->refreshPage('editProfile.php', 1);
        } else {
            echo $_GB->ErrorDisplay('Failed to update your information', 'no');
        }
    }
}

?>
<?php
if ($admin_image != null) {
?>

<form class="card-settings mdl-shadow--4dp" action="" method="POST" enctype="multipart/form-data">

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <img class="ben-cherif-dashboard_users--image "
             src="../<?php echo $_GB->getSafeImage($admin_image); ?>">
        <input class="mdl-textfield__input" type='file' id="input_admin_image" name="input_admin_image"/>

    </div>


    <?php
    } else {
    ?>

    <form class="card-settings mdl-shadow--4dp" action="" method="POST" enctype="multipart/form-data">

        <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
            <img class="ben-cherif-dashboard_users--image "
                 src="../uploads/logo.png">
            <input class="mdl-textfield__input" type='file' id="input_admin_image" name="input_admin_image"/>

        </div>
        <?php
        }
        ?>


        <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
            <input class="mdl-textfield__input" type="text" name="admin_name" id="admin_name"
                   value="<?php echo $admin_name ?>">
            <label class="mdl-textfield__label" for="admin_name">Admin nameL</label>
        </div>


        <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
            <input class="mdl-textfield__input" type="text"
                   name="old_admin_password" id="old_admin_password" required>
            <label class="mdl-textfield__label" for="old_admin_password">Old Admin password</label>
        </div>
        <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
            <input class="mdl-textfield__input" type="text"
                   name="admin_password" id="admin_password" required>
            <label class="mdl-textfield__label" for="admin_password">Admin password</label>
        </div>


        <center>
            <button type="submit"
                    class="mdl-button mdl-js-button mdl-button--fab mdl-js-ripple-effect mdl-button--colored ben-cherif--badge--color-green fab-card-settings">
                <i class="material-icons mdl-color-text--white">done</i></button>
        </center>
    </form>

    <?php
    include "footer.php";
    ?>
