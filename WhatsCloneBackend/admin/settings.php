<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/10/16
 * Time: 03:23
 */
include 'header.php';
if ($_GB->getSession('admin') == false) {
    header("location:login.php");
}
?>

<div class="ben-cherif--badge--color-green  mdl-shadow--4dp ">
    <p class=" mdl-card_settings-description">Customization & control </p>
</div>
<form class="card-settings mdl-shadow--4dp" action="" method="POST">
    <div class="mdl-textfield mdl-js-textfield input-card-settings">
        <textarea class="mdl-textfield__input" type="text" rows="2" name="privacy_policy"
                  id="privacy_policy"><?php echo htmlentities($_GB->getSettings('privacy_policy')); ?></textarea>
        <label class="mdl-textfield__label" for="privacy_policy">Privacy Policy</label>
    </div>
    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" name="base_url" id="base_url"
               value="<?php echo htmlentities($_GB->getSettings('base_url')); ?>">
        <label class="mdl-textfield__label" for="base_url">Base URL</label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" value="<?php echo $_GB->getSettings('app_name'); ?>"
               name="app_name" id="app_name">
        <label class="mdl-textfield__label" for="app_name">Application Name Ex:WhatsClone</label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text"
               value="<?php echo $_GB->getSettings('sms_authentication_key'); ?>" name="sms_authentication_key"
               id="sms_authentication_key">
        <label class="mdl-textfield__label" for="sms_authentication_key">SMS Authentication Key</label>
    </div>

    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" maxlength="6" type="text" value="<?php echo $_GB->getSettings('sms_sender'); ?>"
               name="sms_sender" id="sms_sender">
        <label class="mdl-textfield__label" for="sms_sender">SMS provider Sender Name (should be 6 characters long)</label>
    </div>
    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" value="<?php echo $_GB->getSettings('sms_username'); ?>"
               name="sms_username" id="sms_username">
        <label class="mdl-textfield__label" for="sms_username">SMS provider username</label>
    </div>
    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" value="<?php echo $_GB->getSettings('sms_password'); ?>"
               name="sms_password" id="sms_password">
        <label class="mdl-textfield__label" for="sms_password">SMS provider password</label>
    </div>
    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-settings">
        <input class="mdl-textfield__input" type="text" name="sms_api_url"
               value="<?php echo htmlentities($_GB->getSettings('sms_api_url')); ?>" id="sms_api_url">
        <label class="mdl-textfield__label" for="sms_api_url">SMS provider API URL</label>
    </div>
    <center>
        <button type="submit"
                class="mdl-button mdl-js-button mdl-button--fab mdl-js-ripple-effect mdl-button--colored ben-cherif--badge--color-green fab-card-settings">
            <i class="material-icons mdl-color-text--white">done</i></button>
    </center>
</form>


<?php
if (isset($_POST['sms_api_url'])) {
    $privacy_policy = $_POST['privacy_policy'];
    $base_url = $_POST['base_url'];
    $app_name = $_POST['app_name'];
    $sms_sender = $_POST['sms_sender'];
    $sms_authentication_key = $_POST['sms_authentication_key'];
    $sms_username = $_POST['sms_username'];
    $sms_password = $_POST['sms_password'];
    $sms_api_url = $_POST['sms_api_url'];

    $_GB->updateSettings("privacy_policy", $privacy_policy);
    $_GB->updateSettings("base_url", $base_url);
    $_GB->updateSettings("app_name", $app_name);
    $_GB->updateSettings("sms_sender", $sms_sender);
    $_GB->updateSettings("sms_authentication_key", $sms_authentication_key);
    $_GB->updateSettings("sms_username", $sms_username);
    $_GB->updateSettings("sms_password", $sms_password);
    $_GB->updateSettings("sms_api_url", $sms_api_url);

    echo $_GB->ErrorDisplay('Settings updated successfully', 'yes');
    header("Refresh: 1; url=settings.php");

}

include 'footer.php';
?>
