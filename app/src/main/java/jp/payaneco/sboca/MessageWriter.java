package jp.payaneco.sboca;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by payaneco on 15/03/15.
 */
public class MessageWriter {
    private Ghost ghost;
    private Message message;
    private HashMap<Integer, Integer> surfaceNoMap;
    private ArrayList<View> viewList;
    private ArrayList<String> scriptList;
    private ArrayList<Integer> surfaceList;
    private ArrayList<View> tagList;

    public MessageWriter(Context context, Ghost ghost, String script) {
        setGhost(ghost);
        message = getMessage(script);

        createSurfaceNoMap(ghost);
        initViewList(context);
        initTagViewList(context);
    }

    private void createSurfaceNoMap(Ghost ghost) {
        surfaceNoMap = new HashMap<>();
        int i = 0;
        surfaceNoMap.put(i, -1);
        for(int surfaceNo : ghost.getSurfaceNoSet()) {
            i++;
            surfaceNoMap.put(i, surfaceNo);
        }
    }

    public Ghost getGhost() {
        return ghost;
    }

    public void setGhost(Ghost ghost) {
        this.ghost = ghost;
    }

    public GridAdapter getGridAdapter(Context context) {
        return new GridAdapter(ghost, surfaceNoMap, context);
    }

    public Message getMessage() {
        return message;
    }

    public Message getMessage(String script) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddkkmmss");
        sb.append(format.format(new Date())).append('\t');
        sb.append("0000000000000000").append('\t');
        sb.append(Message.PLACE_GREETINGS).append('\t');
        sb.append(ghost.getSakura()).append('\t');
        sb.append(0).append('\t');
        sb.append(-1).append('\t');
        sb.append(-1).append('\t');
        sb.append(script);
        return new Message(sb.toString());
    }

    public void modify(Context context, View v, String script) {

    }

    public void set(Context context, String script) {
        message.setScript(script);
        initViewList(context);
    }

    public void add(Context context, ActorType actor, int surfaceNo, String text, boolean isScript) {
        message.insert(actor, surfaceNo, text, isScript);
        initViewList(context);
    }

    public void modify(Context context, int position, String text) {
        message.modify(position, text);
        initViewList(context);
    }

    private void initTagViewList(Context context) {
        tagList = new ArrayList<>();
        int i = 0;
        tagList.add(getTagTextView(context, i++, "\\h"));
        tagList.add(getTagTextView(context, i++, "\\u"));
        tagList.add(getTagTextView(context, i++, "\\n"));
        tagList.add(getTagTextView(context, i++, "\\w5"));
        tagList.add(getTagTextView(context, i++, "\\w9"));
        tagList.add(getTagTextView(context, i++, "\\_s"));
        tagList.add(getTagTextView(context, i++, "\\_q"));
        tagList.add(getTagTextView(context, i++, "\\c"));
        tagList.add(getTagTextView(context, i++, "\\w"));
        tagList.add(getTagTextView(context, i++, "\\n[half]"));
        tagList.add(getTagTextView(context, i++, "\\URL[]"));
        tagList.add(getTagTextView(context, i++, "[]"));
    }

    private TextView getTagTextView(Context context, int i, String text) {
        int color = (i % 2 == 0) ? R.color.background_tag : R.color.background_tag2;
        TextView view = getTextView(context, text, color);
        view.setTextSize(20);
        view.setPadding(3, 2, 3, 2);
        return view;
    }

    public List<View> getTagViewList() {
        return tagList;
    }

    private void initViewList(Context context) {
        viewList = new ArrayList<>();
        scriptList = new ArrayList<>();
        surfaceList = new ArrayList<>();
        View view = getTextLayoutView(context, "¥t", R.color.background_command);
        StringBuilder sb = new StringBuilder();
        int surfaceNo = -1;
        ActorType actor = null;
        for(Phrase phrase : message.getPhraseList()) {
            switch (phrase.getCommand()) {
                case Hontai:
                    surfaceNo = phrase.getSurfaceNo();
                    put(view, sb.toString(), surfaceNo);
                    actor = ActorType.Hontai;
                    view = getSakuraView(context);
                    sb = new StringBuilder();
                    break;
                case Unyu:
                    surfaceNo = phrase.getSurfaceNo();
                    put(view, sb.toString(), surfaceNo);
                    actor = ActorType.Unyu;
                    view = getUnyuView(context);
                    sb = new StringBuilder();
                    break;
                case Surface:
                    put(view, sb.toString(), phrase.getSurfaceNo());
                    surfaceNo = phrase.getSurfaceNo();
                    view = getSurfaceView(context, surfaceNo);
                    sb = new StringBuilder();
                    break;
                case Unhandled:
                    continue;
            }
            sb.append(phrase.getScript());
        }
        put(view, sb.toString(), surfaceNo);
        view = getTextLayoutView(context, "¥e", R.color.background_command);
        put(view, "", getLastSurfaceNo(actor));
    }

    private void put(View view, String script, int position) {
        viewList.add(view);
        scriptList.add(script);
        surfaceList.add(position);
    }

    public List<View> getViewList() {
        return viewList;
    }

    public List<String> getScriptList() {
        return scriptList;
    }

    private View getSakuraView(Context context) {
        return getTextLayoutView(context, "¥h", R.color.background_sakura);
    }

    private View getUnyuView(Context context) {
        return getTextLayoutView(context, "¥u", R.color.background_unyu);
    }

    private View getTextLayoutView(Context context, String text, int colorId) {
        TextView view = getTextView(context, text, colorId);
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setPadding(3, 5, 3, 5);
        layout.addView(view);
        return layout;
    }

    private TextView getTextView(Context context, String text, int colorId) {
        TextView view = new TextView(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setText(text);
        int color = context.getResources().getColor(colorId);
        view.setBackgroundColor(color);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private View getSurfaceView(Context context, int surfaceNo) {
        ImageView view = new ImageView(context);
        view.setImageBitmap(ghost.getBitmap(surfaceNo));
        view.setPadding(3, 2, 3, 2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        return view;
    }

    public ActorType getLastActor() {
        return message.getPhraseList().getLast().getActor();
    }

    public int getLastSurfaceIndex(ActorType actor) {
        int surfaceNo = getLastSurfaceNo(actor);
        return getSurfaceIndex(surfaceNo);
    }

    private int getLastSurfaceNo(ActorType actor) {
        int surfaceNo = -1;
        for(Phrase phrase : message.getPhraseList()) {
            if(!phrase.getCommand().equals(CommandType.Surface)) {
                continue;
            }
            if(!phrase.getActor().equals(actor)) {
                continue;
            }
            surfaceNo = phrase.getSurfaceNo();
        }
        return surfaceNo;
    }

    private int getSurfaceIndex(int surfaceNo) {
        for(int key : surfaceNoMap.keySet()) {
            if(surfaceNoMap.get(key).equals(surfaceNo)) {
                return key;
            }
        }
        return 0;
    }

    public int getSurfaceIndex(View v) {
        int index = getViewIndex(v);
        return getSurfaceIndex(surfaceList.get(index));
    }

    private int getViewIndex(View v) {
        for(int i = 0; i < viewList.size(); i++) {
            View view = viewList.get(i);
            if(v.equals(view)) {
                return i;
            }
        }
        return viewList.size() - 1;
    }

    public void setSelection(View v) {
        int selIndex = getViewIndex(v);
        for(int i = 0; i < viewList.size(); i++) {
            View view = viewList.get(i);
            if(selIndex == i) {
                view.setBackgroundColor(Color.RED);
            }
            else
            {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    public int getSelection() {
        for(int i = 0; i < viewList.size(); i++) {
            View view = viewList.get(i);
            int color = ((ColorDrawable) view.getBackground()).getColor();
            if(Color.RED == color) {
                return i;
            }
        }
        return viewList.size() - 1;
    }

    public int size() {
        return viewList.size();
    }

    public String getAllScript(boolean isTrim) {
        return message.getScript(isTrim);
    }

    public String getSelectedScript() {
        return scriptList.get(getSelection());
    }
}
