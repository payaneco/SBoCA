package jp.payaneco.sboca;


import android.content.res.Resources;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class WriteFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "3";

    private Handler mHandler;
    private MessageWriter writer;
    private View selGridItem;
    private boolean canPost;
    private String storedText;

    private String getScript() {
        MainActivity activity = (MainActivity)getActivity();
        return activity.getWriterScript();
    }

    private void setScript(String script) {
        MainActivity activity = (MainActivity)getActivity();
        activity.setWriterScript(script);

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static WriteFragment newInstance(int sectionNumber) {
        WriteFragment fragment = new WriteFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_write, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
        storedText = editText.getText().toString();

        mHandler = new Handler();

        final MainActivity activity = (MainActivity)getActivity();
        switch (activity.getViewMode()) {
            default:
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        canPost = initialize();
                    }
                });
                thread.start();
                try {
                    thread.join();
                    if(!canPost) {
                        showToast("設定が無効です。", Toast.LENGTH_SHORT);
                        activity.setViewMode(ViewModeType.Write);
                        activity.navigateFragment(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
        activity.setViewMode(ViewModeType.Write);

        Button btnAppend = (Button)getActivity().findViewById(R.id.button_append);
        btnAppend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ActorType actor = getCheckedActor();
                        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
                        writer.add(getActivity(), actor, getSelectedSurfaceNo(), editText.getText().toString(), false);
                        resetEditors();
                    }
                });
            }
        });
        Button btnModify = (Button)getActivity().findViewById(R.id.button_modify);
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
                        writer.modify(getActivity(), writer.getSelection() - 1, editText.getText().toString());
                        resetEditors();
                    }
                });
            }
        });
        Button btnModifyAll = (Button)getActivity().findViewById(R.id.button_modify_all);
        btnModifyAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
                        writer.set(getActivity(), editText.getText().toString());
                        resetEditors();
                    }
                });
            }
        });
        Button btnTry = (Button)getActivity().findViewById(R.id.button_try);
        btnTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = writer.getMessage();
                if(!message.isPostEnabled()) {
                    //todo ちゃんと該当スクリプトを表示
                    showToast("不正なスクリプトが含まれています。", Toast.LENGTH_SHORT);
                    return;
                }

                activity.setRehearsal(message);
                activity.setViewMode(ViewModeType.Rehearsal);
                activity.navigateFragment(1);

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        RadioButton radioText = (RadioButton)getActivity().findViewById(R.id.radio_text);
        radioText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
                        editText.setText("");
                        setButtonVisible(true, false, false);
                        setRadioActorVisible(true);
                    }
                });
            }
        });

        RadioButton radioScript = (RadioButton)getActivity().findViewById(R.id.radio_script);
        radioScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
                        editText.setText(writer.getSelectedScript());
                        setButtonVisible(false, true, false);
                        setRadioActorVisible(false);
                    }
                });
            }
        });

        RadioButton radioAllScript = (RadioButton)getActivity().findViewById(R.id.radio_all_script);
        radioAllScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
                        editText.setText(writer.getAllScript(true));
                        setButtonVisible(false, false, true);
                        setRadioActorVisible(false);
                    }
                });
            }
        });

        GridView gridView = (GridView)getActivity().findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setGridViewSelection(view);
            }
        });
        gridView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setGridViewSelection(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void resetEditors() {
        setMessageView(writer.getViewList());
        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
        editText.setText("");
        ActorType actor = writer.getLastActor();
        int newRadioId = (actor.equals(ActorType.Hontai)) ? R.id.radio_unyu : R.id.radio_sakura;
        RadioButton radioButton = (RadioButton)getActivity().findViewById(newRadioId);
        radioButton.setChecked(true);
        int position = writer.getLastSurfaceIndex(getCheckedActor(actor));
        setGridViewSelection(position);
        setScript(writer.getAllScript(false));
    }

    private void setButtonVisible(boolean isAddVisible, boolean isModVisible, boolean isAllVisible) {
        setButtonVisible(R.id.button_append, isAddVisible);
        setButtonVisible(R.id.button_modify, isModVisible);
        setButtonVisible(R.id.button_modify_all, isAllVisible);
    }

    private void setButtonVisible(int id, boolean isVisible) {
        Button target = (Button)getActivity().findViewById(id);
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        target.setVisibility(visibility);
    }

    private void setRadioActorVisible(boolean isVisible) {
        RadioButton radioSakura = (RadioButton)getActivity().findViewById(R.id.radio_sakura);
        RadioButton radioUnyu = (RadioButton)getActivity().findViewById(R.id.radio_unyu);
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        radioSakura.setVisibility(visibility);
        radioUnyu.setVisibility(visibility);
    }

    private void setGridViewSelection(int position) {
        GridView gridView = (GridView)getActivity().findViewById(R.id.grid_view);
        gridView.setSelection(position);
        View view = gridView.getChildAt(position);
        view = gridView.getAdapter().getView(position, view, gridView);
        setGridViewSelection(view);
    }

    private ActorType getCheckedActor() {
        return getCheckedActor(null);
    }

    private ActorType getCheckedActor(ActorType actor) {
        switch (getCheckedRadioButtonId())
        {
            case R.id.radio_sakura:
                actor = ActorType.Hontai;
                break;
            case R.id.radio_unyu:
                actor = ActorType.Unyu;
                break;
        }
        return actor;
    }

    private int getSelectedSurfaceNo() {
        ViewGroup layout = (ViewGroup)selGridItem;
        TextView textView = (TextView)layout.getChildAt(1);
        return Integer.parseInt(textView.getText().toString());
    }

    private void setGridViewSelection(View view) {
        if(view == null) {
            return;
        }
        selGridItem = view;
        ImageView imageView = (ImageView)getActivity().findViewById(R.id.image_write);
        int surfaceNo = getSelectedSurfaceNo();
        imageView.setImageBitmap(writer.getGhost().getBitmap(surfaceNo));
    }

    private int getCheckedRadioButtonId() {
        RadioButton radio;
        int id = R.id.radio_sakura;
        radio = (RadioButton)getActivity().findViewById(id);
        if(radio.isChecked()) return id;
        id = R.id.radio_unyu;
        radio = (RadioButton)getActivity().findViewById(id);
        if(radio.isChecked()) return id;
        //todo もっと綺麗に
        return R.id.radio_sakura;
    }

    private boolean initialize() {
        if(!Settings.canPost(getActivity())) {
            return false;
        }
        Ghost ghost;
        try {
            ghost = new GhostStorage(getActivity()).summon(Settings.getCurrentGhost(getActivity()));
        } catch (IOException e) {
            ghost = new Ghost();
        }
        if(ghost == null || !ghost.isLoaded()) {
            //ロードできない場合、投稿は禁止
            return false;
        }
        writer = new MessageWriter(getActivity(), ghost, getScript());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                GridView gridView = (GridView)getActivity().findViewById(R.id.grid_view);
                gridView.setAdapter(writer.getGridAdapter(getActivity()));
                setMessageView(writer.getViewList());
                EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
                editText.setText(storedText);
            }
        });
        return true;
    }

    private void setMessageView(List<View> viewList) {
        LinearLayout container = (LinearLayout)getActivity().findViewById(R.id.phrase_view);
        container.removeAllViews();
        for(int i = 0; i < viewList.size(); i++) {
            View view = viewList.get(i);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedMessageView(v);
                }
            });
            container.addView(view);
        }
        setSelectedMessageView(viewList.get(viewList.size() - 1));
    }

    private void setSelectedMessageView(View v) {
        writer.setSelection(v);
        int selIndex = writer.getSelection();

        EditText editText = (EditText)getActivity().findViewById(R.id.edit_write);
        editText.setText(writer.getScriptList().get(selIndex));
        boolean isLast = (selIndex == writer.size() - 1);
        setButtonVisible(isLast, !isLast, false);
        RadioButton radioScript = (RadioButton)getActivity().findViewById(R.id.radio_script);
        radioScript.setChecked(!isLast);
        RadioButton radioText = (RadioButton)getActivity().findViewById(R.id.radio_text);
        radioText.setChecked(isLast);
        setGridViewSelection(writer.getSurfaceIndex(v));
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
