<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 7/8/16
 * Time: 02:00
 */


include 'header.php';

if ($_GB->getSession('admin') != false) {
    header("location:index.php");
}
?>
<?php
if (isset($_POST['username'], $_POST['password'])) {
    $Users->adminLogin($_POST['username'], $_POST['password']);
}
?>
<div class="card-login mdl-card mdl-shadow--2dp ">
    <div class="mdl-card__title">
        <h2 class="mdl-card__title-text  mdl-color-text--primary-dark">Welcome</h2>
    </div>
    <form action="" method="POST">
        <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label  input-card-login">
            <input class="mdl-textfield__input" type="text" name="username" id="username">
            <label class="mdl-textfield__label" for="username">Username</label>
        </div>
        <label>
            <i class="material-icons  mdl-color-text--primary ">face</i>
        </label>
        <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label input-card-login">
            <input class="mdl-textfield__input" type="password" name="password" id="password">
            <label class="mdl-textfield__label" for="password">Password</label>
        </div>
        <label>
            <i class="material-icons mdl-color-text--primary">lock</i>
        </label>


        <button type="submit"
                class="mdl-button mdl-js-button mdl-button--fab mdl-js-ripple-effect mdl-button--colored fab-card-login">
            <i class="material-icons mdl-color-text--white">arrow_forward</i>
        </button>
    </form>

</div>
<?php
include 'footer.php';
?>
