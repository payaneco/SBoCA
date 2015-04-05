package jp.payaneco.sboca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by payaneco on 15/02/13.
 */
public class Ghost {
    private HashMap<String, StringBuilder> valueMap;
    private Surface surface;
    private boolean isLoaded;

    /**
     * 空のコンストラクタ。デフォルト用に使う場合、initialize(stream, byte[])を呼ぶ
     */
    public Ghost() {
        isLoaded = false;
    }

    public Ghost(String ghostText) throws IOException {
        this();
        InputStream stream = new ByteArrayInputStream(ghostText.getBytes());
        initialize(stream);
    }

    public Ghost(String ghostText, byte[] bytes) throws IOException {
        this(ghostText);
        setImage(bytes);
    }

    /**
     * コンストラクタ
     */
    public Ghost(BufferedReader reader) throws IOException {
        initialize(reader);
    }

    /**
     * ghostTextと画像を指定する初期化コード
     * @param ghost
     * @param image
     * @throws IOException
     */
    public void initialize(InputStream ghost, InputStream image) throws IOException {
        initialize(ghost);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while(image.read(buffer) != -1) {
            bout.write(buffer);
        }
        setImage(bout.toByteArray());
    }

    private void initialize(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        initialize(reader);
    }

    private void initialize(BufferedReader reader) throws IOException {
        initValueMap();
        surface = new Surface();
        String line = reader.readLine();
        //最初の行でゴースト定義ファイルかどうかを判別
        if(!line.toUpperCase().startsWith("SVG3")){
            isLoaded = false;
            return;
        }
        //各行のデータを取得
        setDataFromReader(reader);
        //必須項目チェック
        isLoaded = checkLoadedStatus();
    }

    private void initValueMap() {
        valueMap = new HashMap<>();
        final String[] keys = {"sakura", "unyu", "user", "craftman", "url", "hpname", "surfacefile",
        "update", "sakura2", "ghostname", "supported", "sakura-action", "inform"};
        for(String key: keys){
            valueMap.put(key, new StringBuilder());
        }
    }

    public void setImage(byte[] bytes) throws IOException {
        InputStream surfaceStream = new ByteArrayInputStream(bytes);
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(surfaceStream, false);
        setDecoder(decoder);
    }

    public void setDecoder(BitmapRegionDecoder decoder) {
        surface.setDecoder(decoder);
    }

    public Bitmap getBitmap(int surfaceNo) {
        if(surface == null) {
            return null;
        }
        return surface.getBitmap(surfaceNo);
    }

    private void setDataFromReader(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        String parameter = "";
        while(line != null) {
            //EOFコード以下はすべてスキップ
            if(line.equalsIgnoreCase("/EOF")){
                break;
            }
            for(String token: line.split(",")){
                parameter = setDataFromToken(parameter, token);
            }
            line = reader.readLine();
        }
    }

    public String setDataFromToken(String parameter, String line) {
        String newParameter = parameter;
        String value;
        String[] ss = line.split("=", 2);
        boolean isAppending = (ss.length < 2);
        if(isAppending){
            value = ss[0];
        } else {
            newParameter = ss[0].trim().toLowerCase();
            value = ss[1];
        }
        value = value.trim().replace("\"", "");
        if(!value.isEmpty()) {
            setParsedData(newParameter, value, isAppending);
        }
        return newParameter;
    }

    private void setParsedData(String parameter, String value, boolean isAppending) {
        if (Surface.containsKey(parameter)){
            surface.setParsedData(parameter, value);
            return;
        } else if(!valueMap.containsKey(parameter)){
            return;
        }
        if (parameter.equalsIgnoreCase("supported")){
            valueMap.get(parameter).append(value.toUpperCase());
            return;
        }
        if(isAppending){
            valueMap.get(parameter).append(System.getProperty("line.separator"));
        }
        else
        {
            valueMap.put(parameter, new StringBuilder());
        }
        valueMap.get(parameter).append(value);
    }

    private boolean checkLoadedStatus() {
        return true;
    }

    /**
     * さくら（\\0=\\h）側の名前。元ゴーストのdescript.txtのsakura.nameと同じ名前。必須
     * @return さくら（\\0=\\h）側の名前。
     */
    public String getSakura() {
        return valueMap.get("sakura").toString();
    }

    /**
     * うにゅう（\\1=\\u）側の名前。元ゴーストのdescript.txtのunyu.nameと同じ名前。必須
     * @return うにゅう（\\1=\\u）側の名前。
     */
    public String getUnyu() {
        return valueMap.get("unyu").toString();
    }

    /**
     * ユーザー呼称。「ユーザーさん」など。ゴースト特有のものがあれば指定。
     * 「%username」という文字列を指定するか、省略することでS-V本体設定の呼称が使用される。
     * @return
     */
    public String getUser() {
        String user = valueMap.get("user").toString();
        if(user.isEmpty()){
            return "%username";
        }
        return user;
    }

    /**
     * 元ゴーストのシェルを作った人・ゴーストを作った人。SVGを編集した人ではないので注意。
     * 「シェルベンダ名／ゴーストベンダ名」を一応推奨。
     * @return
     */
    public String getCraftman() {
        return valueMap.get("craftman").toString();
    }

    /**
     * 元ゴーストのシェル・ゴーストが配布されているサイトのURL。
     * ゴースト配布元とシェル配布元が異なるときは、一応、シェル配布元を推奨
     * @return 元ゴーストのシェル・ゴーストが配布されているサイトのURL。
     */
    public String getUrl() {
        return valueMap.get("url").toString();
    }

    /**
     * 配布元のWebサイトの名前。
     * @return
     */
    public String getHpname() {
        return valueMap.get("hpname").toString();
    }

    /**
     * SVGサーフィスファイル名。
     * ＳＶＧサーフィスファイルは、定義ファイルと同一ディレクトリにあることが前提なので、相対パス指定の場合は動作は保証外。
     * サポートフォーマットは.JPG、.PNG。(SVG ViewerでサポートしているBMP、.GIFは未サポート)
     * @return SVGサーフィスファイル名。
     */
    public String getSurfacefile() {
        return valueMap.get("surfacefile").toString();
    }

    /**
     * SVNサーフィスファイル名に"Ghost"を補完したパスを返す
     */
    public String getSurfaceUrl() {
        String surface = getSurfacefile();
        return String.format("Ghost\\%s", surface);
    }

    /**
     * SVGの最終更新日付。「YYYY/MM/DD」で指定。
     * @return SVGの最終更新日付。
     */
    public String getUpdate() {
        return valueMap.get("update").toString();
    }

    /**
     * さくら（\\h）側のTrueName。「黒衣さくら（1st）」と「川上さくら（3rd）」の二人の「さくら」を区別するためのもの。ifGhostが同じになってしまう追加シェル等で使用してもよいが、通常は指定する必要はない。
     * 本ツールでは未使用。
     * （Viewerでは、特別にsakura2でもifGhostの処理ができる。）
     * @return さくら（\\h）側のTrueName。
     */
    public String getSakura2() {
        return valueMap.get("sakura2").toString();
    }

    /**
     * 元ゴーストのパッケージの名前。パッケージ名は、SSPなどでゴースト選択メニューに出る名前。のこと
     * @return 元ゴーストのパッケージの名前。
     */
    public String getGhostname() {
        return valueMap.get("ghostname").toString();
    }

    /**
     * 「NORMAL」、「COMPACT」、「FULLSURFACE」、「EXPAND」を指定。
     * 本ツールでは未使用。
     * CompactはSVGのスプライト数を減らすように、SVGデザイナが調整したもの（怒り顔を全て\\s[7]にエリアスするなど）。
     * FullSurfaceはアニメーション・着せ替え用の直接使用されないものを除き、全てのサーフィスを（updateの日付の時点では）厳密にサポートしていることを示す。
     * Expandは、他の同ゴーストのシェルと互換を持たせるために本来は存在しないサーフィスが追加されている事を示す。
     * Compactはサイズが軽いが、書くツールにおいてはサーフィス指定ミスをするおそれがあり、読むツールでも注意が必要となる。 Expandは読む分には違和感がないが、書くツールにおいては使用されていないサーフィスを指定してしまうおそれがある。 FullSurafaceは読み書き双方においても(48×64のレベルでは）誤解は生じない。
     * @return supportedの内容
     */
    public String getSupported() {
        return valueMap.get("supported").toString();
    }

    /**
     *「OK」または「NG」を指定。
     *「元ゴーストが、標準的なSSTP（さくらのSSTP）を受信できることを示す。すなわち、\\s[0]～\\s[8]および\\s[10]、\\s[11]といった デファクトスタンダートな基本サーフィスをほぼサポートし、歌やアナウンスのような口調を気にしないレベルでさくらと互換があることを示す。
     *「省略すると「不明」として扱われる。
     *「新旧のさくらはもちろん、双葉・まゆら・せりこ・奈留といった普通の女の子のゴーストとして作られているのは皆OKとする。 ねここ（\\uのサーフィスが特徴的）や汁親父（キモい）あたりになってくるとSVGデザイナの判断。
     * @return 指定された値が「OK」の場合 true, それ以外や未指定は false
     */
    public boolean canSakuraAction() {
        String sakuraAction = valueMap.get("sakura-action").toString();
        return sakuraAction.equalsIgnoreCase("OK");
    }

    /**
     * このSVGに関する、SVGデザイナからユーザーへの注意事項や告知事項。Viewerではインストールに表示される。
     * @return informの内容
     */
    public String getInform() {
        return valueMap.get("inform").toString();
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * サーフィスの番号に応じた画像のインデックスをかえす
     * @param surfaceNo サーフィスの番号
     * @return サーフィスの番号に応じた画像のインデックス
     */
    public int getSurfaceIndex(int surfaceNo) {
        return surface.get(surfaceNo);
    }

    public Integer[] getAlias(ActorType actorType, int surfaceNo) {
        return surface.getAlias(actorType, surfaceNo);
    }

    public LinkedList<String> getSequence(int surfaceNo) {
        return surface.getSequence(surfaceNo);
    }

    public Set<Integer> getSurfaceNoSet() {
        return surface.getIndexSet();
    }
}
