<?php
    $escapedFragment = $_GET['_escaped_fragment_'];

    if (isset($escapedFragment) && !empty($escapedFragment)) {

        $content = file_get_contents('partials/'.$escapedFragment.'.html');

        if ($content != false) {
            echo '<!DOCTYPE html><html lang="en"><head></head><body>';
            echo $content;
            echo '</body></html>';
        }
        else {
            header("HTTP/1.0 404 Not Found");
        }
    }
    else {
        include('main.html');
    }
?>