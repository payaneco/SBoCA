package jp.payaneco.sboca;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.AbstractMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener, GestureDetector.OnGestureListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private SstpClient mSstpClient;
    private Fragment fragment;

    private List<Message> messageList;
    private String writerScript;
    private int selIndex;
    private ViewModeType viewMode;
    private Message broadcast;
    private Message rehearsal;
    private String targetChannel;
    private int allUsers;
    private int channelUsers;
    private String targetMid;
    private int newValue;

    //todo 画面遷移制御
    //todo チャンネルリスト表示
    //todo 修正機能追加

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewMode = ViewModeType.Initialize;
        writerScript = "\\t\\e";
        //todo NetworkInfoで接続確認
        //http://nvtrlab.jp/blog/penco/%E3%83%8D%E3%83%83%E3%83%88%E3%83%AF%E3%83%BC%E3%82%AF%E3%81%AE%E6%8E%A5%E7%B6%9A%E7%8A%B6%E6%B3%81%E3%82%92%E7%9B%A3%E8%A6%96%E3%81%99%E3%82%8B.html
        mSstpClient = new SstpClient(this, new AsyncCallback() {
            private SlppModeType slppMode = SlppModeType.none;
            @Override
            public void onPreExecute() {

            }

            @Override
            public void onPostExecute(int result) {

            }

            @Override
            public void onProgressUpdate(int progress) {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String command = mSstpClient.getCommand();
                        while (command != null) {
                            slppMode = getSlppMode(slppMode, command);
                            AbstractMap.SimpleEntry<String, String> entry = SstpClient.getCommandEntry(command);
                            if (entry == null) {
                                command = mSstpClient.getCommand();
                                continue;
                            }
                            switch (slppMode) {
                                case none:
                                    break;
                                case broadcastMessage:
                                case forceBroadcastMessage:
                                    if (broadcast.setEntry(entry)) {
                                        broadcast.createSpannedText();
                                        messageList.add(broadcast);
                                        updateFragment(broadcast);
                                        slppMode = SlppModeType.none;
                                    }
                                    break;
                                case allUsers:
                                    if (entry.getKey().equalsIgnoreCase("Num")) {
                                        allUsers = Integer.valueOf(entry.getValue());
                                        updateUsers();
                                        slppMode = SlppModeType.none;
                                    }
                                    break;
                                case channelUsers:
                                    if (setChannelUserEntry(entry)) {
                                        updateUsers();
                                        slppMode = SlppModeType.none;
                                    }
                                    break;
                                case channelList:
                                    break;
                                case broadcastInformation:
                                    break;
                                case forceBroadcastInformation:
                                    break;
                                case forceBroadcastInformationTypeVote:
                                    //todo メソッド化
                                    if (entry.getKey().equalsIgnoreCase("MID")) {
                                        targetMid = entry.getValue();
                                    } else if (entry.getKey().equalsIgnoreCase("Num")) {
                                        newValue = Integer.parseInt(entry.getValue());
                                    }
                                    if(!targetMid.isEmpty() && newValue > 0) {
                                        Message message = getMessage(targetMid);
                                        if(message != null) {
                                            message.setVotes(newValue);
                                            updateFragment(message);
                                        }
                                        slppMode = SlppModeType.none;
                                    }
                                    break;
                                case forceBroadcastInformationTypeAgree:
                                    if (entry.getKey().equalsIgnoreCase("MID")) {
                                        targetMid = entry.getValue();
                                    } else if (entry.getKey().equalsIgnoreCase("Num")) {
                                        newValue = Integer.parseInt(entry.getValue());
                                    }
                                    if(!targetMid.isEmpty() && newValue > 0) {
                                        Message message = getMessage(targetMid);
                                        if(message != null) {
                                            message.setAgrees(newValue);
                                            updateFragment(message);
                                        }
                                        slppMode = SlppModeType.none;
                                    }
                                    break;
                                case closeChannel:
                                    break;
                            }
                            command = mSstpClient.getCommand();
                        }
                    }
                });
            }

            @Override
            public void onCancelled() {

            }
        });
        mSstpClient.execute(Settings.getLuid(this));

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                                getString(R.string.title_section3),
                        }),
                this);
        navigateFragment(1);
    }

    private Message getMessage(String mid) {
        for(Message message : messageList) {
            if(mid.equals(message.getMid())) {
                return message;
            }
        }
        return null;
    }

    private boolean setChannelUserEntry(AbstractMap.SimpleEntry<String, String> entry) {
        switch (entry.getKey()) {
            case "Channel": targetChannel = entry.getValue(); break;
            case "Num": channelUsers = Integer.valueOf(entry.getValue()); break;
        }

        if(Settings.getCurrentChannel(this).equalsIgnoreCase(targetChannel)
                && channelUsers > 0) {
            return true;
        }
        return false;
    }

    private void updateFragment(Message message) {
        if(fragment == null) {
            return;
        }
        if(fragment instanceof PlaceholderFragment) {
            PlaceholderFragment placeholder = (PlaceholderFragment)fragment;
            placeholder.update(message);
        }
    }

    private void updateUsers() {
        if(fragment instanceof PlaceholderFragment) {
            PlaceholderFragment placeholder = (PlaceholderFragment)fragment;
            placeholder.updateUsers();
        }
    }

    public int getAllUsers() {
        return allUsers;
    }

    public int getChannelUsers() {
        return channelUsers;
    }

    private SlppModeType getSlppMode(SlppModeType slppMode, String command) {
        switch (command.trim()) {
            case "broadcastMessage":
                broadcast = new Message(false);
                return SlppModeType.broadcastMessage;
            case "forceBroadcastMessage":
                broadcast = new Message(true);
                return SlppModeType.forceBroadcastMessage;
            case "allUsers": return SlppModeType.allUsers;
            case "channelUsers":
                targetChannel = "";
                channelUsers = 0;
                return SlppModeType.channelUsers;
            case "channelList": return SlppModeType.channelList;
            case "broadcastInformation": return SlppModeType.broadcastInformation;
            case "forceBroadcastInformation": return SlppModeType.forceBroadcastInformation;
            case "closeChannel": return SlppModeType.closeChannel;
        }
        if(slppMode.equals(SlppModeType.forceBroadcastInformation)) {
            switch (command.trim()) {
                case "Type: Vote":
                    targetMid = "";
                    newValue = 0;
                    return SlppModeType.forceBroadcastInformationTypeVote;
                case "Type: Agree":
                    targetMid = "";
                    newValue = 0;
                    return SlppModeType.forceBroadcastInformationTypeAgree;
            }
        }
        if(slppMode == null) {
            return SlppModeType.none;
        }
        return slppMode;
    }

    public void setChannels() {
        mSstpClient.setChannels();
    }

    public String getLog() {
        return mSstpClient.getLog();
    }

    public void closeChannels() {
        mSstpClient.closeChannels();
    }

    public BroadcastResult postMessage(Message message) {
        String channel = Settings.getCurrentChannel(this).trim();
        String ghost = Settings.getCurrentGhost(this).trim();
        return mSstpClient.postMessage(message, channel, ghost);
    }

    public VoteResult postVote(String mid) {
        return mSstpClient.postVote(mid);
    }

    public VoteResult postAgree(String mid) {
        return mSstpClient.postAgree(mid);
    }

    public ChannelMap getChannels() {
        return mSstpClient.getChannels();
    }

    public void navigateFragment(int index) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSelectedNavigationItem(index);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id)
        {
            case R.id.action_settings:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                return true;
            case R.id.action_sstp:
                Uri uri = Uri.parse(getString(R.string.url_sstp));
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        switch (position) {
            case 0: fragment = new ListFragment(); break;
            case 1: fragment = new PlaceholderFragment(); break;
            case 2: fragment = new WriteFragment(); break;
            default: return false;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mSstpClient.closeConnection();
            }
        });
        thread.start();
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
        selIndex = 0;
    }

    public int getSelIndex() {
        selIndex = Math.min(selIndex, messageList.size() - 1);
        return selIndex;
    }

    public void setSelIndex(int selIndex) {
        this.selIndex = selIndex;
    }

    public ViewModeType getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewModeType viewMode) {
        this.viewMode = viewMode;
    }

    public String getWriterScript() {
        return writerScript;
    }

    public void setWriterScript(String script) {
        this.writerScript = script;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Toast.makeText(this, String.format("%s, %s", velocityX, velocityY), Toast.LENGTH_SHORT).show();
        return false;
    }

    public Message getRehearsal() {
        return rehearsal;
    }

    public void setRehearsal(Message rehearsal) {
        this.rehearsal = rehearsal;
    }
}
