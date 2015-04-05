package jp.payaneco.sboca;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by payaneco on 15/03/08.
 */


public class MessageArrayAdapter extends ArrayAdapter<Message> {

    private int resourceId;
    private List<Message> items;
    private LayoutInflater inflater;

    public MessageArrayAdapter(Context context, int resourceId, List<Message> items) {
        super(context, resourceId, items);

        this.resourceId = resourceId;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = inflater.inflate(this.resourceId, null);
        }

        Message message = items.get(position);

        // テキストをセット
        TextView infoText = (TextView)view.findViewById(R.id.info_text);
        infoText.setText(message.getSpannedText());

        // todo バージョン管理導入後後削除
            /*
            // アイコンはセットしない
            ImageView appInfoImage = (ImageView)view.findViewById(R.id.item_image);
            GhostStorage ghostStorage = new GhostStorage(getActivity());
            if(ghostStorage.hasLocalStorage(message.getGhost())) {
                Ghost ghost;
                try {
                    ghost = ghostStorage.summon(message.getGhost());
                } catch (IOException e) {
                    ghost = new Ghost();
                    try {
                        ghost.initialize(getResources().getAssets().open("default.txt"), getResources().openRawResource(R.raw.conductor));
                    } catch (IOException e1) {
                        return view;
                    }
                }
                appInfoImage.setImageBitmap(ghost.getBitmap(0));
            }
            */

        return view;
    }
}