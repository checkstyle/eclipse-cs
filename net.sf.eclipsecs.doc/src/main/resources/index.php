<?php
    $escapedFragment = $_GET['_escaped_fragment_'];

    if (isset($escapedFragment) && !empty($escapedFragment)) {

        if ($escapedFragment=='/' || $escapedFragment=='//') {
            $content = file_get_contents('partials/index.html');
            echo '<!DOCTYPE html><html lang="en"><head><meta charset="utf-8" /></head><body>';
            echo $content;
            echo '</body></html>';
        }
        else if ($escapedFragment=='/releasenotes') {

            $releases_content = file_get_contents('releases.json');
            $releases = json_decode($releases_content);

            echo '<!DOCTYPE html><html lang="en"><head><meta charset="utf-8" /></head><body>';
            $count = count($releases);
            for ($i = 0; $i < $count; $i++) {

                $release = $releases[$i];

                echo '<h1>'.$release->version.'</h1>';
                $content = file_get_contents($release->template);
                if ($content != false) {
                    echo $content;
                }
            }
            echo '</body></html>';
        }
        else {
            $content = file_get_contents('partials/'.$escapedFragment.'.html');

            if ($content != false) {
                echo '<!DOCTYPE html><html lang="en"><head><meta charset="utf-8" /></head><body>';
                echo $content;
                echo '</body></html>';
            }
            else {
                header("HTTP/1.0 404 Not Found");
            }
        }
    }
    else {
        include('main.html');
    }
?>