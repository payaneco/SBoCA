package jp.payaneco.sboca;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by payaneco on 15/03/07.
 */
public class ListFragment extends Fragment {

    private ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final MainActivity activity = (MainActivity)getActivity();
        List<Message> list = activity.getMessageList();
        activity.setViewMode(ViewModeType.List);

        // adapterのインスタンスを作成
        MessageArrayAdapter adapter =
                new MessageArrayAdapter(getActivity(), R.layout.message_list_item, list);

        listView = (ListView)getActivity().findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setSelection(activity.getSelIndex());

        // アイテムクリック時のイベントを追加
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View view, int pos, long id) {
                activity.setSelIndex(pos);
                PlaceholderFragment.newInstance(0);
                activity.navigateFragment(1);
            }
        });

        Button btnKakoLog = (Button)getActivity().findViewById(R.id.button_kakolog);
        btnKakoLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                final EditText editKakoLog = (EditText) getActivity().findViewById(R.id.edit_kakolog);
                imm.hideSoftInputFromWindow(editKakoLog.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                final Handler handler = new Handler();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(editKakoLog.length() <= 0)
                        {
                            showToast("@string/msg_time_empty", Toast.LENGTH_SHORT);
                            return;
                        }
                        int value = Integer.parseInt(editKakoLog.getText().toString());
                        RadioButton rdoHour = (RadioButton) getActivity().findViewById(R.id.radio_hour);
                        if (rdoHour.isChecked()) {
                            value *= 60;
                        }
                        final MainActivity activity = (MainActivity) getActivity();
                        BottleClient client = new BottleClient();
                        List<Message> list = client.getLog(value);
                        list.set(0, Message.getGreetings(list.size()));
                        activity.setMessageList(list);

                        activity.setSelIndex(0);
                        PlaceholderFragment.newInstance(0);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                activity.navigateFragment(1);
                            }
                        });

                        for(Message message : list) {
                            message.createSpannedText();
                        }
                        //MessageArrayAdapter adapter = new MessageArrayAdapter(getActivity(), R.layout.message_list_item, list);
                        //listView.setAdapter(adapter);
                        //listView.setSelection(activity.getSelIndex());
                    }
                });
                thread.start();
            }
        });
    }

    private void showToast(final String msg, final int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, duration).show();
            }
        });
    }
}
