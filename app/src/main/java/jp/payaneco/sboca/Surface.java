package jp.payaneco.sboca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * サーフィスのpng全体と、サーフィスに対応するチップを管理する
 * Created by payaneco on 15/02/12.
 */
public class Surface {
    public static final int UNDEFINED = Integer.MIN_VALUE;

    private static final String[] KEYS = {"surface", "alias", "sequential"};
    private static final int TILE_WIDTH = 48;
    private static final int TILE_HEIGHT = 64;

    private TreeMap<Integer, Integer> mIndexMap;
    private HashMap<Integer, HashSet<Integer>> mSakuraAliasMap;
    private HashMap<Integer, HashSet<Integer>> mKeroAliasMap;
    private HashMap<Integer, LinkedList<String>> mSequentialMap;
    private BitmapRegionDecoder mDecoder;

    /**
     * コンストラクタ
     */
    public Surface(){
        mIndexMap = new TreeMap<>();
        mSakuraAliasMap = new HashMap<>();
        mKeroAliasMap = new HashMap<>();
        mSequentialMap = new HashMap<>();
    }

    public static final boolean containsKey(String key){
        for(String s: KEYS){
            if(s.equalsIgnoreCase(key)){
                return true;
            }
        }
        return false;
    }

    public void setParsedData(String parameter, String value) {
        switch (parameter){
            case "surface": add(value); break;
            case "alias": setAlias(value); break;
            case "sequential": setSequence(value);
        }
    }

    public void add(String value){
        String[] ss =  value.split(":");
        if(ss.length != 2) {
            return;
        }
        try {
            putSurfaces(ss[0], ss[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void putSurfaces(String surface, String value) throws NumberFormatException{
        String[] ss = surface.split("-", 2);
        int start = Integer.parseInt(ss[0]);
        int end = start;
        if(ss.length > 1){
            end = Integer.parseInt(ss[1]);
        }
        for(int i = start; i <= end; i++) {
            int index;
            if(value.equalsIgnoreCase("*")) {
                index = i;
            } else {
                index = Integer.parseInt(value);
            }
            mIndexMap.put(i, index);
        }
    }

    public int get(int surfaceNo) {
        if(!mIndexMap.containsKey(surfaceNo)){
            return -1;
        }
        return mIndexMap.get(surfaceNo);
    }

    public Set<Integer> getIndexSet() {
        return mIndexMap.keySet();
    }

    public void setAlias(String value){
        Pattern pattern = Pattern.compile("^ *\\[ *(sakura|kero)\\.(\\d+) *= *([\\d| ]+)\\] *$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        if(matcher.matches()){
            int surfaceNo = Integer.parseInt(matcher.group(2));
            setAlias(matcher.group(1), surfaceNo, matcher.group(3));
        }
    }

    private void setAlias(String actor, int surfaceNo, String value){
        switch (actor.toLowerCase()) {
            case "sakura": setAlias(surfaceNo, value, mSakuraAliasMap); break;
            case "kero": setAlias(surfaceNo, value, mKeroAliasMap); break;
        }
    }

    private void setAlias(int surfaceNo, String value, HashMap<Integer, HashSet<Integer>> aliasMap) {
        HashSet<Integer> set = new HashSet<>();
        String[] ss = value.split("\\|");
        for(String s: ss){
            String index = s.trim();
            if(index == null || index.isEmpty()){
                continue;
            }
            set.add(Integer.valueOf(index));
        }
        aliasMap.put(surfaceNo, set);
    }

    public Integer[] getAlias(ActorType actorType, int surfaceNo) {
        HashMap<Integer, HashSet<Integer>> map;
        switch (actorType) {
            case Hontai: map = mSakuraAliasMap;break;
            case Unyu: map = mKeroAliasMap;break;
            default: return null;
        }
        if(!map.containsKey(surfaceNo)){
            return null;
        }
        HashSet<Integer> set = map.get(surfaceNo);
        return set.toArray(new Integer[set.size()]);
    }

    private void setSequence(String value) {
        Pattern pattern = Pattern.compile("^ *\\[ *(\\d+) *= *(\\d[\\d>\\*\\- ]+)\\] *$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        if(matcher.matches()){
            int surfaceNo = Integer.parseInt(matcher.group(1));
            setSequence(surfaceNo, matcher.group(2));
        }
    }

    private void setSequence(int surfaceNo, String value) {
        LinkedList<String> list = new LinkedList<>();
        String[] ss = value.split(">");
        String surface = "-1";
        for(String s: ss){
            String token = s.trim();
            if(token != null && !token.isEmpty()){
                surface = token;
            }
            list.add(surface);
        }
        mSequentialMap.put(surfaceNo, list);
    }

    public LinkedList<String> getSequence(int surfaceNo) {
        return mSequentialMap.get(surfaceNo);
    }

    public void setDecoder(BitmapRegionDecoder decoder) {
        this.mDecoder = decoder;
    }

    public Bitmap getBitmap(int surfaceNo) {
        int index = get(surfaceNo);
        if(index == -1){
            return null;
        }
        Integer xCount = mDecoder.getWidth() / TILE_WIDTH;
        //Integer yCount = mDecoder.getHeight() / TILE_HEIGHT;
        int x = index % xCount;
        int y = index / xCount;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Point point = new Point(x * TILE_WIDTH, y * TILE_HEIGHT);
        Rect rect = new Rect(point.x, point.y, point.x + TILE_WIDTH, point.y + TILE_HEIGHT);
        Bitmap bitmap = mDecoder.decodeRegion(rect, options);
        return bitmap;
    }
}
