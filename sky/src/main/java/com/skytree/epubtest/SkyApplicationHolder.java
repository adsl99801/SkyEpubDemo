package com.skytree.epubtest;

import android.content.Context;

import com.skytree.epub.BookInformation;
import com.skytree.epub.SkyKeyManager;

import java.util.ArrayList;

public class SkyApplicationHolder {
    public static ArrayList<BookInformation> bis;
    public static ArrayList<CustomFont> customFonts = new ArrayList<CustomFont>();
    public static SkySetting setting;
    public static SkyDatabase sd = null;
    public static int sortType = 0;
    public static SkyKeyManager keyManager;

    public static void reloadBookInformations() {
        bis = sd.fetchBookInformations(sortType, "");
    }

    public static void reloadBookInformations(String key) {
        bis = sd.fetchBookInformations(sortType, key);
    }

    public void init(Context context) {
//        if (SkySetting.getStorageDirectory() == null) {
//			 All book related data will be stored /data/data/com....../files/appName/
//            SkySetting.setStorageDirectory(getFilesDir().getAbsolutePath(), appName);
        // All book related data will be stored /sdcard/appName/...
//			SkySetting.setStorageDirectory(Environment.getExternalStorageDirectory().getAbsolutePath(),appName);
//        }

        SkySetting.setFilesDir(context.getFilesDir().getAbsolutePath() );
        sd = new SkyDatabase(context);
        reloadBookInformations();
        loadSetting();
        createSkyDRM();
    }

    public static void loadSetting() {
        setting = sd.fetchSetting();
    }

    public static void saveSetting() {
        sd.updateSetting(setting);
    }

    public static void createSkyDRM() {
        keyManager = new SkyKeyManager("A3UBZzJNCoXmXQlBWD4xNo", "zfZl40AQXu8xHTGKMRwG69");
    }
}
