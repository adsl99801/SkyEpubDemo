package com.tcf.epub

import android.util.Log

/**
 * Created by keith on 2017/10/25.
 */
class EpubCommontUtils private constructor(){
    init {
    }

    private object Handler {
        val INSTANCE = EpubCommontUtils()
    }

    companion object {
        val instance: EpubCommontUtils by lazy { Handler.INSTANCE }
    }
    val Debug=true
    fun log(msg:String){
        Log.i("EpubCommontUtils",""+msg)
    }
}
