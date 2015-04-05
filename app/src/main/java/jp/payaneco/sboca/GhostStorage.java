package jp.payaneco.sboca;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by payaneco on 15/02/18.
 */
public class GhostStorage extends SQLiteOpenHelper {
    private static final String DB_NAME = "sboca.db";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase db;

    public GhostStorage(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
    }

    public boolean hasStrage(String ghostName) {
        if(hasLocalStorage(ghostName)) {
            return true;
        }
        if(hasWebStorage(ghostName)) {
            return true;
        }
        return false;
    }

    public boolean hasLocalStorage(String ghostName) {
        if(ghostName.isEmpty()) {
            //default ghost
            return true;
        }
        String where = String.format("ghost_name='%s'", ghostName);
        Cursor c = db.query("ghost", new String[] {"ghost_name"}, where, null, null, null, null);
        return (c.getCount() > 0);
    }

    public boolean hasWebStorage(String ghostName) {
        return SbocaClient.getGhostMap().containsKey(ghostName);
    }

    public Ghost summon(String ghostName) throws IOException {
        if(ghostName.isEmpty()) {
            //default ghost
            return new Ghost();
        }
        if (!hasLocalStorage(ghostName)) {
            if(!hasWebStorage(ghostName)) {
                return null;
            }
            return download(ghostName);
        }
        return new Ghost(getGhostText(ghostName), getGhostSurface(ghostName));
    }

    private String getGhostText(String ghostName) {
        String where = String.format("ghost_name='%s'", ghostName);
        Cursor c = db.query("ghost", new String[] {"ghost_text"}, where, null, null, null, null);
        c.moveToFirst();
        return c.getString(0);
    }

    public byte[] getGhostSurface(String ghostName) {
        String where = String.format("ghost_name='%s'", ghostName);
        Cursor c = db.query("ghost", new String[] {"ghost_surface"}, where, null, null, null, null);
        c.moveToFirst();
        return c.getBlob(0);
    }

    private Ghost download(String ghostName) throws IOException {
        String fileName = getGhostFileName(ghostName);
        if(fileName == null || fileName.isEmpty()){
            return null;
        }
        ContentValues values = new ContentValues();
        values.put("ghost_name", ghostName);
        String ghostText = SbocaClient.getWholeText(fileName, "SJIS");
        values.put("ghost_text", ghostText);
        Ghost ghost = new Ghost(ghostText);
        byte[] image = SbocaClient.getImage(ghost.getSurfaceUrl());
        ghost.setImage(image);
        values.put("ghost_surface", image);

        db.insert("ghost", "", values);
        return ghost;
    }

    private String getGhostFileName(String ghostName) {
        HashMap<String, String> map = SbocaClient.getGhostMap();
        if(!map.containsKey(ghostName)) {
            return null;
        }
        return map.get(ghostName);
    }

    public String getGhostUrl(String ghostName) {
        String fileName = getGhostFileName(ghostName);
        if(fileName == null || fileName.isEmpty()) {
            return null;
        }
        return SbocaClient.getUrl(fileName);
    }

    public void deleteFromStorage(String ghostName) {
        db.delete("ghost", "ghost_name=?", new String[]{ghostName});
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists ghost(ghost_name primary key, ghost_text text, ghost_surface BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
