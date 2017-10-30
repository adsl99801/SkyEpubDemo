package com.tcf.epub

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.skytree.epub.BookInformation
import com.skytree.epubtest.LocalServiceTool
import com.skytree.epubtest.SkyApplicationHolder
import java.io.File

/**
 * Created by keith on 2017/10/25.
 */
class MainActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val app = SkyApplicationHolder()
        val context = this
        app.init(context)

        val path = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                + File.separator + "books" + File.separator + "2017.epub")
        if (!File(path).exists()) {
            LocalServiceTool.debug("!new File(path).exists()")
            return
        }

        val bi = LocalServiceTool.installBook(path) as BookInformation

        LocalServiceTool.openBookViewer(context, bi, true)
    }
}
