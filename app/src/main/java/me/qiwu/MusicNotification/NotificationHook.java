package me.qiwu.MusicNotification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import cn.nexus6p.QQMusicNotify.Base.BasicParam;
import cn.nexus6p.QQMusicNotify.Utils.GeneralUtils;
import cn.nexus6p.QQMusicNotify.Utils.LogUtils;
import cn.nexus6p.QQMusicNotify.Utils.PreferenceUtil;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import soptqs.medianotification.utils.NotificationUtils;

import static cn.nexus6p.QQMusicNotify.Utils.GeneralUtils.getContext;


/**
 * Created by Deng on 2019/2/18.
 */

public class NotificationHook {


    public void init(String packageName) {
        XposedHelpers.findAndHookMethod(Notification.Builder.class, "build", new XC_MethodHook() {

            @SuppressLint("ResourceType")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Notification notification = (Notification) param.getResult();
                if (isMediaNotification(notification)) {
                    Bundle extras = NotificationCompat.getExtras(notification);
                    Log.d("MusicNotification", extras.toString());
                    String title = null;
                    String subtitle = null;
                    try {
                        title = extras.get(NotificationCompat.EXTRA_TITLE).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        subtitle = extras.get(NotificationCompat.EXTRA_TEXT).toString();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    title = title == null || title.equals("") ? "未知音乐" : title;
                    subtitle = subtitle == null || subtitle.equals("") ? "未知艺术家" : subtitle;
                    //RemoteViews remoteViews = getContentView(title,subtitle,notification);
                    int resId = getIconId(notification.getSmallIcon());
                    /*MediaSession.Token token = null;
                    try {
                        token=notification.extras.getParcelable(EXTRA_MEDIA_SESSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/

                    BasicParam basicParam = new BasicParam(
                            getContext(), resId, title, subtitle, getLargeIcon(notification), PreferenceUtil.getPreference(getContext()).getBoolean("always_show", false) || (notification.flags == Notification.FLAG_ONGOING_EVENT), null
                    );
                    basicParam.setContentIntent(notification.contentIntent);
                    basicParam.setDeleteIntent(notification.deleteIntent);
                    List<NotificationCompat.Action> actions = new ArrayList<>();
                    List<Bitmap> actionIcons = new ArrayList<>();
                    int actionCount = NotificationCompat.getActionCount(notification);
                    for (int i = 0; i < actionCount; i++) {
                        NotificationCompat.Action action = NotificationCompat.getAction(notification, i);
                        try {
                            actionIcons.add(getBitmap(getContext().getDrawable(action.getIconCompat().getResId())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        actions.add(new NotificationCompat.Action.Builder(action.getIconCompat().getResId(), action.getTitle(), action.getActionIntent()).build());
                    }
                    Notification newNotification = new NotificationUtils().setParam(basicParam, actions, actionIcons).updateNotification();
                    param.setResult(newNotification);
                    //param.setResult(GeneralUtils.buildMusicNotificationWithoutAction(basicParam,remoteViews,notification.contentIntent,Build.VERSION.SDK_INT >= 26?notification.getChannelId():null,notification.deleteIntent));
                    LogUtils.Companion.addLogByContentProvider(packageName, "NotificationHook", GeneralUtils.getContext());
                }
            }
        });

    }

    private int getIconId(Icon icon) {
        int id = -1;
        if (icon != null) {
            try {
                id = (int) XposedHelpers.callMethod(icon, "getResId");
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }
        return id;
    }

    private Bitmap getLargeIcon(Notification notification) {
        Bitmap bitmap = null;
        if (notification.getLargeIcon() != null) {
            try {
                bitmap = (Bitmap) XposedHelpers.callMethod(notification.getLargeIcon(), "getBitmap");
            } catch (Exception e) {
                bitmap = BitmapFactory.decodeResource(getContext().getResources(), getIconId(notification.getLargeIcon()));
            }
        }
        return bitmap;
    }


    private boolean isMediaNotification(Notification notification) {

        if (notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
            return true;
        } else if (!TextUtils.isEmpty(notification.extras.getString(Notification.EXTRA_TEMPLATE))) {
            return Notification.MediaStyle.class.getName().equals(notification.extras.getString(Notification.EXTRA_TEMPLATE));
        }
        return false;
    }


    public static Bitmap getBitmap(Drawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }


}
