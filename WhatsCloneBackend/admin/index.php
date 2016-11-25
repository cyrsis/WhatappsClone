<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 7/8/16
 * Time: 02:00
 */
include 'header.php';
if ($_GB->getSession('admin') == false) {
    header("location:login.php");
}
?>
<div class="ben-cherif-users-card-square-header mdl-card mdl-shadow--2dp">
    <?php
    $totalUsers = $_DB->CountRows('users');
    $totalGroups = $_DB->CountRows('groups');
    $totalMessages = $_DB->CountRows('messages'); ?>
    <div class="mdl-grid">
        <div class="mdl-cell mdl-cell--4-col">
            <div class="ben-cherif-users-card-square-header-elements mdl-card mdl-shadow--2dp">
                <div class="ben-cherif--badge--color-orange ben-cherif-titles-counters-body">
                    <center><span class="ben-cherif-titles ">Total Users </span></center>
                </div>
                <div class="mdl-card__actions mdl-card--border">
                    <center><span class="ben-cherif-counter"><?php echo $totalUsers ?></span></center>
                </div>
            </div>
        </div>

        <div class="mdl-cell mdl-cell--4-col">
            <div class="ben-cherif-users-card-square-header-elements mdl-card mdl-shadow--2dp">
                <div class="ben-cherif--badge--color-green ben-cherif-titles-counters-body">
                    <center><span class="ben-cherif-titles ">Total Groups </span></center>
                </div>
                <div class="mdl-card__actions mdl-card--border">
                    <center><span class="ben-cherif-counter"><?php echo $totalGroups ?></span></center>
                </div>
            </div>
        </div>

        <div class="mdl-cell mdl-cell--4-col">
            <div class="ben-cherif-users-card-square-header-elements mdl-card mdl-shadow--2dp">
                <div class="ben-cherif--badge--color-red ben-cherif-titles-counters-body">
                    <center><span class="ben-cherif-titles ">Total Messages</span></center>
                </div>
                <div class="mdl-card__actions mdl-card--border">
                    <center><span class="ben-cherif-counter"><?php echo $totalMessages ?></span></center>
                </div>
            </div>
        </div>
        <div class="mdl-cell  mdl-cell--4-col ">
            <div class="ben-cherif-users-card-square mdl-card mdl-shadow--2dp">
                <div class="ben-cherif--badge--color-red-indicator"></div>
                <strong style="text-align:center;   color: black !important; font-size: 20px; padding: 2%">Last new
                    users</strong>

                <center>
                    <div class="mdl-card__actions mdl-card--border">
                        <div class="mdl-grid">
                            <?php
                            $query = $_DB->select('users', '*', '', '`id` DESC', 6);
                            while ($fetch = $_DB->fetchAssoc($query)) {
                                $username = $fetch['username'];
                                $userImage = $fetch['image'];
                                echo '<div class="mdl-cell mdl-cell--4-col">';
                                //echo '<div class=" ben-cherif-user-card-image mdl-shadow--2dp">';
                                echo '<center>';
                                if ($userImage != null) { ?>
                                    <img class="img-user-rounded ben-cherif-dashboard_users--image "
                                         src="../<?php echo $_GB->getSafeImage($userImage); ?>">
                                <?php } else { ?>
                                    <img class="img-user-rounded ben-cherif-dashboard_users--image "
                                         src="../uploads/logo.png">
                                    <?php
                                }
                                echo '</center>';
                                echo '<div class="mdl-card__actions">';
                                echo '<span   class="ben-cherif-user-card-image__filename">';
                                if ($username == null) {
                                    echo $fetch['phone'];
                                } else {
                                    echo $fetch['username'];
                                }
                                echo '</span>';
                                echo '</div>';
                                // echo '</div>';
                                echo '<div class="ben-cherif--badge--color-blue-indicator-with-margins"></div>';
                                echo '</div>';
                            } ?>
                        </div>
                    </div>
                </center>
                <div class="mdl-card__actions mdl-card--border">
                    <center><a
                            class="mdl-button mdl-button--colored mdl-js-button mdl-js-ripple-effect mdl-color-text--red"
                            href="users.php?cmd=users">
                            View All users</a></center>
                </div>

                <div class="ben-cherif--badge--color-red-indicator"></div>
            </div>
        </div>

        <div class="mdl-cell   mdl-cell--4-col">
            <div class="ben-cherif-countries-card-square mdl-card mdl-shadow--2dp">
                <div class="ben-cherif--badge--color-green-indicator"></div>
                <strong style="text-align:center;   color: black !important; font-size: 20px; padding: 2%">Users by
                    countries</strong>

                <center>
                    <div class="mdl-card__actions mdl-card--border">
                        <div id="regions_div" class="ben-cherif-countries-chart-card-square">
                            <div class="mdl-spinner mdl-js-spinner is-active"></div>
                        </div>
                    </div>
                </center>
                <div class="ben-cherif--badge--color-green-indicator"></div>
            </div>
        </div>

    </div>

</div>


<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<?php
$query = $_DB->selectDistinct('users', 'country', '', '`id` DESC');
$countries = array();
$userNumber = array();

while ($fetch = $_DB->fetchAssoc($query)) {
    $fetch['country'] = (empty($fetch['country'])) ? null : $fetch['country'];
    $fetch['userCounter'] = $_DB->CountRows('users', "`country`= '{$fetch['country']}'");
    array_push($countries, $fetch['country']);
    array_push($userNumber, $fetch['userCounter']);
}
$countriesData = array(['Country', 'Popularity']);
foreach ($countries as $k => $v) {
    $countriesData[] = array($v, $userNumber[$k]);
}
?>
<script>
    google.charts.load('current', {'packages': ['geochart']});
    google.charts.setOnLoadCallback(drawRegionsMap);

    function drawRegionsMap() {

        var data = google.visualization.arrayToDataTable(<?php echo json_encode($countriesData)?>);

        var options = {
            colorAxis: {
                colors: ['#00a65a', '#f39c12', '#dd4b39'],
                minValue: 0,
                maxValue: 2
            }
        };

        var chart = new google.visualization.GeoChart(document.getElementById('regions_div'));

        chart.draw(data, options);
    }
</script>
<?php
include 'footer.php';
?>

