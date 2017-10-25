package com.skytree.epubtest;

import android.content.Context;

public class SkySetting {
    public int bookCode;
    public String fontName;
    public int fontSize;
    public int lineSpacing;
    public int foreground;
    public int background;
    public int theme;
    public double brightness;
    public int transitionType;
    public boolean lockRotation;
    public boolean doublePaged;
    public boolean allow3G;
    public boolean globalPagination;
    public boolean mediaOverlay;
    public boolean tts;
    public boolean autoStartPlaying;
    public boolean autoLoadNewChapter;
    public boolean highlightTextToVoice;
    private static String FilesDir="";

    public String getFilesDir() {
        return FilesDir;
    }

    public static void setFilesDir(String filesDir) {
        FilesDir = filesDir;
    }

    public  static String getStorageDirectory() {
        return FilesDir;
    }

}
