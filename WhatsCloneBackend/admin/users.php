<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/7/16
 * Time: 23:38
 */
include 'header.php';
if ($_GB->getSession('admin') == false) {
    header("location:login.php");
}
?>

<div class="ben-cherif-users-card-square-header mdl-card mdl-shadow--2dp">
    <?php
    if (isset($_GET['cmd']) && $_GET['cmd'] == 'users') {
        ?>

        <div class="card-display-error-success  mdl-shadow--4dp ">
            <p class=" mdl-card__title__pages">List of users</p>
        </div>
        <center>
            <table class="mdl-data-table ben-cherif-tables mdl-js-data-table  mdl-shadow--2dp ">
                <thead>
                <tr>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Username</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Phone</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Avatar</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Country</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Actions</th>
                </tr>
                </thead>
                <tbody>
                <?php
                $rows = $_DB->CountRows('users');
                $page = (isset($_GET['page']) && !empty($_GET['page'])) ? $Security->MA_INT($_GET['page']) : 1;
                $_PAG = new Pagination($page, $rows, 20, 'users.php?cmd=users&page=#i#');
                $query = $_DB->select('users', '*', '', '`id` DESC', $_PAG->limit);
                while ($fetch = $_DB->fetchAssoc($query)) {
                    $username = $fetch['username'];
                    echo '<tr>';
                    echo '<td class="mdl-data-table__cell--non-numeric">';
                    if ($username == null) {
                        echo '<center><div class="ben-cherif--color-red-warning">** No username **</div></center>';
                    } else {
                        echo $fetch['username'];
                    }
                    echo '</td>';
                    echo '<td>';
                    echo $fetch['phone'];
                    echo '</td>';
                    echo '<td>';
                    $userImage = $fetch['image'];
                    if ($userImage != null ) {
                        ?>
                        <center><img class="img-rounded ben-cherif-navigation-drawer--image "   src="../<?php echo $_GB->getSafeImage($userImage); ?>"></center>
                        <?php
                    }else{
                        ?>
                        <center><img class="img-rounded ben-cherif-navigation-drawer--image "
                                     src="../uploads/logo.png"></center>
                        <?php
                    }
                    echo '</td>';
                    echo '<td>';
                    echo $fetch['country'];
                    echo '</td>';
                    echo '<td>';
                    echo '<a type="button"  href="users.php?cmd=deleteUser&id=' . $fetch['id'] . '" onclick="return checkDelete()"  class="mdl-button mdl-js-button mdl-button--raised ben-cherif--badge--color-red ben-cherif-tables-buttons"> Delete </a>';
                    echo ' </td > ';
                    echo '</tr > ';
                } ?>
                </tbody>
            </table>
        </center>
        <?php
    } else if (isset($_GET['cmd'], $_GET['id']) && $_GET['cmd'] == 'deleteUser') {
        $id = $_DB->escapeString($_GET['id']);
        $delete = $_DB->delete('users', '`id` = ' . $id);
        if ($delete) {
            echo $_GB->ErrorDisplay('The user Deleted successfully', 'yes');
            echo $_GB->refreshPage('users.php?cmd=users', 1);
        } else {
            echo $_GB->ErrorDisplay('Failed to delete this user ,please try again later');
            echo $_GB->refreshPage('users.php?cmd=users', 1);
        }
    } ?>

</div>

<?php
echo $_PAG->urls;
include 'footer.php'
?>
