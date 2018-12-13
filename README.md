# SkyEpubDemo
from skyEpubDemo
https://skyepub.net/
This is a project wraping skyepub with kotlin 
usage

        val app = SkyApplicationHolder()
        val context = this
        app.init(context)

//you must exist 2017.epub
        val path = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                + File.separator + "books" + File.separator + "2017.epub")
        if (!File(path).exists()) {
            LocalServiceTool.debug("!new File(path).exists()")
            return
        }

        val bi = LocalServiceTool.installBook(path) as BookInformation

        LocalServiceTool.openBookViewer(context, bi, true)
        
