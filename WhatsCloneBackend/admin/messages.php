<?php
/**
 * Created by PhpStorm.
 * User: abderrahimelimame
 * Date: 8/8/16
 * Time: 00:25
 */

include 'header.php';
if ($_GB->getSession('admin') == false) {
    header("location:login.php");
}
?>

<div class="ben-cherif-users-card-square-header mdl-card mdl-shadow--2dp">
    <?php
    if (isset($_GET['cmd']) && $_GET['cmd'] == 'messages') {
        ?>
        <div class="card-display-error-success  mdl-shadow--4dp ">
            <p class=" mdl-card__title__pages">List of messages</p>
        </div>
        <center>
            <table class="mdl-data-table ben-cherif-tables-messages mdl-js-data-table  mdl-shadow--2dp ">
                <thead>
                <tr>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Message</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Media</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Media actions</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">isGroup</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">From</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">To</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Date</th>
                    <th style="text-align:center;   color: #0073b7 !important; font-size: 15px;">Actions</th>
                </tr>
                </thead>
                <tbody>
                <?php
                $rows = $_DB->CountRows('messages');
                $page = (isset($_GET['page']) && !empty($_GET['page'])) ? $Security->MA_INT($_GET['page']) : 1;
                $_PAG = new Pagination($page, $rows, 20, 'messages.php?cmd=messages&page=#i#');
                $query = "SELECT M.id,
                            M.Date AS date,
                            M.message ,
                            M.image AS imageFile,
                            M.video AS videoFile,
                            M.thumbnail AS thumbnailFile,
                            M.document AS documentFile,
                            M.audio AS audioFile,
                            M.ConversationID AS conversationID  ,
                           C.recipient  AS recipientID,
                           C.sender AS senderID,
                            M.UserID AS sender,
                           M.groupID 
                  FROM  prefix_messages M

                  LEFT JOIN  prefix_users AS U
                  ON U.id = M.UserID

                  LEFT JOIN  prefix_conversations C
                  ON C.id = M.ConversationID
                  
                    GROUP BY M.id ORDER BY M.id DESC LIMIT  {$_PAG->limit}";

                $query = $_DB->MySQL_Query($query);
                while ($fetch = $_DB->fetchAssoc($query)) {
                    $message = $fetch['message'];

                    if ($fetch['groupID'] != 0) {
                        $isGroup = true;
                    } else {
                        $isGroup = false;
                    }

                    if ($isGroup){

                        $recipient = 0;
                        if ($fetch['sender'] != 0 )
                            $from = $Groups->getUserNameByID($fetch['sender']);
                        else
                            $from = "** Empty **";

                        if ($recipient != 0)
                            $to = $Groups->getUserNameByID($recipient);
                        else
                            $to = "** Empty **";
                    }else{

                        $recipient = 0;
                        $sender = 0;

                        if ($fetch['senderID'] == $fetch['sender']){
                            $recipient = $fetch['recipientID'];
                            $sender = $fetch['senderID'];
                        }else{

                            $recipient = $fetch['senderID'];
                            $sender = $fetch['recipientID'];
                        }

                        if ($sender != 0 )
                            $from = $Groups->getUserNameByID($sender);
                        else
                            $from = "** Empty **";

                        if ($recipient != 0)
                            $to = $Groups->getUserNameByID($recipient);
                        else
                            $to = "** Empty **";
                    }
                    $date = $fetch['date'];


                    echo '<tr>';
                    echo ' <td class="mdl-data-table__cell--non-numeric">';
                    if ($message != null)
                        echo $message;
                    else
                        echo '<center><div class="ben-cherif--color-red-warning">** Empty message **</div></center>';
                    echo '</td>';
                    echo '<td>';
                    if ($fetch['imageFile'] != null && $fetch['imageFile'] != "null") {
                        ?>
                        <center><img class="ben-cherif-data--image "
                                     src="../<?php echo $fetch['imageFile']; ?>">
                        </center>
                        <?php
                    } else if ($fetch['videoFile'] != null && $fetch['videoFile'] != "null") {
                        ?>
                        <video class="ben-cherif-data--image " src="../<?php echo $fetch['videoFile']; ?>" controls
                               poster="../<?php echo $fetch['thumbnailFile']; ?>">
                        </video>

                    <?php } else
                        if ($fetch['documentFile'] != null && $fetch['documentFile'] != "null") {
                            ?>
                            <center>
                                <object class="ben-cherif-data--image ">
                                    <embed src="../<?php echo $fetch['documentFile']; ?>"></embed>
                                </object>
                            </center>
                            <?php
                        } else if ($fetch['audioFile'] != null && $fetch['audioFile'] != "null") {
                            ?>
                            <center>
                                <audio controls>
                                    <source src="../<?php echo $fetch['audioFile']; ?>"
                                            type="audio/mpeg">

                                </audio>
                            </center>
                            <?php
                        } else {
                            echo '<center><div class="ben-cherif--color-red-warning">**  No media  **</div></center>';
                        }
                    echo '</td>';
                    echo '<td>';
                    if ($fetch['imageFile'] != null && $fetch['imageFile'] != "null") {
                        echo ' <center><a type="button"  href="../' . $fetch['imageFile'] . '"  class="mdl-button mdl-js-button mdl-button--raised ben-cherif--badge--color-green ben-cherif-tables-buttons" download> Download </a> </center>';
                    } else if ($fetch['videoFile'] != null && $fetch['videoFile'] != "null") {
                        echo ' <center><a type="button"  href="../' . $fetch['videoFile'] . '"  class="mdl-button mdl-js-button mdl-button--raised ben-cherif--badge--color-green ben-cherif-tables-buttons" download> Download </a> </center>';
                    } else if ($fetch['documentFile'] != null && $fetch['documentFile'] != "null") {
                        echo ' <center><a type="button"  href="../' . $fetch['documentFile'] . '"  class="mdl-button mdl-js-button mdl-button--raised ben-cherif--badge--color-green ben-cherif-tables-buttons" download> Download </a> </center>';
                    } else if ($fetch['audioFile'] != null && $fetch['audioFile'] != "null") {
                        echo ' <center><a type="button"  href="../' . $fetch['audioFile'] . '"  class="mdl-button mdl-js-button mdl-button--raised ben-cherif--badge--color-green ben-cherif-tables-buttons" download> Download </a> </center>';
                    }
                    echo '</td>';
                    echo '<td>';
                    if ($isGroup){
                        echo '<center><div class="ben-cherif--color-green-warning">** True **</div></center>';
                    }else{
                        echo '<center><div class="ben-cherif--color-red-warning">** False **</div></center>';
                    }
                    echo '</td>';
                    echo '<td>';
                    echo $from;
                    echo '</td>';
                    echo '<td>';
                    echo $to;
                    echo '</td>';
                    echo '<td>';
                    echo $date;
                    echo '</td>';
                    echo '<td>';
                    echo '<a type="button"  href="messages.php?cmd=deleteMessage&id=' . $fetch['id'] . '" onclick="return checkDelete()"  class="mdl-button mdl-js-button mdl-button--raised ben-cherif--badge--color-red ben-cherif-tables-buttons"> Delete </a>';
                    echo '</td>';
                    echo '</tr>';
                } ?>
                </tbody>
            </table>
        </center>
        <?php
    } else if (isset($_GET['cmd'], $_GET['id']) && $_GET['cmd'] == 'deleteMessage') {
        $id = $_DB->escapeString($_GET['id']);
        $delete = $_DB->delete('messages', '`id` = ' . $id);
        if ($delete) {
            echo $_GB->ErrorDisplay('The message Deleted successfully', 'yes');
            echo $_GB->refreshPage('messages.php?cmd=messages', 1);
        } else {
            echo $_GB->ErrorDisplay('Failed to delete this message ,please try again later');
            echo $_GB->refreshPage('messages.php?cmd=messages', 1);
        }
    } ?>
</div>
<?php

echo $_PAG->urls;
include 'footer.php'
?>
