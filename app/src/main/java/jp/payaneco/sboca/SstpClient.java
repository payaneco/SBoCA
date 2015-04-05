package jp.payaneco.sboca;


import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by payaneco on 15/03/08.
 */
public class SstpClient extends AsyncTask<String, Integer, Integer> {
    private DefaultHttpClient httpClient;
    private MainActivity activity;
    private AsyncCallback _asyncCallback = null;;
    private String luid;
    private Thread slppThread;
    private HttpResponse slppResponse;
    private int progress;
    private ChannelMap channelMap;
    private Queue<String> queue;

    public SstpClient(MainActivity activity, AsyncCallback asyncCallback) {
        this.activity = activity;
        this._asyncCallback = asyncCallback;
        progress = 0;
        queue = new ConcurrentLinkedQueue<>();
        logBuilder = new StringBuilder();
    }

    public ChannelMap getChannels() {
        if(channelMap != null) {
            return channelMap;
        }
        try {
            BufferedReader reader = post("getChannels", true);
            try {
                channelMap = new ChannelMap();
                String line;
                while ((line = reader.readLine()) != null) {
                    channelMap.putCommand(line);
                }
                return channelMap;
            } finally {
                reader.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BufferedReader post(String command, boolean isAddLuid) throws IOException {
        String postString = getPostBuilder(command, isAddLuid).toString();
        return post(postString);
    }

    public static BufferedReader post(String luid) throws IOException {
        URL url = new URL("http://bottle.mikage.to/bottle2.cgi");
        URLConnection uc = url.openConnection();
        uc.setDoOutput(true);//POST可能にする

        uc.setRequestProperty("User-Agent", "SBoCA Ver 0.0.1");// ヘッダを設定
        uc.setRequestProperty("Accept-Language", "ja");// ヘッダを設定
        OutputStream os = uc.getOutputStream();//POST用のOutputStreamを取得

        PrintStream ps = new PrintStream(os, true, "SJIS");
        ps.print(luid);//データをPOSTする
        ps.close();

        InputStream is = uc.getInputStream();//POSTした結果を取得
        return new BufferedReader(new InputStreamReader(is, "SJIS"));
    }

    private StringBuilder getPostBuilder(String command, boolean isAddLuid) {
        StringBuilder sb = getPostBuilder(command);
        if(isAddLuid) {
            sb.append("LUID: ").append(luid).append("\r\n");
        }
        return sb;
    }

    private static StringBuilder getPostBuilder(String command) {
        StringBuilder sb = new StringBuilder();
        sb.append("Command: ").append(command).append("\r\n");
        sb.append("Agent: ").append("SBoCA Ver 0.0.1").append("\r\n");
        return sb;
    }

    public static AbstractMap.SimpleEntry<String, String> getCommandEntry(String command) {
        Pattern pattern = Pattern.compile("^(\\w+): *(.+)$");
        Matcher matcher = pattern.matcher(command);
        if(!matcher.find()) {
            return null;
        }
        String key = matcher.group(1);
        String value = matcher.group(2).trim();
        return new AbstractMap.SimpleEntry<String, String>(key, value);
    }

    public static NewIdResult getNewId() {
        NewIdResult result = new NewIdResult();
        StringBuilder sb = getPostBuilder("getNewId");
        result.execute(sb.toString());
        return result;
    }

    public void setChannels() {
        try {
            StringBuilder sb = getPostBuilder("setChannels", true);
            ChannelMap map = getChannels();
            sb.append("Num: ").append(map.size()).append("\r\n");
            for(Channel channel : map.values()) {
                //Entryじゃなかった！
                String entry = String.format("Ch%d: %s", channel.getId(), channel.getName());
                sb.append(entry).append("\r\n");
            }
            BufferedReader reader = post(sb.toString());
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    //todo エラー処理
                    Settings.setChannelMap(map);
                }
            } finally {
                reader.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VoteResult postVote(String mid) {
        return sendVoteMessage(mid, "Vote");
    }

    public VoteResult postAgree(String mid) {
        return sendVoteMessage(mid, "Agree");
    }

    private VoteResult sendVoteMessage(String mid, String type) {
        VoteResult result = new VoteResult(mid, type);
        if(!result.isReady()) {
            return result;
        }
        StringBuilder sb = getPostBuilder("voteMessage", true);
        sb.append("MID: ").append(mid).append("\r\n");
        sb.append("VoteType: ").append(type).append("\r\n");
        result.execute(sb.toString());
        return result;
    }

    public BroadcastResult postMessage(Message message, String channel, String ghost) {
        BroadcastResult result = new BroadcastResult(channel, ghost, message);
        if(!result.isReady()) {
            return result;
        }
        StringBuilder sb = getPostBuilder("sendBroadcast", true);
        sb.append("Channel: ").append(channel).append("\r\n");
        sb.append("Talk: ").append(message.getScript()).append("\r\n");
        sb.append("Ghost: ").append(ghost).append("\r\n");
        try {
            result.parse(post(sb.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void closeChannels() {
        StringBuilder sb = getPostBuilder("setChannels", true);
        sb.append("Num: 0").append("\r\n");
        try {
            post(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private StringBuilder logBuilder;

    public String getLog() {
        return logBuilder.toString();
    }

    public String getCommand() {
        return queue.poll();
    }

    public void readAsync() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(slppResponse.getEntity().getContent(), "SJIS"));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                progress++;
                _asyncCallback.onProgressUpdate(progress);
                queue.offer(line);
                logBuilder.append(line).append("\r\n");
            }
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
    }

    public void closeConnection() {
        closeChannels();
        if(httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    @Override
    protected Integer doInBackground(String... contents) {
        luid = contents[0];
        if(luid.isEmpty()) {
            return null;
        }
        HttpPost request = new HttpPost("http://bottle.mikage.to:9871/");
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair(luid, ""));
        try {
            request.setEntity(new UrlEncodedFormEntity(list, "SJIS"));
            // HTTPリクエスト発行
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 0);
            HttpConnectionParams.setSoTimeout(params, 0);
            httpClient = new DefaultHttpClient(params);
            slppResponse = httpClient.execute(request);
            Integer statusCode = slppResponse.getStatusLine().getStatusCode();
            if(!statusCode.equals(200)) {
                return statusCode;
            }
            //ログを読みつづける
            slppThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        readAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            slppThread.setPriority(Thread.MIN_PRIORITY);
            slppThread.start();
            // チャンネル取得
            channelMap = getChannels();
            if(channelMap != null) {
                setChannels();
            }
            return statusCode;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this._asyncCallback.onPreExecute();
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        this._asyncCallback.onProgressUpdate(values[0]);
    }

    protected void onPostExecute(int result) {
        super.onPostExecute(result);
        this._asyncCallback.onPostExecute(result);
    }

    protected void onCancelled() {
        super.onCancelled();
        this._asyncCallback.onCancelled();
    }
}
