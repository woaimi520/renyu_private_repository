package com.wenming.library.upload;

import android.app.IntentService;
import android.content.Intent;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.wenming.library.LogReport;
import com.wenming.library.util.CompressUtil;
import com.wenming.library.util.FileUtil;
import com.wenming.library.util.LogUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 此Service用于后台发送日志
 * Created by wenmingvs on 2016/7/9.
 */
public class UploadService extends IntentService {

    public static final String TAG = "UploadService";

    /**
     * 压缩包名称的一部分：时间戳
     */
    public final static SimpleDateFormat ZIP_FOLDER_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS", Locale.getDefault());

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UploadService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 同一时间只会有一个耗时任务被执行，其他的请求还要在后面排队，
     * onHandleIntent()方法不会多线程并发执行，所有无需考虑同步问题
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        callBackStatue(UploadStatue.LOADING.getType());
        try {
            final File logfolder = new File(LogReport.getInstance().getROOT() + "Log/");
            // 如果Log文件夹都不存在，说明不存在崩溃日志，检查缓存是否超出大小后退出
            if (!logfolder.exists() || logfolder.listFiles().length == 0) {
                callBackStatue(UploadStatue.NOFILE.getType());
                LogUtil.d("Log文件夹都不存在，无需上传");
                return;
            }
            //只存在log文件，但是不存在崩溃日志，也不会上传
            ArrayList<File> crashFileList = FileUtil.Companion.getCrashList(logfolder);
//        if (crashFileList.size() == 0) {
//            LogUtil.d(TAG, "只存在log文件，但是不存在崩溃日志，所以不上传");
//            return;
//        }
            File zipfolder = new File(LogReport.getInstance().getROOT() + "AlreadyUploadLog/");
            File zipfile = new File(zipfolder, "UploadOn" + ZIP_FOLDER_TIME_FORMAT.format(System.currentTimeMillis()) + ".zip");
            final File rootdir = new File(LogReport.getInstance().getROOT());
            StringBuilder content = new StringBuilder();

            //创建文件，如果父路径缺少，创建父路径
            zipfile = FileUtil.Companion.createFile(zipfolder, zipfile);

            //把日志文件压缩到压缩包中
            if (CompressUtil.zipFileAtPath(logfolder.getAbsolutePath(), zipfile.getAbsolutePath())) {
                LogUtil.d("把日志文件压缩到压缩包中 ----> 成功");
                if (crashFileList.size() != 0) {
                    for (File crash : crashFileList) {
                        content.append(FileUtil.Companion.getText(crash));
                        content.append("\n");
                    }
                }

                LogReport.getInstance().getUpload().sendFile(zipfile, content.toString(), new ILogUpload.OnUploadFinishedListener() {
                    @Override
                    public void onSuceess() {
                        callBackStatue(UploadStatue.SUCCESS.getType());
                        LogUtil.d("日志发送成功！！");
                        FileUtil.Companion.deleteDir(logfolder);
                        boolean checkresult = checkCacheSize(rootdir);
                        LogUtil.d("缓存大小检查，是否删除root下的所有文件 = " + checkresult);
                        stopSelf();
                    }

                    @Override
                    public void onError(String error) {
                        callBackStatue(UploadStatue.ERROR.getType());
                        LogUtil.d("日志发送失败：  = " + error);
                        boolean checkresult = checkCacheSize(rootdir);
                        LogUtil.d("缓存大小检查，是否删除root下的所有文件 " + checkresult);
                        stopSelf();
                    }
                });
            } else {
                callBackStatue(UploadStatue.ERROR.getType());

                LogUtil.d("把日志文件压缩到压缩包中 ----> 失败");
            }
        } catch (Exception e) {
            callBackStatue(UploadStatue.ERROR.getType());
        }finally {
            callBackStatue(UploadStatue.CANCLE.getType());
        }
    }

    public void callBackStatue(String statue) {
        LiveEventBus
                .get("error_up_key")
                .post(statue);
    }


    /**
     * 检查文件夹是否超出缓存大小
     *
     * @param dir 需要检查大小的文件夹
     * @return 返回是否超过大小，true为是，false为否
     */

    public boolean checkCacheSize(File dir) {
        long dirSize = FileUtil.Companion.folderSize(dir);
        return dirSize >= LogReport.getInstance().getCacheSize() && FileUtil.Companion.deleteDir(dir);
    }
}
