package com.skytree.epubtest;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.skytree.epub.Book;
import com.skytree.epub.BookInformation;
import com.skytree.epub.KeyListener;
import com.skytree.epub.SkyProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class LocalService extends Service {
    private final IBinder mBinder = new LocalBinder();

    // called after downloading is finished.
    private BroadcastReceiver completeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            Query query = new Query();
            query.setFilterById(downloadId);
            Cursor c = downloadManager.query(query);
            if (c == null) return;
            if (c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                    String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    int bytes_total = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    int bookCode = SkyApplicationHolder.sd.getBookCodeByFileName(title);

                    int fileUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                    String fileUri = c.getString(fileUriIdx);
                    if (fileUri != null) {
                        String sourceFile = Uri.parse(fileUri).getPath();
                        postDownload(bookCode, bytes_total, sourceFile);
                    }

                }
            }
        }
    };

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

    public void debug(String msg) {
        Log.d("EPub", msg);
    }

    public void onCreate() {
        super.onCreate();

        IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(completeReceiver, completeFilter);
        checkDownloads();
    }

    public void checkDownloads() {
        for (int i = 0; i < SkyApplicationHolder.bis.size(); i++) {
            BookInformation bi = SkyApplicationHolder.bis.get(i);
            if (!bi.isDownloaded) {
                this.deleteBookByBookCode(bi.bookCode);
                this.deleteFileByDownloadId(bi.res0);
//				this.deleteFileFromDownloads(bi.bookCode);
//				this.resumeDownload(bi);
            }
        }
        reloadBookInformations();
    }

    public boolean deleteFileFromDownloads(int bookCode) {
        boolean deleted = false;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = SkyApplicationHolder.sd.getFileNameByBookCode(bookCode);
        File file = new File(path, fileName);
        if (file.exists()) {
            deleted = file.delete();
        }
        return deleted;
    }

    public void deleteFileByDownloadId(long downloadId) {
        final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.remove(downloadId);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = super.onStartCommand(intent, flags, startId);
        return i;
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(completeReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public String getMessage(String msg) {
        return msg;
    }

    public boolean isBookExist(String url) {
        for (int i = 0; i < SkyApplicationHolder.bis.size(); i++) {
            BookInformation bi = SkyApplicationHolder.bis.get(i);
            if (bi.url.equalsIgnoreCase(url)) return true;
            if (bi.url.contains(url)) return true;
            if (url.contains(bi.url)) return true;
        }
        return false;
    }

    public int getBookCodeByURL(String url) {
        for (int i = 0; i < SkyApplicationHolder.bis.size(); i++) {
            BookInformation bi = SkyApplicationHolder.bis.get(i);
            if (bi.url.equalsIgnoreCase(url)) return bi.bookCode;
            if (bi.url.contains(url)) return bi.bookCode;
            if (url.contains(bi.url)) return bi.bookCode;
        }
        return -1;
    }

    public boolean isBookDownloaded(int bookCode) {
        for (int i = 0; i < SkyApplicationHolder.bis.size(); i++) {
            BookInformation bi = SkyApplicationHolder.bis.get(i);
            if (bi.bookCode == bookCode) {
                return bi.isDownloaded;
            }
        }
        return false;
    }

    public void reloadBookInformations() {
        SkyApplicationHolder.reloadBookInformations();
        this.sendReload();
    }

    public void reloadBookInformation(int bookCode) {
        SkyApplicationHolder.reloadBookInformations();
        this.sendReloadBook(bookCode);
    }

    public void sendProgress(int bookCode, int bytes_downloaded, int bytes_total, double percent) {
        BookInformation bi = new BookInformation();
        bi.bookCode = bookCode;
        bi.fileSize = bytes_total;
        bi.downSize = bytes_downloaded;
        SkyApplicationHolder.sd.updateDownloadProcess(bi);

        final String PROGRESS_INTENT = "com.skytree.android.intent.action.PROGRESS";
        Intent intent = new Intent(PROGRESS_INTENT);
        intent.putExtra("BOOKCODE", bookCode);
        intent.putExtra("BYTES_DONWLOADED", bytes_downloaded);
        intent.putExtra("BYTES_TOTAL", bytes_total);
        intent.putExtra("PERCENT", percent);

//		debug("Sender   BookCode:"+bookCode+" "+percent);
        this.sendBroadcast(intent);
    }

    public void sendReload() {
        final String RELOAD_INTENT = "com.skytree.android.intent.action.RELOAD";
        Intent intent = new Intent(RELOAD_INTENT);
        this.sendBroadcast(intent);
    }

    public void sendReloadBook(int bookCode) {
        final String RELOADBOOK_INTENT = "com.skytree.android.intent.action.RELOADBOOK";
        Intent intent = new Intent(RELOADBOOK_INTENT);
        intent.putExtra("BOOKCODE", bookCode);

        this.sendBroadcast(intent);
    }

    String fileNameEncode(String str) {
        String es = str.replace(" ", "%20");
        return es;
    }

    public void startDownload(String url, String coverUrl, String title, String author) {
        int bookCode = -1;
        try {
            bookCode = getBookCodeByURL(url);
            if (bookCode != -1) {
                if (this.isBookDownloaded(bookCode)) {
                    return;
                }
            }
            bookCode = SkyApplicationHolder.sd.insertEmptyBook(url, coverUrl, title, author, 0);
            String targetName = SkyApplicationHolder.sd.getFileNameByBookCode(bookCode);
            String targetCover = targetName.replace(".epub", ".jpg");
            if (!(coverUrl == null || coverUrl.isEmpty())) donwloadCover(coverUrl, targetCover);
            final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request;
            Uri urlToDownload = Uri.parse(this.fileNameEncode(url));
            request = new DownloadManager.Request(urlToDownload);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, targetName);
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
            final long downloadId = downloadManager.enqueue(request);
            request.setTitle(targetName);
            Timer downloadTimer = new Timer();
            downloadTimer.schedule(new DownloadTask(bookCode, downloadId), 0, 100);
            reloadBookInformations();
        } catch (Exception e) {
            deleteBook(bookCode);
            e.printStackTrace();
        }
    }

    public void resumeDownload(BookInformation bi) {
        int bookCode = bi.bookCode;
        try {
            String url = bi.url;
            String coverUrl = bi.coverUrl;
            String targetName = SkyApplicationHolder.sd.getFileNameByBookCode(bookCode);
            String targetCover = targetName.replace(".epub", ".jpg");
            if (!(coverUrl == null || coverUrl.isEmpty())) donwloadCover(coverUrl, targetCover);
            final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request;
            Uri urlToDownload = Uri.parse(this.fileNameEncode(url));
            request = new DownloadManager.Request(urlToDownload);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, targetName);
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
            final long downloadId = downloadManager.enqueue(request);
            request.setTitle(targetName);
            Timer downloadTimer = new Timer();
            downloadTimer.schedule(new DownloadTask(bookCode, downloadId), 0, 250);
            reloadBookInformations();
        } catch (Exception e) {
            deleteBook(bookCode);
            e.printStackTrace();
        }
    }

    public void donwloadCover(String url, String fileName) {
        try {
            String targetFile = new String(SkySetting.getStorageDirectory( ) + "/books/" + fileName);
            new DownloadCoverTask().execute(this.fileNameEncode(url), targetFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postDownload(int bookCode, int fileSize, String sourceFile) {
        BookInformation bi = new BookInformation();
        bi.bookCode = bookCode;
        bi.fileSize = fileSize;
        bi.downSize = fileSize;
        bi.isDownloaded = false;
        SkyApplicationHolder.sd.updateBook(bi);
        String destFolder = new String(SkySetting.getStorageDirectory( ) + "/books");
        File df = new File(destFolder);
        df.mkdir();
        String fileName = SkyApplicationHolder.sd.getFileNameByBookCode(bookCode);
        String targetFile = destFolder + "/" + SkyApplicationHolder.sd.getFileNameByBookCode(bookCode);
        SkyUtility.moveFile(sourceFile, targetFile);
        String coverPath = SkyApplicationHolder.sd.getCoverPathByBookCode(bookCode);
        String baseDirectory = SkySetting.getStorageDirectory( ) + "/books";
        sendProgress(bookCode, 0, 0, 0.9f);

        bi = getBookInformation(fileName, baseDirectory, coverPath);

        bi.bookCode = bookCode;
        bi.fileSize = -1;
        bi.downSize = -1;
        bi.isDownloaded = true;
        final BookInformation tbi = bi;
        SkyApplicationHolder.sd.updateBook(bi);
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                reloadBookInformation(tbi.bookCode);
            }
        }, 500);
    }

    public BookInformation getBookInformation(String fileName, String baseDirectory, String coverPath) {
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
        skyProvider.setKeyListener(new KeyDelegate());
        bi.makeInformation();
        return bi;
    }

    public void deleteBookByBookCode(int bookCode) {
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

    public void deleteCachedByBookCode(int bookCode) {
        String prefix = String.format("sb%d", bookCode);
        String cacheFolder = SkySetting.getStorageDirectory( ) + "/caches";
        File[] directory = new File(cacheFolder).listFiles();
        if (directory != null) {
            for (File file : directory) {
                if (file.getName().startsWith(prefix)) {
                    file.delete();
                }
            }
        }
    }

    public void deleteBook(int bookCode) {
        this.deleteBookByBookCode(bookCode);
        this.reloadBookInformations();
    }

    public void deleteAllBooks() {
        for (int i = 0; i < SkyApplicationHolder.bis.size(); i++) {
            BookInformation bi = SkyApplicationHolder.bis.get(i);
            this.deleteBookByBookCode(bi.bookCode);
        }
        this.reloadBookInformations();
    }

    private boolean checkStatus(Cursor cursor) {
        boolean ret = true;
        //column for status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //column for reason code if the download failed or paused
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);

        String statusText = "";
        String reasonText = "";

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                ret = false;
                statusText = "STATUS_FAILED";
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
                break;
        }

//		debug(statusText+"  "+reasonText);
        return ret;
    }

    public boolean isBookDownloaded(String url) {
        int bookCode = getBookCodeByURL(url);
        if (bookCode == -1) return false;

        for (int i = 0; i < SkyApplicationHolder.bis.size(); i++) {
            BookInformation bi = SkyApplicationHolder.bis.get(i);
            if (bi.bookCode == bookCode) {
                return bi.isDownloaded;
            }
        }
        return false;
    }

    public synchronized void installBook(String url) {
        debug("instalBook start");
        int bookCode = -1;
        try {
            if (isBookDownloaded(url)) return;
            String extension = SkyUtility.getFileExtension(url);
            if (!extension.contains("epub")) return;
            String pureName = SkyUtility.getPureName(url);
            debug("instalBook starts real");
            bookCode = SkyApplicationHolder.sd.insertEmptyBook(url, "", "", "", 0);
            String targetName = SkyApplicationHolder.sd.getFileNameByBookCode(bookCode);
            copyBookToDevice(url, targetName);

            BookInformation bi;
            String coverPath = SkyApplicationHolder.sd.getCoverPathByBookCode(bookCode);
            String baseDirectory = SkySetting.getStorageDirectory( ) + "/books";

            bi = getBookInformation(targetName, baseDirectory, coverPath);
            bi.bookCode = bookCode;
            bi.title = pureName;
            bi.fileSize = -1;
            bi.downSize = -1;
            bi.isDownloaded = true;
            final BookInformation tbi = bi;
            SkyApplicationHolder.sd.updateBook(bi);
            debug("instalBook ends");
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    reloadBookInformation(tbi.bookCode);
                }
            }, 500);
        } catch (Exception e) {
            debug(e.getMessage());
        }
    }

    public synchronized void copyBookToDevice(String filePath, String targetName) {
        try {
            InputStream localInputStream = null;

            if (filePath.contains("asset")) {
                String fileName = SkyUtility.getFileName(filePath);
                localInputStream = this.getAssets().open("books/" + fileName);
            } else {
                localInputStream = new FileInputStream(filePath);
            }
            String bookDir = SkySetting.getStorageDirectory( ) + "/books";
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
        } catch (IOException localIOException) {
            localIOException.printStackTrace();
            return;
        }
    }

    public class LocalBinder extends Binder {
        public LocalService getService() {
            return LocalService.this;
        }
    }

    private class DownloadCoverTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                String url = params[0];
                String fileName = params[1];
                Bitmap bmp = getBitmapFromURL(url);
                FileOutputStream out = new FileOutputStream(fileName);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                bmp.recycle();
            } catch (Exception e) {
                e.printStackTrace();
//				Log.e("donwloadCover error: ", e.getMessage().toString());
            }
            return null;
        }
    }

    class DownloadTask extends TimerTask {
        private final Handler progressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int bookCode = msg.getData().getInt("BOOKCODE");
                int bytes_downloaded = msg.getData().getInt("BYTES_DOWNLOADED");
                int bytes_total = msg.getData().getInt("BYTES_TOTAL");
                double percent = msg.getData().getDouble("PERCENT");
                sendProgress(bookCode, bytes_downloaded, bytes_total, percent);
            }
        };
        int bookCode = -1;
        long downloadId = -1;
        Handler cancelHandler = new Handler() {
            public void handleMessage(Message m) {
                deleteBook(bookCode);
                reloadBookInformations();
            }
        };

        DownloadTask(int bookCode, long downloadId) {
            this.bookCode = bookCode;
            this.downloadId = downloadId;
        }

        @Override
        public void run() {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);
            Cursor cursor = downloadManager.query(q);
            cursor.moveToFirst();
            boolean ret = checkStatus(cursor);
            if (ret == false) {
                cancel();
                Message msg = new Message();
                cancelHandler.sendMessage(msg);
            }
            int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            debug("total " + bytes_total);
            cursor.close();
            double dl_progress = (double) ((double) bytes_downloaded / (double) bytes_total);
            double percent = dl_progress;

            if (bytes_total > 0) {
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putInt("BOOKCODE", bookCode);
                b.putInt("BYTES_DOWNLOADED", bytes_downloaded);
                b.putInt("BYTES_TOTAL", bytes_total);
                b.putDouble("PERCENT", percent);
                msg.setData(b);
                progressHandler.sendMessage(msg);
            }
            if (isBookDownloaded(bookCode)) {
                downloadManager.remove(downloadId);
                debug("download finished successfully for BookCode:" + bookCode);
                cancel();
            }
        }
    }

    class KeyDelegate implements KeyListener {
        @Override
        public String getKeyForEncryptedData(String uuidForContent, String contentName, String uuidForEpub) {
            // TODO Auto-generated method stub
            String key = SkyApplicationHolder.keyManager.getKey(uuidForContent, uuidForEpub);
            return key;
        }

        @Override
        public Book getBook() {
            // TODO Auto-generated method stub
            return null;
        }
    }

}
