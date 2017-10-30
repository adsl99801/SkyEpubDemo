package com.skytree.epubtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.skytree.epub.BookInformation;
import com.skytree.epub.KeyListener;

import java.io.File;

import static com.skytree.epub.Setting.debug;

/**
 * Created by keith on 2017/10/25.
 */

@Deprecated
public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SkyApplicationHolder app = new SkyApplicationHolder();
        Context context = this;
        app.init(context);

        String path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                + File.separator+"books"+File.separator+"2017.epub";
        if(!new File(path).exists()){
            LocalServiceTool.debug("!new File(path).exists()");
            return;
        }

        BookInformation bi = LocalServiceTool.installBook(path);
        LocalServiceTool.openBookViewer(context,bi,true);


    }






}
