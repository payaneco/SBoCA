package jp.payaneco.sboca;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "2";
    private Stage mHontai;
    private Stage mUnyu;

    private Handler mHandler;
    private Thread mPlayThread;
    private Thread mSpanThread;

    public Handler getHandler() {
        if(mHandler == null) {
            mHandler = new Handler();
        }
        return mHandler;
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //スレッド終了まで待機
        try {
            cancelThreads();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void cancelThreads() throws InterruptedException {
        if(mHontai != null) {
            mHontai.setCancel(true);
        }
        if(mUnyu != null) {
            mUnyu.setCancel(true);
        }
        joinThread(mPlayThread);
        mPlayThread = null;
        joinThread(mSpanThread);
        mSpanThread = null;
    }

    private void joinThread(Thread thread) throws InterruptedException {
        if(thread == null || !thread.isAlive()) {
            return;
        }
        thread.join();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        getHandler();
        return rootView;
    }

    private List<Message> getMessageList() {
        MainActivity activity = (MainActivity)getActivity();
        return activity.getMessageList();
    }

    private void setMessageList(List<Message> messageList) {
        MainActivity activity = (MainActivity)getActivity();
        activity.setMessageList(messageList);
    }

    private Message getMessage() {
        return getMessageList().get(getSelIndex());
    }

    private Message getRehearsalMessage() {
        MainActivity activity = (MainActivity) getActivity();
        return activity.getRehearsal();
    }

    private int getSelIndex() {
        MainActivity activity = (MainActivity)getActivity();
        return activity.getSelIndex();
    }

    private void setSelIndex(int selIndex) {
        MainActivity activity = (MainActivity)getActivity();
        activity.setSelIndex(selIndex);
    }

    private Spanned getLastText() {
        int last = getMessageList().size() - getSelIndex() - 1;
        String text = String.format("残%d瓶", last);
        if(last == 0) {
            return Html.fromHtml(text);
        }
        String tag = String.format("<b><font color=\"Red\">%s</font></b>", text);
        return Html.fromHtml(tag);
    }

    private void selectFirst() {
        setSelIndex(0);
    }

    private void selectLast() {
        setSelIndex(getMessageList().size() - 1);
    }

    private void selectNext() {
        addSelection(1);
    }

    private void selectPrevious() {
        addSelection(-1);
    }

    private void addSelection(int addValue) {
        int selIndex = getSelIndex();
        setSelIndex(selIndex + addValue);
    }

    @Override
    public void onStart() {
        super.onStart();

        getHandler();
        MainActivity activity = (MainActivity)getActivity();
        boolean isRehearsal = false;
        switch (activity.getViewMode()) {
            case Initialize:
                initialize();
                break;
            case List:
                mPlayThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        play();
                    }
                });
                mPlayThread.start();
                break;
            case Play:
            case Write:
                if(getMessageList() == null) {
                    initialize();
                }
                break;
            case Rehearsal:
                isRehearsal = true;
                break;
        }
        if(!isRehearsal) {
            activity.setViewMode(ViewModeType.Play);
        }

        Button btnPlay = (Button)getActivity().findViewById(R.id.button_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        Button btnFirst = (Button)getActivity().findViewById(R.id.button_first);
        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFirst();
                play();
            }
        });

        Button btnLast = (Button)getActivity().findViewById(R.id.button_last);
        btnLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLast();
                play();
            }
        });

        Button btnPrevious = (Button)getActivity().findViewById(R.id.button_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPrevious();
                if(getSelIndex() < 0) {
                    showToast(getString(R.string.first_bottle), Toast.LENGTH_SHORT);
                    selectFirst();
                    return;
                }
                play();
            }
        });

        Button btnNext = (Button)getActivity().findViewById(R.id.button_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNext();
                if (getSelIndex() >= getMessageList().size()) {
                    showToast(getString(R.string.last_bottle), Toast.LENGTH_SHORT);
                    selectLast();
                    return;
                }
                play();
            }
        });

        TextView btnVotes = (TextView)getActivity().findViewById(R.id.button_votes);
        btnVotes.setEnabled(!isRehearsal);
        btnVotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        postVote();
                    }
                });
                thread.start();
            }
        });

        TextView btnAgrees = (TextView)getActivity().findViewById(R.id.button_agrees);
        btnVotes.setEnabled(!isRehearsal);
        btnAgrees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        postAgree();
                    }
                });
                thread.start();
            }
        });

        TextView btnTest = (TextView)getActivity().findViewById(R.id.button_test);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mPlayThread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        MainActivity activity = (MainActivity)getActivity();
//                        final String s = activity.getLog();
//                        getHandler().post(new Runnable() {
//                            @Override
//                            public void run() {
//                                TextView sakuraText = (TextView)getActivity().findViewById(R.id.sakuraText);
//                                sakuraText.setText(s);
//
//                            }
//                        });
//                    }
//                });
//                mPlayThread.start();
                // 確認ダイアログの生成
                AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
                alertDlg.setTitle("投瓶確認");
                alertDlg.setMessage("test？");
                alertDlg.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity activity = (MainActivity)getActivity();
                                        activity.setViewMode(ViewModeType.Play);
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                initialize();
                                                setButtonVisibilities(false);
                                            }
                                        });
                                    }
                                });
                                thread.start();
                            }
                        });
                alertDlg.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Cancel ボタンクリック処理
                            }
                        });

                // 表示
                alertDlg.show();
            }
        });

        Button btnRehearsal = (Button)getActivity().findViewById(R.id.button_rehearsal);
        btnRehearsal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(getRehearsalMessage());
            }
        });

        Button btnPost = (Button)getActivity().findViewById(R.id.button_post);
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postMessage(getRehearsalMessage());
            }
        });

        if(isRehearsal) {
            mPlayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    play(getRehearsalMessage());
                }
            });
            mPlayThread.start();
        }

        setButtonVisibilities(isRehearsal);
    }

    private void setButtonVisibilities(boolean isRehearsal) {
        int playVisibility = isRehearsal ? View.GONE : View.VISIBLE;
        Button btnPlay = (Button)getActivity().findViewById(R.id.button_play);
        btnPlay.setVisibility(playVisibility);
        Button btnFirst = (Button)getActivity().findViewById(R.id.button_first);
        btnFirst.setVisibility(playVisibility);
        Button btnLast = (Button)getActivity().findViewById(R.id.button_last);
        btnLast.setVisibility(playVisibility);
        Button btnPrevious = (Button)getActivity().findViewById(R.id.button_previous);
        btnPrevious.setVisibility(playVisibility);
        Button btnNext = (Button)getActivity().findViewById(R.id.button_next);
        btnNext.setVisibility(playVisibility);

        Button btnRehearsal = (Button)getActivity().findViewById(R.id.button_rehearsal);
        btnRehearsal.setVisibility(isRehearsal ? View.VISIBLE : View.GONE);
        Button btnPost = (Button)getActivity().findViewById(R.id.button_post);
        btnPost.setVisibility(isRehearsal ? View.VISIBLE : View.GONE);
        TextView btnTest = (TextView)getActivity().findViewById(R.id.button_test);
        btnTest.setVisibility(View.GONE);
    }

    private void postVote() {
        MainActivity activity = (MainActivity)getActivity();
        String mid = getMessage().getMid();
        final VoteResult result = activity.postVote(mid);
        showResult(result, Toast.LENGTH_SHORT);
    }

    private void postAgree() {
        MainActivity activity = (MainActivity)getActivity();
        String mid = getMessage().getMid();
        final VoteResult result = activity.postAgree(mid);
        showResult(result, Toast.LENGTH_SHORT);
    }

    private void initialize() {
        mPlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int span = Settings.getSpan(getActivity());
                List<Message> list = new BottleClient().getLog(span);
                Message greetings;
                if(Settings.getLuid(getActivity()).isEmpty()) {
                    greetings = Message.getGreetings();
                    Settings.trySetLastLogTime(getActivity(), new Date());
                } else {
                    greetings = Message.getGreetings(span, list.size());
                }
                list.add(0, greetings);
                setMessageList(list);
                selectFirst();
                play();
            }
        });
        mPlayThread.start();
        //SpannedText生成(処理が遅いのでマルチスレッド化)
        mSpanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mPlayThread.join();
                    for(Message message : getMessageList()) {
                        message.createSpannedText();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mSpanThread.setPriority(Thread.MIN_PRIORITY);
        mSpanThread.start();
    }

    private void play() {
        play(getMessage());
    }

    private void play(final Message message) {
        if(mHontai != null) {
            mHontai.setCancel(true);
        }
        if(mUnyu != null) {
            mUnyu.setCancel(true);
        }
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                setPlayIcon(getHandler(), true);
                if(!message.isGreetings()) {
                    Settings.trySetLastLogTime(getActivity(), message.getSendTime());
                }

                setMessageCount();

                TextView statusText = (TextView)getActivity().findViewById(R.id.statusText);
                statusText.setText(message.getStatus());
                setVote(message.getVotes());
                setAgree(message.getAgrees());
                HashMap<String, String> map = message.getLinkMap();
                LinearLayout layout = (LinearLayout)getActivity().findViewById(R.id.link_layout);
                layout.removeAllViews();
                if(map == null) {
                    return;
                }
                for(String key : map.keySet()) {
                    TextView linkView = new TextView(getActivity());
                    String tag = String.format("<a href=\"%s\">%s</a>", map.get(key), key);
                    linkView.setText(Html.fromHtml(tag));
                    MovementMethod movementMethod = LinkMovementMethod.getInstance();
                    linkView.setMovementMethod(movementMethod);
                    layout.addView(linkView);
                }
            }
        });

        mPlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    play(message, getHandler());
                    setPlayIcon(getHandler(), false);
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast(getString(R.string.play_failed), Toast.LENGTH_SHORT);
                }
            }
        });
        mPlayThread.start();
    }

    private void setMessageCount() {
        TextView positionText = (TextView)getActivity().findViewById(R.id.positionText);
        positionText.setText(getLastText());
        String count = String.format("全%d瓶", getMessageList().size() - 1);
        TextView messageCountText = (TextView)getActivity().findViewById(R.id.messageCountText);
        messageCountText.setText(count);
    }

    private void setVote(int value) {
        TextView voteText = (TextView)getActivity().findViewById(R.id.voteText);
        String votes = (value < 0) ? "" : String.format(getString(R.string.vote_format), value);
        voteText.setText(votes);
    }

    private void setAgree(int value) {
        TextView agreeText = (TextView)getActivity().findViewById(R.id.agreeText);
        String agrees = (value < 0) ? "" : String.format(getString(R.string.agree_format), value);
        agreeText.setText(agrees);
    }

    private void play(Message message, Handler handler) throws IOException{
        GhostStorage ghostStorage = new GhostStorage(getActivity());
        if(!ghostStorage.hasLocalStorage(message.getGhost())) {
            String toast = String.format(getString(R.string.ghost_downloading_format), message.getGhost());
            showToast(toast, Toast.LENGTH_LONG);
        }
        Ghost ghost;
        try {
            ghost = ghostStorage.summon(message.getGhost());
        } catch (IOException e) {
            showToast(getString(R.string.summon_failed), Toast.LENGTH_SHORT);
            ghost = new Ghost();
        }
        if(!ghost.isLoaded()) {
            //ロードできない場合、デフォルトに切り替え
            ghost.initialize(getResources().getAssets().open("default.txt"), getResources().openRawResource(R.raw.conductor));
        }
        //todo クラス分割
        mHontai = new Stage(ActorType.Hontai, handler, ghost, getActivity());
        mUnyu = new Stage(ActorType.Unyu, handler, ghost, getActivity());

        for(Phrase phrase: message.getPhraseList()) {
            mHontai.play(phrase);
            mUnyu.play(phrase);
            if(mHontai.isCancel() || mUnyu.isCancel()){
                //todo しっかり停止
                break;
            }
        }
    }

    private void postMessage(final Message message) {
        // 確認ダイアログの生成
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("投瓶確認");
        alertDlg.setMessage("この瓶を放流してよろしいですか？");
        alertDlg.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity activity = (MainActivity)getActivity();
                                BroadcastResult result = activity.postMessage(message);
                                showResult(result, Toast.LENGTH_SHORT);
                                activity.setViewMode(ViewModeType.Play);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        initialize();
                                        setButtonVisibilities(false);
                                    }
                                });
                            }
                        });
                        thread.start();
                    }
                });
        alertDlg.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel ボタンクリック処理
                    }
                });

        // 表示
        alertDlg.show();
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

    private void showResult(SstpResult result, int duration) {
        if(result.success()) {
            showToast(result.getMessage(), duration);
            return;
        }
        //エラー処理
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("エラー");
        dialog.setMessage(result.getExtraMessage());
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void setPlayIcon(Handler handler, boolean isPlaying) {
        final int id = isPlaying ? R.drawable.ic_btn_speak_now : R.drawable.ic_media_play;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(getActivity() == null) {
                    return;
                }
                Button btnPlay = (Button)getActivity().findViewById(R.id.button_play);
                btnPlay.setBackgroundResource(id);
            }
        });
    }

    public void update(final Message target) {
        final Message message = getMessage();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                setMessageCount();
                if(message == null) {
                    return;
                }
                //midが異なり、お知らせでもない
                if(!message.getMid().equals(target.getMid()) && !target.isForced()) {
                    return;
                }
                //todo forceBroadcast 対応
                setVote(message.getVotes());
                setAgree(message.getAgrees());
            }
        });
    }

    public void updateUsers() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                MainActivity activity = (MainActivity)getActivity();
                setUserText(R.id.all_users_text, activity.getAllUsers());
                setUserText(R.id.channel_users_text, activity.getChannelUsers());
            }
        });
    }

    private void setUserText(int textId, int value) {
        if(value <= 0) {
            return;
        }
        TextView textView = (TextView)getActivity().findViewById(textId);
        textView.setText(String.format("%d人", value));
    }
}
