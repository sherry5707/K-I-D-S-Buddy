package com.kinstalk.her.qchatapi.aws;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.FileUtils;
import com.kinstalk.her.qchatcomm.utils.NetworkUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;

import java.io.File;

/**
 * Created by wangzhipeng on 2018/4/7.
 */

public class AWSTransferHelper {
    private static final String TAG = "AWSTransferHelper";
    private static AWSTransferHelper mAWSTransferHelper;
    private static UploadFinishListener mListener;
    /**
     * 发送完成后的回调
     */
    public interface UploadFinishListener {
        //时长  和 文件
        void onUploadFinish(int status);
    }

    public static void setUploadFinishListener(UploadFinishListener listener) {
        mListener = listener;
    }

    public static void unSetUploadFinishListener() {
        mListener = null;
    }

    public void asyncNotifyUploadFinish(final int status) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                if(mListener != null)
                    mListener.onUploadFinish(status);
            }});
    }
    public static AWSTransferHelper getInstance() {
        if (mAWSTransferHelper == null) {
            mAWSTransferHelper = new AWSTransferHelper();
        }
        return mAWSTransferHelper;
    }

    public void upload(String filepath, String fileName) {
        String key = fileName;
        try {
            File file = new File(filepath);
            TransferUtility transferUtility = S3TransferUtil.getTransferUtility(BaseApplication.getInstance().getApplicationContext());
            TransferObserver observer = transferUtility.upload(
                    Constants.BUCKET_QWX,
                    key,
                    file);
            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    Log.e(TAG, "--onStateChanged test---state--" + state);
                    if (TransferState.COMPLETED.equals(state)) {
                        asyncNotifyUploadFinish(0);
                        QAILog.d(TAG, "send voice message success");
                    } else if (TransferState.CANCELED.equals(state)) {
                        asyncNotifyUploadFinish(-1);
                        //do something
                    } else if(TransferState.FAILED.equals(state)){
                        asyncNotifyUploadFinish(-1);
                    } else if(TransferState.PAUSED.equals(state)) {
                        asyncNotifyUploadFinish(-1);
                    } else if(TransferState.UNKNOWN.equals(state)){
                        asyncNotifyUploadFinish(-1);
                    } else {
                        Log.d(TAG, "upload TransferState: " + state);
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    Log.e(TAG, "onProgressChanged bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal);
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.e(TAG, "upload onError");
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void downloadRetry(final Context context, final String objectKey, int type, int times) {
        download(context, objectKey, type, times);
    }


    public void download(final Context context, final String objectKey, final int type, final int times) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                try {
                    TransferUtility transferUtility = S3TransferUtil.getTransferUtility(BaseApplication.getInstance().getApplicationContext());
                    final String savedPath = "/sdcard/" + objectKey;
                    TransferObserver observer = transferUtility.download(
                            Constants.BUCKET_QWX,
                            objectKey,
                            new File(savedPath));
                    Log.e(TAG, "--onStateChanged---state--" + objectKey);
                    observer.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            Log.e(TAG, "--onStateChanged---state--" + state);
                            if (TransferState.COMPLETED.equals(state)) {
                                ContentValues values = new ContentValues();
                                String where = "";

                                if (type == ChatProviderHelper.Chat.MSG_TYPE_VOICE) {
                                    values.put(ChatProviderHelper.Chat.COLUMN_AUDIO_LOCAL_PATH, savedPath);

                                    int dur = FileUtils.getDuration(savedPath);
                                    values.put(ChatProviderHelper.Chat.COLUMN_DURATION, dur);
                                    where = ChatProviderHelper.Chat.COLUMN_AUDIO_DOWNLOAD_PATH + "=?";

                                } else if (type == ChatProviderHelper.Chat.MSG_TYPE_PIC) {
                                    values.put(ChatProviderHelper.Homework.COLUMN_URL, savedPath);
                                    where = ChatProviderHelper.Homework.COLUMN_URL + "=?";
                                }
                                String[] selectionArgs = {objectKey};
                                if (type == 4) {
                                    BaseApplication.getInstance().getApplicationContext().getContentResolver()
                                            .update(ChatProviderHelper.Homework.CONTENT_URI, values, where, selectionArgs);
                                    MessageManager.asyncNotifyHomeworkChange(true);
                                } else {
                                    BaseApplication.getInstance().getApplicationContext().getContentResolver()
                                            .update(ChatProviderHelper.Chat.CONTENT_URI, values, where, selectionArgs);
                                    QAILog.d(TAG, "got voice message");
                                    MessageManager.asyncNotifyMessageChange(true);
                                    MessageManager.notifyLauncher(context);
                                }
                                // 音频下载完成，发通知
                                if (ChatProviderHelper.Chat.MSG_TYPE_VOICE == type) {

                                } else if (ChatProviderHelper.Chat.MSG_TYPE_PIC == type) {
                                }
                            } else if (TransferState.FAILED.equals(state)) {
                                int i = times;
                                if (i++ < 3)
                                    downloadRetry(context, objectKey, type, i);
                                else {
                                    if (type == 4) {
                                        String where = ChatProviderHelper.Homework.COLUMN_URL + "=?";
                                        String[] selectionArgs = {objectKey};
                                        BaseApplication.getInstance().getApplicationContext().getContentResolver()
                                                .delete(ChatProviderHelper.Homework.CONTENT_URI, where, selectionArgs);
                                    }
                                }
                            } else if (TransferState.CANCELED.equals(state)) {
                                //do something
                            } else {
                                Log.d(TAG, "download TransferState: " + state);
                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            Log.e(TAG, "onProgressChanged bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal);
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            Log.e(TAG, "download onError");
                            ex.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
