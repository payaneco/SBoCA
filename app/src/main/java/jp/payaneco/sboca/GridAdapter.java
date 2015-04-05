package jp.payaneco.sboca;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by payaneco on 15/03/13.
 */
public class GridAdapter extends BaseAdapter {
    private Ghost ghost;
    private HashMap<Integer, Integer> surfaceNoMap;
    private LayoutInflater layoutInflater;

    public GridAdapter(Ghost ghost, HashMap<Integer, Integer> surfaceNoMap, Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.ghost = ghost;
        this.surfaceNoMap = surfaceNoMap;
    }

    @Override
    public int getCount() {
        return surfaceNoMap.size();
    }

    @Override
    public Object getItem(int position) {
        return surfaceNoMap.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.grid_image);
            holder.textView = (TextView)convertView.findViewById(R.id.grid_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        Integer surfaceNo = surfaceNoMap.get(position);
        holder.imageView.setImageBitmap(ghost.getBitmap(surfaceNo));
        holder.textView.setText(String.valueOf(surfaceNo));

        return convertView;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }
}
