package jp.payaneco.sboca;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ApplicationTestCase;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    /*
     ききたいことリスト
     * URLが8個以上の場合の処理
     * httpから始まらないリンクの許容
     * サーフィス番号の最小値、最大値
     * SVG SJIS以外の処遇…
     * アイコンは http://jp.freepik.com/free-vector/various-sketch-elements-of-vector-material---food-and-drink---elements_580659.htm
     todo ユーザを正しく表示
     todo 過去ログを逐次表示
     todo コメントしっかりつける
     todo シーケンシャルなサーフェス表示
     todo 設定画面のエラー回避・場所の即時設定
     todo ページ整備
     todo 機内モード時のエラー処理
     todo ghost.txtをzipで取得
     */

    public static final String TEST_LUID = "LiXq2BUxh.w43YjMriaw5584RaUsgWBzyoLrKNLpOp7j.sSd1.nGiXq7L.2SCpVVEDrjc9.wHt41SDAOLF";


    public ApplicationTest() {
        super(Application.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
/*
    public void testClient() throws Exception {
        SbocaClient client = new SbocaClient();
        assertEquals("http://www.payaneco.jp/sboca/Ghost/infinity%20zero.txt", SbocaClient.getUrl("Ghost\\infinity zero.txt"));
///*
        assertEquals("<html><body>Hello bottle!</body></html>", client.getLog());
        InputStream input = null;
        try {
            byte[] bytes = SbocaClient.getImage("Ghost\\infinity zero.png");

            Bitmap web = getBitmap(bytes);
            byte[] buffer = new byte[bytes.length];
            input = getContext().getResources().openRawResource(R.raw.conductor);
            input.read(buffer);
            Bitmap raw = getBitmap(buffer);
            byte[] wBytes = getBytes(web);
            byte[] rBytes = getBytes(raw);
            testBytes(wBytes, rBytes);
            GhostStorage ghostStorage = new GhostStorage(getContext());
            String ghostName = "∞";
            ghostStorage.deleteFromStorage(ghostName);
            ghostStorage.summon(ghostName);
            byte[] gBytes = ghostStorage.getGhostSurface(ghostName);
            testBytes(wBytes, gBytes);

        } finally {
            input.close();
        }*/
/*    }

    private Bitmap getBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 10, output);
        return output.toByteArray();
    }

    private void testBytes(byte[] b1, byte[] b2) {
        for(int i = 0; i < b1.length; i++) {
            if(b1[i] != b2[i]) {
                fail(String.valueOf(i));
            }
        }
    }

    public void testConnection() {
        //SstpClient client = new SstpClient();
        //String s = client.startConnection(TEST_LUID);
        //assertEquals("200", s);
    }

    public void testMessage() throws ParseException {
        String log = "20150117093617\t54b9ae8100000001\t駅前繁華街\tさくら\t37\t3\t4\t\\t\\u\\s[10]\\h\\s[0]久しぶりに妖怪ウォッチ列できてた。\\w9\\w9\\uふーん。\\e";
        Message message = new Message(log);

        java.util.Date expDate = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss").parse("2015/01/17 09:36:17");
        assertEquals(expDate, message.getSendTime());
        assertEquals("54b9ae8100000001", message.getMid());
        assertEquals("駅前繁華街", message.getPlace());
        assertEquals("さくら", message.getGhost());
        assertEquals(37, message.getUsers());
        assertEquals(3, message.getVotes());
        message.setVotes(1);
        assertEquals(1, message.getVotes());
        assertEquals(4, message.getAgrees());
        message.setAgrees(2);
        assertEquals(2, message.getAgrees());

        assertEquals("ふーん。", message.getPhraseList().peekLast().getContext());

        message.setScript("\\t\\u\\s[10]\\h\\s[0]てすと。\\w9\\w9\\u\\URL[http://test/]\\e");
        HashMap<String, String> map = message.getLinkMap();
        assertEquals("http://test/", map.get("リンク"));
        message.setScript("\\t\\u\\s[10]\\h\\s[0]てすと。\\w9\\w9\\u\\URL[あああ][http://test/1][いいい][http://test/2][ううう]\\e");
        map = message.getLinkMap();
        assertEquals("http://test/1", map.get("あああ"));
        assertEquals("http://test/2", map.get("いいい"));
    }

    public void testPhrase(){
        Phrase phrase = new Phrase();
        assertEquals(ActorType.Hontai, phrase.getActor());
        phrase = new Phrase(phrase, "w9");
        assertEquals(9, phrase.getWaitTime());
        phrase = new Phrase(phrase, "w12");
        assertEquals(1, phrase.getWaitTime());

        assertEquals(CommandType.Hontai, Phrase.getCommand("h"));
        assertEquals(CommandType.Unyu, Phrase.getCommand("u"));
        assertEquals(CommandType.Surface, Phrase.getCommand("s[10]"));
        assertEquals(CommandType.Surface, Phrase.getCommand("s[-1]"));
        assertEquals(CommandType.Unhandled, Phrase.getCommand("s[-2]"));
        assertEquals(CommandType.Unhandled, Phrase.getCommand("s[]"));
        assertEquals(CommandType.Surface, Phrase.getCommand("s[65535]"));
        assertEquals(CommandType.Unhandled, Phrase.getCommand("s[65536]"));
        assertEquals(CommandType.Unhandled, Phrase.getCommand("s[1.5]"));
        assertEquals(CommandType.Unhandled, Phrase.getCommand("s[abc]"));
        assertEquals(CommandType.Unhandled, Phrase.getCommand("s10"));
        assertEquals(CommandType.NewHalfLine, Phrase.getCommand("n[half]"));
        assertEquals(CommandType.NewLine, Phrase.getCommand("n"));
        assertEquals(CommandType.NewLine, Phrase.getCommand("nhalf"));
        assertEquals(CommandType.NewLine, Phrase.getCommand("n[harf]"));
        assertEquals(CommandType.Clear, Phrase.getCommand("c"));
        assertEquals(CommandType.Wait, Phrase.getCommand("w9"));
        assertEquals(CommandType.Wait, Phrase.getCommand("w5"));
        assertEquals(CommandType.Unhandled, Phrase.getCommand("w[5]"));
        assertEquals(CommandType.Unhandled, Phrase.getCommand("wait"));
        assertEquals(CommandType.Synchronized, Phrase.getCommand("_s"));
        assertEquals(CommandType.QuickSession, Phrase.getCommand("_q"));
        assertEquals(CommandType.MultipleURL, Phrase.getCommand("URL[どこにも行きたくない][http://nh.mikage.to/][板][http://bottle.mikage.to/][瓶]"));
        assertEquals(CommandType.SimpleURL, Phrase.getCommand("URL[http://a/]"));
        LinkedList<Phrase> list = Phrase.parseScript("\\t\\\\u\\e");//¥t¥¥h¥e エスケープのテスト
        assertEquals(CommandType.Escape, list.get(0).getCommand());
        list = Phrase.parseScript("\\t\\u\\s[10]\\h\\s[0]\\e");
        assertEquals(ActorType.Unyu, list.get(0).getActor());
        assertEquals(ActorType.Unyu, list.get(1).getActor());
        assertEquals(ActorType.Hontai, list.get(2).getActor());
        assertEquals(-1, list.get(0).getSurfaceNo());
        assertEquals(10, list.get(1).getSurfaceNo());
        assertEquals(0, list.get(3).getSurfaceNo());

        //String src = "\\t\\u\\s[10]\\h\\s[4]RPGやシミュレーションで、\\w5どの仲間を使うかというのは悩ましい問題だよね。\\w9\\w9\\u\\s[13]使わない仲間は育たないから後から交代も厳しい。\\w9今だけでなく先のことも考えないと。\\w9\\w9\\h\\n\\nそして面倒くさくなってくる。\\w9\\w9\\u\\n\\nプレイヤーが弱すぎる…\\w9…\\w9。\\e";
        //LinkedList<Phrase> list = Phrase.parseScript(src);
        //assertEquals(25, list.size());
    }

    public void testGhost(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("test.txt"), "SJIS"));
            Ghost ghost = new Ghost(reader);
            assertEquals("∞", ghost.getSakura());
            assertEquals("0", ghost.getUnyu());
            assertEquals("%usernameさん", ghost.getUser());
            assertEquals("ぜろでばいど", ghost.getCraftman());
            assertEquals("http://www.magi-system.org/", ghost.getUrl());
            assertEquals("MAGI-SYSTEM" + System.getProperty("line.separator") + "なのです", ghost.getHpname());
            assertEquals("infinity zero.png", ghost.getSurfacefile());
            assertEquals("2005/12/10", ghost.getUpdate());
            assertEquals("てすと", ghost.getSakura2());
            assertEquals("さるまた", ghost.getGhostname());
            assertEquals("NORMAL", ghost.getSupported());
            assertFalse(ghost.canSakuraAction());
            assertEquals("ab" + System.getProperty("line.separator") + "cd", ghost.getInform());
            assertEquals(9, ghost.getSurfaceIndex(10));
            assertEquals(63, ghost.getSurfaceIndex(998));
            assertEquals(-1, ghost.getSurfaceIndex(99999));
            Integer[] alias = ghost.getAlias(ActorType.Hontai, 1);
            assertEquals(51, (int)alias[0]);
            assertEquals(41, (int)alias[1]);
            alias = ghost.getAlias(ActorType.Hontai, 2);
            assertEquals(42, (int)alias[0]);
            assertEquals(52, (int)alias[1]);
            alias = ghost.getAlias(ActorType.Unyu, 93);
            assertEquals(2000, (int) alias[0]);
            Iterator<String> iterator = ghost.getSequence(30).iterator();
            assertEquals("31", iterator.next());
            assertEquals("32", iterator.next());
            assertEquals("32", iterator.next());
            assertEquals("33", iterator.next());
            assertEquals("33", iterator.next());
            assertEquals("33", iterator.next());
            assertEquals("*", iterator.next());
            iterator = ghost.getSequence(34).iterator();
            assertEquals("35", iterator.next());
            assertEquals("-2", iterator.next());

            ghost.setDataFromToken("", "sakura=ぱにぽに");
            assertEquals("ぱにぽに", ghost.getSakura());
            ghost.setDataFromToken("sakura", "ぺったん");
            assertEquals("ぱにぽに" + System.getProperty("line.separator") + "ぺったん", ghost.getSakura());
            ghost.setDataFromToken("", "unyu=\"ぽにぱに\"");
            assertEquals("ぽにぱに", ghost.getUnyu());
            ghost.setDataFromToken("", "sakura-action=\"OK\"");
            assertTrue(ghost.canSakuraAction());
            ghost.setDataFromToken("", "surface=0:0");
            assertEquals(0, ghost.getSurfaceIndex(0));
            ghost.setDataFromToken("", "surface=400-410:*");
            for(int i = 400; i <= 410; i++) {
                assertEquals(i, ghost.getSurfaceIndex(i));
            }
        }catch (IOException e){
            fail();
        }
    }

    public void testGhostStrage() {
        GhostStorage ghostStorage = new GhostStorage(getContext());
        assertTrue(ghostStorage.hasWebStorage("あかね"));
        assertEquals("http://www.payaneco.jp/sboca/Ghost/akane.txt", ghostStorage.getGhostUrl("あかね"));
        String ghostName = "．さくら";
        //ghostStorage.deleteFromStorage(ghostName);
        //assertFalse(ghostStorage.hasLocalStorage(ghostName));
        try {
            Ghost ghost = ghostStorage.summon(ghostName);
            assertTrue(ghostStorage.hasLocalStorage(ghostName));
            assertEquals("うにゅう", ghost.getUnyu());
            assertEquals("．さくらStation", ghost.getHpname());
        } catch (IOException e) {
            fail();
        }
        ghostStorage.deleteFromStorage(ghostName);
    }

    public void testSettings() {
        Date current = Settings.getLastLogTime(getContext());
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        try {
            Settings.setLastLogTime(getContext(), format.parse("2001/12/31 23:59:59"));
            assertEquals("2001/12/31 23:59:59", format.format(Settings.getLastLogTime(getContext())));
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            Settings.setLastLogTime(getContext(), current);
        }
    }

    public void testChannel() {
        ChannelMap map = new ChannelMap();
        Channel channel = map.putCommand("CH1_name: hoge");
        assertEquals(1, channel.getId());
        map.putCommand("CH1_ghost: ごーすと");
        map.putCommand("CH1_name: お名前");
        map.putCommand("CH1_info: 説明");
        map.putCommand("CH1_warnpost: 0");
        map.putCommand("CH1_nopost: 1");
        map.putCommand("CH1_count: 40");
        assertEquals("お名前", channel.getName());
        assertEquals("ごーすと", channel.getGhost());
        assertEquals("0", channel.getWarnPost());
        assertEquals("1", channel.getNoPost());
        assertEquals("40", channel.getCount());
        assertEquals("説明", channel.getInfo());
        channel = map.putCommand("CH2_name: fuga");
        assertEquals(2, channel.getId());
    }
*/
    public void testMessageWriter() {
        Message message = new Message(false);
        assertEquals("\\t\\e", message.getScript());
        message.insert(ActorType.Hontai, 0, "あ。", false);
        assertEquals("\\t\\h\\s[0]あ。\\e", message.getScript());
        message.insert(ActorType.Unyu, 10, "う！", false);
        assertEquals("\\t\\u\\s[10]\\h\\s[0]あ。\\w9\\w9\\uう！\\e", message.getScript());
        message.insert(ActorType.Unyu, 11, "ん？", false);
        assertEquals("\\t\\u\\s[10]\\h\\s[0]あ。\\w9\\w9\\uう！\\w9\\s[11]ん？\\e", message.getScript());
        message.insert(ActorType.Hontai, 5, "くっ…。", false);
        assertEquals("\\t\\u\\s[10]\\h\\s[0]あ。\\w9\\w9\\uう！\\w9\\s[11]ん？\\w9\\w9\\h\\n\\n\\s[5]くっ…\\w5。\\e", message.getScript());
        message.insert(ActorType.Hontai, 5, "", false);
        assertEquals("\\t\\u\\s[10]\\h\\s[0]あ。\\w9\\w9\\uう！\\w9\\s[11]ん？\\w9\\w9\\h\\n\\n\\s[5]くっ…\\w5。\\e", message.getScript());
        message.insert(ActorType.Hontai, 5, "る～♪", false);
        assertEquals("\\t\\u\\s[10]\\h\\s[0]あ。\\w9\\w9\\uう！\\w9\\s[11]ん？\\w9\\w9\\h\\n\\n\\s[5]くっ…\\w5。\\w9る～♪\\e", message.getScript());
        message.insert(ActorType.Hontai, 9, "!!?\n\nhttp://test/", false);
        assertEquals("\\t\\u\\s[10]\\h\\s[0]あ。\\w9\\w9\\uう！\\w9\\s[11]ん？\\w9\\w9\\h\\n\\n\\s[5]くっ…\\w5。\\w9る～♪\\w9\\s[9]!!?\\w9\\n\\n\\URL[http://test/]\\e", message.getScript());

        message = new Message(false);
        message.insert(0, ActorType.Hontai, 0, "あ。", false);
        assertEquals("\\t\\h\\s[0]あ。\\e", message.getScript());
        message.insert(ActorType.Unyu, 10, "う！\nす。\r\n！？", false);
        assertEquals("\\t\\u\\s[10]\\h\\s[0]あ。\\w9\\w9\\uう！\\w9\\nす。\\w9\\n！？\\e", message.getScript());
        message.modify(1, "\\s[10]にゃ");
        assertEquals("\\t\\u\\s[10]にゃ\\h\\s[0]あ。\\w9\\w9\\uう！\\w9\\nす。\\w9\\n！？\\e", message.getScript());
        message.modify(0, "あ\\u");
        assertEquals("\\tあ\\u\\s[10]にゃ\\h\\s[0]あ。\\w9\\w9\\uう！\\w9\\nす。\\w9\\n！？\\e", message.getScript());
        //assertEquals("", message.getScript());
    }
}