package com.tcf.epub

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.skytree.epubtest.HomeActivity

/**
 * Created by keith on 2017/10/25.
 */
class MainActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = com.skytree.epubtest.SkyApplicationHolder()
        val context = this
        app.init(context)
        startActivity(Intent(this, HomeActivity::class.java))
    }
}
