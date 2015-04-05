package jp.payaneco.sboca;

import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by payaneco on 15/01/17.
 */
public class Message {
    public  static final String PLACE_GREETINGS = "SBoCA";

    private Date sendTime;
    private String mid;
    private String ghost;
    private int users;
    private int votes;
    private int agrees;
    private String place;
    private String script;
    private boolean forced;
    private Spanned spannedText;
    private String dialogMessage;

    private static Message getGreetings(String script) {
        Message message = new Message();
        message.setScript(script);
        return message;
    }

    public static Message getGreetings() {
        StringBuilder sb = new StringBuilder();
        sb.append("\\t\\u\\s[10]\\h\\s[5]このたびは\\nSstp Bottle Client for Android、\\w5\\n略して\\w5\"SBoCA\"\\w5をダウンロードしてくれて、\\w5ありがとーございます！\\w9\\w9\\n\\n");
        sb.append("\\u\\s[26]…\\w5ありがとうございます。\\w9\\w9\\n\\n");
        sb.append("\\h\\s[38]酔狂って…\\w5。\\w9\\n\\n\\u失礼、\\w5噛みました。\\w9\\w9\\n\\n");
        sb.append("\\h\\s[58]噛み…\\w5？\\w9\\n\\s[238]と、\\w5ともかくっ、\\w5\\s[5]SBoCAはアンドロイド専用の\\w5ボトルクライアントです！\\w9\\w9\\n\\n");
        sb.append("\\u\\s[10]まだ開発プレビュー版です。\\w9\\n不具合や使いにくい点は…\\w5笑って流してください。\\w9\\w9\\n\\n");
        sb.append("\\h\\s[3]流すって…\\w5ボトルに？\\w9\\w9\\n\\n");
        sb.append("\\u\\s[26]違います…\\w5。\\w9いえ、\\w5それはそれで。\\w9\\nLUIDは画面右上のメニューから取得や設定ができます。\\w9\\w9\\n\\n");
        sb.append("\\h\\s[5]投稿先のチャンネルやゴーストも、\\w5そこから設定できます。\\w9\\n詳しくはリンク先を見てね！\\w9\\w9\\n\\n");
        sb.append("\\u\\s[10]最後に、\\w5SBoCAの作者は\\w5\\nSSTP Bottleの作者様とは、\\w5一切関係がありません。\\w9\\w9\\n\\n");
        sb.append("\\h\\s[5]SBoCAに関するお問い合わせを\\w5そちらに投げることは、\\w5どうかご遠慮ください。\\w9\\w9\\n\\n");
        sb.append("\\_sそれでは、\\w5よいボトルライフを！！\\_s");

        sb.append("\\e");

        return getGreetings(sb.toString());
    }

    public static Message getGreetings(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\t\\u\\s[10]\\h\\s[5]瓶を集めてきたよー\\w9\\w9\\u\\s[10]集まった瓶の数は");
        sb.append(count).append("瓶でした。\\n\\n\\w9\\w9");
        sb.append("\\h\\s[3]…\\w5って、\\w5過去ログ取ってくると一覧からここに戻っちゃうのね…\\w5。\\n\\n\\w9\\w9");
        sb.append("\\u残念ながら仕様です。\\n\\w9\\s[26]作者のスキル不足に起因する残念な仕様です。\\w9\\w9");
        sb.append("\\h\\s[38]み…\\w5身もふたもないわね。");

        sb.append("\\e");
        return getGreetings(sb.toString());
    }

    public static Message getGreetings(int span, int count){
        StringBuilder sb = new StringBuilder();
        sb.append("\\t\\u\\s[10]\\h\\s[5]");

        if(span < 30) {
            sb.append("さっきぶりー！\\n\\n\\u\\s[26]どうも。\\n\\w9\\w9");
        }
        else {
            sb.append("ボトル界へようこそっ！\\n\\n\\u\\s[26]");
            int dHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if(5 <= dHour && dHour < 12) {
                sb.append("おはようございます。");
            }
            else if(dHour <= 12 && dHour < 18) {
                sb.append("こんにちは。");
            }
            else
            {
                sb.append("こんばんは。");
            }
            sb.append("\\n\\n\\w9\\w9");
        }
        sb.append("\\h\\s[3]再生済みの瓶で一番新しいのは…\\w5いつの瓶だっけ？\\n\\n\\w9\\w9\\u\\s[10]ええと…\\w5");
        int hour = span / 60;
        if(span >= Settings.MAX_SPAN) {
            sb.append(hour).append("時間以上前でした。");
        } else {
            if (hour > 0) {
                sb.append(hour).append("時間");
            }
            int minute = span % 60;
            sb.append(minute).append("分前ですね。");
        }
        sb.append("\\w9\\n\\uそれ以降の投瓶数は \\w5").append(count).append("瓶です。\\n\\n\\w9\\w9\\h\\s[5]");
        if(count <= 1){
            sb.append("静ボトの静ボトによる静ボトのための瓶数ッ！");
        }
        else if(count < 50) {
            sb.append("へへ…\\w5きたぜ\\w5ぬるりと。\\n\\w9新しい瓶が…！");
        } else {
            sb.append("読む瓶が増えるよ。\\w9やったね！");
        }

        sb.append("\\n\\n\\w9\\u\\s[26]はいはい、\\w5そうですね。\\n\\w9\\w9");
        sb.append("\\hそれじゃ、\\w5\\s[5]ゆっくりしていってね！");

        sb.append("\\e");
        return getGreetings(sb.toString());
    }

    private Message() {
        setSendTime(new Date());
        setPlace(PLACE_GREETINGS);
        setGhost("");
        setUsers(-1);
        setVotes(-1);
        setAgrees(-1);
        forced = false;
    }

    public Message(String log){
        String[] ss = log.split("\t");
        setSendTime(ss[0]);
        setMid(ss[1]);
        setPlace(ss[2]);
        setGhost(ss[3]);
        setUsers(ss[4]);
        setVotes(ss[5]);
        setAgrees(ss[6]);
        setScript(ss[7]);
        forced = false;
    }

    public Message(boolean isForced) {
        setSendTime(new Date());
        setMid("");
        setScript("");
        setPlace(isForced ? "お知らせ" : "");
        setGhost("");
        setUsers(0);
        setVotes(0);
        setAgrees(0);
        forced = isForced;
        dialogMessage = "";
    }

    public boolean setEntry(AbstractMap.SimpleEntry<String, String> entry) {
        String value = entry.getValue();
        switch (entry.getKey()) {
            case "MID": setMid(value); break;
            case "Script": setScript(value); break;
            case "IfGhost": setGhost(value); break;
            case "Channel": setPlace(value); break;
        }
        return isReady();
    }

    private boolean isReady() {
        if(mid.isEmpty() || script.isEmpty() || ghost.isEmpty() || place.isEmpty()) {
            return false;
        }
        if(forced && dialogMessage.isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean isPostEnabled() {
        if(ghost.isEmpty() || getScript(true).isEmpty()) {
            return false;
        }
        for(Phrase phrase : getPhraseList()) {
            if(phrase.getCommand().equals(CommandType.Unhandled)) {
                return false;
            }
        }
        return true;
    }

    public LinkedList<Phrase> getPhraseList() {
        return Phrase.parseScript(getScript());
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public void setSendTime(String sendTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddkkmmss");
        try {
            this.sendTime = format.parse(sendTime.trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getLinkMap() {
        for(Phrase phrase : getPhraseList()) {
            HashMap<String, String> map = phrase.getLinkMap();
            if(map != null) {
                return map;
            }
        }
        return null;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid.trim();
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(String users) {
        setUsers(Integer.parseInt(users));
    }

    public void setUsers(int users) {
        this.users = users;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(String votes) {
        setVotes(Integer.parseInt(votes));
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public int getAgrees() {
        return agrees;
    }

    public void setAgrees(String agrees) {
        setAgrees(Integer.parseInt(agrees));
    }

    public void setAgrees(int agrees) {
        this.agrees = agrees;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place.trim();
    }

    public String getScript() {
        return getScript(false);
    }

    public String getScript(boolean isTrim) {
        if(!isTrim) {
            return script;
        }
        Pattern p = Pattern.compile("\\\\t(.*)\\\\e");
        Matcher m = p.matcher(script);
        if(!m.find()){
            return script;
        }
        return m.group(1);
    }

    public void setScript(String text) {
        String script = LogBuffer.getScript(text, true);
        //\\t\\eを付加
        Pattern p = Pattern.compile("^\\\\t(.*)\\\\e$");
        Matcher m = p.matcher(script);
        if(!m.find()){
            script = String.format("\\t%s\\e", script);
        }
        this.script = script;
    }

    public boolean isForced() {
        return forced;
    }

    public String getGhost() {
        return ghost;
    }

    public void setGhost(String ghost) {
        this.ghost = ghost;
    }

    public boolean isGreetings() {
        return place.equalsIgnoreCase(PLACE_GREETINGS);
    }

    public String getStatus() {
        final String STATUS_FORMAT = "%1$s@%2$s [%3$s]";
        CharSequence date = DateFormat.format("yyyy/MM/dd kk:mm:ss", getSendTime());
        return String.format(STATUS_FORMAT, getGhost(), getPlace(), date);
    }

    public Spanned getSpannedText() {
        return spannedText;
    }

    public void createSpannedText() {
        if(isGreetings()) {
            spannedText = Html.fromHtml("投稿された瓶一覧 <font color=\"Navy\">非同期で一覧を生成します</font>");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<b>[").append(getGhost());
        int vote = getVotes();
        if(vote > 0) {
            sb.append(" <font color=\"Red\">投票:</font>").append(getMarkUpPoint(vote));
        }
        int agree = getAgrees();
        if(agree > 0) {
            sb.append(" <font color=\"Green\">同意:</font>").append(getMarkUpPoint(agree));
        }
        sb.append("]</b>");
        for(Phrase phrase : getPhraseList()) {
            String text = phrase.getContext();
            if(text == null || text.isEmpty()) {
                continue;
            }
            String colorName = getActorColor(phrase);
            sb.append(getColorTaggedText(colorName, text));
        }
        SimpleDateFormat format = new SimpleDateFormat("(kk:mm)");
        sb.append(format.format(getSendTime()));
        addLinkTexts(sb);
        spannedText =  Html.fromHtml(sb.toString());
    }

    private void addLinkTexts(StringBuilder sb) {
        HashMap<String, String> map = getLinkMap();
        if(map == null) {
            return;
        }
        for(String key : map.keySet()) {
            sb.append("<a href=\"").append(map.get(key)).append("\">");
            sb.append("[").append(key).append("]").append("</a>");
        }
    }

    private String getMarkUpPoint(int point) {
        String colorName;
        switch (point) {
            case 0: return String.valueOf(point);
            case 1:case 2: colorName = "#000080"; break;
            case 3:case 4: colorName = "#00CC00"; break;
            default: colorName = "#FF0000"; break;
        }
        return getColorTaggedText(colorName, String.valueOf(point));
    }

    private String getColorTaggedText(String colorName, String text) {
        String html = text;
        return String.format("<font color=\"%s\">%s</font>", colorName, htmlEscape(html));
    }
    /**
     * <p>[概 要] HTMLエスケープ処理<br/>
     * http://write-remember.com/program/java/html_escape/</p>
     * <p>[詳 細] </p>
     * <p>[備 考] </p>
     * @param  str 文字列
     * @return HTMLエスケープ後の文字列
     */
    private static String htmlEscape(String str){
        StringBuffer result = new StringBuffer();
        for(char c : str.toCharArray()) {
            switch (c) {
                case '&' :
                    result.append("&amp;");
                    break;
                case '<' :
                    result.append("&lt;");
                    break;
                case '>' :
                    result.append("&gt;");
                    break;
                case '"' :
                    result.append("&quot;");
                    break;
                case '\'' :
                    result.append("&#39;");
                    break;
                case ' ' :
                    result.append("&nbsp;");
                    break;
                default :
                    result.append(c);
                    break;
            }
        }
        return result.toString();
    }

    private String getActorColor(Phrase phrase) {
        String colorName;
        switch (phrase.getActor()) {
            //todo 色を定数化
            case Hontai: colorName = "#cc1199"; break;
            case Unyu: colorName = "#222277"; break;
            case Synchronized: colorName = "#d2691e"; break;
            default: return "";
        }
        return colorName;
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    public void insert(ActorType actor, int surfaceNo, String text, boolean isScript) {
        insert(Integer.MAX_VALUE, actor, surfaceNo, text, isScript);
    }

    public void insert(int position, ActorType actor, int surfaceNo, String text, boolean isScript) {
        LogBuffer hb = new LogBuffer(ActorType.Hontai);
        LogBuffer ub = new LogBuffer(ActorType.Unyu);
        StringBuilder sb = new StringBuilder();
        int p = 0;
        for(Phrase phrase : getPhraseList()) {
            sb.append(phrase.getScript());
            hb.tryAdd(phrase);
            ub.tryAdd(phrase);
            switch (phrase.getCommand()) {
                case Hontai:
                case Unyu:
                case Surface:
                    p++;
                    break;
            }
        }
        LogBuffer buffer = ActorType.Hontai.equals(actor) ? hb : ub;
        LogBuffer opposition = ActorType.Hontai.equals(actor) ? ub : hb;
        sb.insert(0, buffer.getPrefix(surfaceNo, isScript));
        sb.append(buffer.getScript(p, surfaceNo, text, opposition.getContext(), isScript));
        sb.insert(0, "\\t");
        sb.append("\\e");
        //末尾のウェイトを強制的に除去
        String script = replaceAll(sb.toString(), "(\\\\w\\d)+(\\\\e)$", "$2");
        setScript(script);
    }

    public void modify(int position, String script) {
        int p = 0;
        boolean isAdded = false;
        StringBuilder sb = new StringBuilder();
        if(position < 0) {
            sb.append(script);
            isAdded = true;
        }
        for(Phrase phrase : getPhraseList()) {
            if(p < position || position < p) {
                sb.append(phrase.getScript());
            } else if(!isAdded) {
                sb.append(script);
                isAdded = true;
            }
            switch (phrase.getCommand()) {
                case Hontai:
                case Unyu:
                case Surface:
                    p++;
                    break;
            }
        }
        setScript(sb.toString());
    }

    private String replaceAll(String input, String pattern, String replacement) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        return m.replaceAll(replacement);
    }
}
