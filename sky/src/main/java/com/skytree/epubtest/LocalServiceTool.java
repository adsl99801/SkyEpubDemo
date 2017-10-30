package com.skytree.epubtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.skytree.epub.BookInformation;
import com.skytree.epub.SkyProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by keith on 2017/10/25.
 */

public class LocalServiceTool {
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BookInformation getBookInformation( String fileName, String baseDirectory, String coverPath) {
        debug(fileName);

        BookInformation bi = new BookInformation();
        // SkyProvider is the default epub file handler since 5.0.
        SkyProvider skyProvider = new SkyProvider();
        bi = new BookInformation();
        bi.setFileName(fileName);
        bi.setBaseDirectory(baseDirectory);
        bi.setContentProvider(skyProvider);
        File coverFile = new File(coverPath);
        if (!coverFile.exists()) bi.setCoverPath(coverPath);
        skyProvider.setBook(bi.getBook());
//        skyProvider.setKeyListener(localService.new KeyDelegate());
        bi.makeInformation();
        return bi;
    }

    public static void deleteBookByBookCode(int bookCode) {
        String targetName = SkyApplicationHolder.sd.getFileNameByBookCode(bookCode);
        String filePath = new String(SkySetting.getStorageDirectory( ) + "/books/" + targetName);
        String targetDir = SkyUtility.removeExtention(filePath);
        String coverPath = SkyApplicationHolder.sd.getCoverPathByBookCode(bookCode);
        coverPath.replace(".epub", ".jpg");
        SkyApplicationHolder.sd.deleteRecursive(new File(targetDir));
        filePath = new String(SkySetting.getStorageDirectory( ) + "/downloads/" + targetName);
        SkyApplicationHolder.sd.deleteRecursive(new File(filePath));
        SkyApplicationHolder.sd.deleteRecursive(new File(coverPath));
        SkyApplicationHolder.sd.deleteBookByBookCode(bookCode);
        SkyApplicationHolder.sd.deleteBookmarksByBookCode(bookCode);
        SkyApplicationHolder.sd.deleteHighlightsByBookCode(bookCode);
        SkyApplicationHolder.sd.deletePagingsByBookCode(bookCode);
    }

    public static void deleteBook(LocalService localService, int bookCode) {
        deleteBookByBookCode(bookCode);
        localService.reloadBookInformations();
    }

    public static synchronized BookInformation installBook( String url) {
        debug("instalBook start");
        int bookCode = -1;
        try {
            String extension = SkyUtility.getFileExtension(url);
            if (!extension.contains("epub")) return null;
            String pureName = SkyUtility.getPureName(url);
            debug("instalBook start");
            bookCode = SkyApplicationHolder.sd.insertEmptyBook(url, "", "", "", 0);
            String targetName = SkyApplicationHolder.sd.getFileNameByBookCode(bookCode);
            copyBookToDevice(url, targetName);

            BookInformation bi;
            String coverPath = SkyApplicationHolder.sd.getCoverPathByBookCode(bookCode);
            String baseDirectory = SkySetting.getStorageDirectory() + "/books";

            bi = getBookInformation(targetName, baseDirectory, coverPath);
            bi.bookCode = bookCode;
            bi.title = pureName;
            bi.fileSize = -1;
            bi.downSize = -1;
            bi.isDownloaded = true;
            SkyApplicationHolder.sd.updateBook(bi);
            debug("instalBook ends");
            return bi;
        } catch (Exception e) {
            debug(e.getMessage());
            return null;
        }
    }
    public static void debug(String msg) {
        Log.d("EPub", msg);
    }

    public static synchronized boolean copyBookToDevice( String filePath, String targetName) {
        try {
            InputStream localInputStream = null;


            localInputStream = new FileInputStream(filePath);
            String bookDir = SkySetting.getStorageDirectory( ) + "/books";
            new File(bookDir).mkdirs();
            String path = bookDir + "/" + targetName;
            FileOutputStream localFileOutputStream = new FileOutputStream(path);
            byte[] arrayOfByte = new byte[1024];
            int offset;
            while ((offset = localInputStream.read(arrayOfByte)) > 0) {
                localFileOutputStream.write(arrayOfByte, 0, offset);
            }
            localFileOutputStream.flush();
            localFileOutputStream.close();
            localInputStream.close();
            return true;
        } catch (IOException localIOException) {
            localIOException.printStackTrace();
        }
        return false;
    }

    public static void openBookViewer(Context context, BookInformation bi, boolean fromBeginning) {
        Intent intent;
        if (!bi.isFixedLayout) {
            intent = new Intent(context, BookViewActivity.class);
        } else {
            intent = new Intent(context, MagazineActivity.class);
        }
        intent.putExtra("BOOKCODE", bi.bookCode);
        intent.putExtra("TITLE", bi.title);
        intent.putExtra("AUTHOR", bi.creator);
        intent.putExtra("BOOKNAME", bi.fileName);
        if (fromBeginning || bi.position < 0.0f) {
            intent.putExtra("POSITION", (double) -1.0f); // 7.x -1 stands for start position for both LTR and RTL book.
        } else {
            intent.putExtra("POSITION", bi.position);
        }
        intent.putExtra("THEMEINDEX", SkyApplicationHolder.setting.theme);
        intent.putExtra("DOUBLEPAGED", SkyApplicationHolder.setting.doublePaged);
        intent.putExtra("transitionType", SkyApplicationHolder.setting.transitionType);
        intent.putExtra("GLOBALPAGINATION", SkyApplicationHolder.setting.globalPagination);
        intent.putExtra("RTL", bi.isRTL);
        intent.putExtra("VERTICALWRITING", bi.isVerticalWriting);

        intent.putExtra("SPREAD", bi.spread);
        intent.putExtra("ORIENTATION", bi.orientation);

        context.startActivity(intent);
    }
}
