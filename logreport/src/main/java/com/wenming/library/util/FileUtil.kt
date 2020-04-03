package com.wenming.library.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

/**
 * 作者：任宇
 * 日期：2020/4/3 10:39
 * 注释：
 */
class FileUtil {
    companion object {
        private val TAG = "FileUtil"

        /**
         * 递归删除目录下的所有文件及子目录下所有文件
         *
         * @param dir 将要删除的文件目录
         * @return 删除成功返回true，否则返回false
         */
        fun deleteDir(dir: File): Boolean {
            if (dir.isDirectory) {
                val children = dir.list()
                // 递归删除目录中的子目录下
                for (aChildren in children) {
                    val success = deleteDir(File(dir, aChildren))
                    if (!success) {
                        return false
                    }
                }
            }
            // 目录此时为空，可以删除
            return dir.delete()
        }


        /**
         * 读取File中的内容
         *
         * @param file 请务必保证file文件已经存在
         * @return file中的内容
         */
        fun getText(file: File): String? {
            if (!file.exists()) {
                return null
            }
            val text = StringBuilder()
            var br: BufferedReader? = null
            try {
                br = BufferedReader(FileReader(file))
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    text.append(line)
                    text.append('\n')
                }
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
                e.printStackTrace()
            } finally {
                if (br != null) {
                    try {
                        br.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return text.toString()
        }

        /**
         * 遍历获取Log文件夹下的所有crash文件
         *
         * @param logdir 从哪个文件夹下找起
         * @return 返回crash文件列表
         */
        fun getCrashList(logdir: File): ArrayList<File>? {
            val crashFileList = ArrayList<File>()
            findFiles(logdir.absolutePath, crashFileList)
            return crashFileList
        }


        /**
         * 将指定文件夹中满足要求的文件存储到list集合中
         *
         * @param f
         * @param list
         */

        /**
         * 将指定文件夹中满足要求的文件存储到list集合中
         *
         * @param f
         * @param list
         */
        /**
         * 递归查找文件
         *
         * @param baseDirName 查找的文件夹路径
         * @param fileList    查找到的文件集合
         */
        fun findFiles(
            baseDirName: String,
            fileList: MutableList<File>
        ) {
            val baseDir = File(baseDirName) // 创建一个File对象
            if (!baseDir.exists() || !baseDir.isDirectory) { // 判断目录是否存在
                LogUtil.e(TAG, "文件查找失败：" + baseDirName + "不是一个目录！")
            }
            var tempName: String
            //判断目录是否存在
            var tempFile: File
            val files = baseDir.listFiles()
            for (file in files) {
                tempFile = file
                if (tempFile.isDirectory) {
                    findFiles(tempFile.absolutePath, fileList)
                } else if (tempFile.isFile) {
                    tempName = tempFile.name
                    if (tempName.contains("Crash")) { // 匹配成功，将文件名添加到结果集
                        fileList.add(tempFile.absoluteFile)
                    }
                }
            }
        }

        /**
         * 获取文件夹的大小
         *
         * @param directory 需要测量大小的文件夹
         * @return 返回文件夹大小，单位byte
         */
        fun folderSize(directory: File): Long {
            var length: Long = 0
            for (file in directory.listFiles()) {
                length += if (file.isFile) file.length() else folderSize(file)
            }
            return length
        }

        fun createFile(zipdir: File, zipfile: File): File? {
            if (!zipdir.exists()) {
                val result = zipdir.mkdirs()
                LogUtil.d("TAG", "zipdir.mkdirs() = $result")
            }
            if (!zipfile.exists()) {
                try {
                    val result = zipfile.createNewFile()
                    LogUtil.d("TAG", "zipdir.createNewFile() = $result")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("TAG", e.message)
                }
            }
            return zipfile
        }

        /**
         *   要删除的文件  是否删除文件夹
         *   删除指定路径下不满足时间限定的 文件及文件夹  文件夹为空的时候就删除
         * */
        public fun deleteFile(file: File,date:Date) {
            //判断文件目录是否存在存在
            if (!file.exists()) {
                return
            }
            //取得这个目录下的所有子文件对象
            var files = file.listFiles()
            //遍历该目录下的文件对象
            files?.run {
                for (f in files) {
                    //判断子目录是否存在子目录,如果是文件则删除
                    if (f.isDirectory) {
                        deleteFile(f,date)
                    } else {
                        //判断是否删除文件
                        if (Date(f.lastModified()).before(date)) {
                            f.delete()
                        }
                    }
                }
                //删除空文件夹
                if (file.isDirectory && file.listFiles().size <= 0) {
                    file.delete()
                }
            }
        }



        // 最外层文件夹
       public fun rootPath(context: Context): String? {
            var path: String? = null
            val state = Environment.getExternalStorageState()
            // /storage/emulated/0/ddlog/ImportantData
            // /data/user/0/com.cqebd.student/cache/ddlog/ImportantData
            val rootDir =
                if (state == Environment.MEDIA_MOUNTED) Environment.getExternalStorageDirectory() else context.getCacheDir()

            path =
                rootDir.absolutePath + File.separator + context.getString(context.getApplicationInfo().labelRes) + File.separator

            return path
        }


        //添加后值保留三天的
        public fun isToSaveForThreeDays(context: Context) {
            val date = Date(System.currentTimeMillis() - 24*3*1000 * 60 * 60) //三天 24*3*1000 * 60 * 60
            rootPath(context)?.run {
                val folder = File(this)
                folder.listFiles()?.run {
                    val files: Array<File> = this
                    for (i in files.indices) {
                        val file = files[i]
                        FileUtil.deleteFile(file,date)
                    }
                }
            }
        }






    }
}