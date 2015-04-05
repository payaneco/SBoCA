package jp.payaneco.sboca;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by payaneco on 15/03/04.
 */
public class Settings {
    public static final int MAX_SPAN = 30 * 60;    //30時間

    private static ChannelMap channelMap;

    private static SharedPreferences getPreferences(Context context) {
        SharedPreferences pref = context.getSharedPreferences("SbocaPreferences", Context.MODE_PRIVATE);
        return pref;
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences pref = getPreferences(context);
        return pref.edit();
    }

    public static Date getLastLogTime(Context context) {
        SharedPreferences pref = getPreferences(context);
        long l = pref.getLong("lastLogTime", 0);
        return new Date(l);
    }

    public static void setLastLogTime(Context context, Date lastLogTime) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putLong("lastLogTime", lastLogTime.getTime());
        editor.commit();
    }

    /**
     * 最新の既読ログ時刻以降なら更新
     * @param context
     * @param lastLogTime
     * @return
     */
    public static boolean trySetLastLogTime(Context context, Date lastLogTime) {
        if(lastLogTime.before(getLastLogTime(context))) {
            return false;
        }
        setLastLogTime(context, lastLogTime);
        return true;
    }

    public static int getSpan(Context context) {
        Date date = getLastLogTime(context);
        if(date.getTime() == 0) {
            return 0;
        }
        long diff = new Date().getTime() - date.getTime();
        diff /= (60 * 1000);
        return (int)Math.min(Math.max(diff + 1, 1), MAX_SPAN);   //1分前～30時間前に制限
    }

    public static String getLuid(Context context) {
        return getString(context, R.string.preference_luid_edit_key);
    }

    public static String getCurrentChannel(Context context) {
        return getString(context, R.string.preference_place_key);
    }

    public static String getCurrentGhost(Context context) {
        return getString(context, R.string.preference_ghost_key);
    }

    private static String getString(Context context, int keyId) {
        SharedPreferences pref = getPreferences(context);
        String key = context.getString(keyId);
        return pref.getString(key, "");
    }

    public static void setLuid(Context context, String luid) {
        setString(context, R.string.preference_luid_edit_key, luid);
    }

    public static void setCurrentChannel(Context context, String channel) {
        setString(context, R.string.preference_place_key, channel);
    }

    public static void setCurrentGhost(Context context, String ghost) {
        setString(context, R.string.preference_ghost_key, ghost);
    }

    private static void setString(Context context, int keyId, String value) {
        SharedPreferences.Editor editor = getEditor(context);
        String key = context.getString(keyId);
        editor.putString(key, value);
        editor.commit();
    }

    public static ChannelMap getChannelMap() {
        return channelMap;
    }

    public static void setChannelMap(ChannelMap channelMap) {
        Settings.channelMap = channelMap;
    }

    public static boolean canPost(Context context) {
        if(getCurrentChannel(context).isEmpty()) {
            return false;
        } else if(getCurrentGhost(context).isEmpty()) {
            return false;
        }
        return true;
    }
}
