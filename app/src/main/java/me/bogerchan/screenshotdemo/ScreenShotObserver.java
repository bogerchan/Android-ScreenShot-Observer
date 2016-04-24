package me.bogerchan.screenshotdemo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * Created by BogerChan on 16/4/24.
 */
public class ScreenShotObserver extends ContentObserver {

    public static final long TIME_GAP = 0x4;

    private Context mContext;
    private Handler mHandler;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public ScreenShotObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        ContentResolver resolver = mContext.getContentResolver();

        long current = System.currentTimeMillis() / 1000;
        String selection = String.format("date_added > %s and date_added < %s and ( _data like ? or _data like ? or _data like ? )", current - TIME_GAP, current + TIME_GAP);

        String[] selectionArgs = new String[]{"%Screenshot%", "%screenshot%", "%\u622a\u5c4f%"};
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null);
            if (cursor == null) {
                sendMessage("返回空的游标，请确认是否有权限！");
                return;
            }
            sendMessage(String.format("发现媒体库变化，相关数据项有%s", cursor.getCount()));
            if (!cursor.moveToLast()) {
                sendMessage("游标不能移动到最后一个");
                return;
            }
            int dateIdx = cursor.getColumnIndexOrThrow("date_added");
            long date = cursor.getLong(dateIdx);
            int dataIdx = cursor.getColumnIndexOrThrow("_data");
            String data = cursor.getString(dataIdx);
            int mineTypeIdx = cursor.getColumnIndexOrThrow("mime_type");
            String mineType = cursor.getString(mineTypeIdx);
            int sizeIdx = cursor.getColumnIndexOrThrow("_size");
            long size = cursor.getLong(sizeIdx);

            if (TextUtils.isEmpty(mineType)) {
                sendMessage("查询不到数据");
                return;
            }
            sendMessage(String.format("监听到要查询的数据:\ndate_added: %s\n_data: %s\nmine_type: %s\nsize: %s", date, data, mineType, size));
        } catch (Throwable tr) {
            sendMessage(String.format("查询发生了异常,异常信息是: %s", tr.getMessage()));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void sendMessage(String msg) {
        Message message = new Message();
        message.what = MainActivity.MSG_SCREENSHOT;
        message.obj = msg;
        mHandler.sendMessage(message);
    }
}
