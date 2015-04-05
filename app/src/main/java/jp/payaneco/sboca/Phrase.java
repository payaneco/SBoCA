package jp.payaneco.sboca;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by payaneco on 15/01/17.
 */
public class Phrase{
    private static final EnumMap<CommandType, String> PATTERN_MAP;
    static{
        PATTERN_MAP = new EnumMap<>(CommandType.class);
        PATTERN_MAP.put(CommandType.Hontai, "^h(.*)$");
        PATTERN_MAP.put(CommandType.Unyu, "^u(.*)$");
        PATTERN_MAP.put(CommandType.Surface, "^s\\[(-?\\d+)\\](.*)$");
        PATTERN_MAP.put(CommandType.NewHalfLine, "^n\\[half\\](.*)$");
        PATTERN_MAP.put(CommandType.NewLine, "^n(.*)$");
        PATTERN_MAP.put(CommandType.Clear, "^c(.*)$");
        PATTERN_MAP.put(CommandType.Wait, "^w(\\d)(.*)$");
        PATTERN_MAP.put(CommandType.Synchronized, "^_s(.*)$");
        PATTERN_MAP.put(CommandType.QuickSession, "^_q(.*)$");
        PATTERN_MAP.put(CommandType.MultipleURL, "^URL\\[[^\\]]+\\]((\\[http[^\\]]+\\]\\[[^\\]]+\\]){1,7})(.*)$");
        PATTERN_MAP.put(CommandType.SimpleURL, "^URL\\[(http[^\\]]+)\\](.*)$");
    }
    private String script;
    private boolean quickSession;
    private int surfaceNo;
    private ActorType actor;
    //シンクロする前のActorTypeを保持する
    private ActorType prevActor;
    private String context;
    private CommandType command;
    private Phrase previous;

    protected Phrase(){
        script = "";
        setSurfaceNo(Surface.UNDEFINED);
        setActor(ActorType.Hontai);
        setPrevActor(ActorType.Hontai);
    }

    public Phrase(Phrase previous, String script){
        this();
        this.script = script;
        this.previous = previous;
        setActor(previous.actor);
        CommandType command = getCommand(script);
        execute(command, script);
        setContext(command, script);
    }

    private void setContext(CommandType command, String script) {
        String context;
        switch (command) {
            case Escape:
                context = "\\";
                break;
            case Unhandled:
                context = "";
                break;
            default:
                Matcher matcher = getMatcher(command, script);
                if(matcher.find()) {
                    context = matcher.group(matcher.groupCount());
                }
                else
                {
                    context = "";
                }
                break;
        }
        setContext(context);
    }

    public static LinkedList<Phrase> parseScript(String script){
        LinkedList<Phrase> list = new LinkedList<Phrase>();
        Pattern p = Pattern.compile("\\\\t(.*)\\\\e");
        Matcher m = p.matcher(script);
        if(!m.find()){
            return list;
        }
        String[] ss = m.group(1).split("\\\\");
        Phrase phrase = new Phrase();
        for(int i = 0; i < ss.length; i++){
            //¥t¥u など、先頭に制御文字がくる場合ss[0]は空文字なのでスキップ
            if(i == 0 && ss[i].isEmpty()){
                continue;
            }
            phrase = new Phrase(phrase, ss[i]);
            list.add(phrase);
        }
        return list;
    }

    public static CommandType getCommand(String script){
        if(script.isEmpty()){
            //空文字の場合はエスケープと判断
            return CommandType.Escape;
        }
        for(CommandType key : PATTERN_MAP.keySet()) {
            Matcher matcher = getMatcher(key, script);
            if (matcher.find()) {
                return getCommand(key, matcher);
            }
        }
        return CommandType.Unhandled;
    }

    private static Matcher getMatcher(CommandType command, String script){
        Pattern p = Pattern.compile((String) PATTERN_MAP.get(command));
        return p.matcher(script);
    }

    //例外処理を施した上でコマンドを取得する
    private static CommandType getCommand(CommandType command, Matcher matcher){
        switch (command){
            case Surface:
                boolean isOutOfRange = (getSurfaceNo(matcher) == Integer.MIN_VALUE);
                return isOutOfRange ? CommandType.Unhandled : command;
            default:
                return command;
        }
    }

    //正規表現のグループからサーフィス番号を取得。取得できない場合はintの最小値を返す
    private static int getSurfaceNo(Matcher matcher){
        if(!matcher.matches()){
            //matcher.matchesがないとエラーになるので注意
            //  http://stackoverflow.com/questions/12911504/rationale-for-matcher-throwing-illegalstateexception-when-no-matching-method-i
            return Integer.MIN_VALUE;
        }
        try {
            int surfaceNo = Integer.parseInt(matcher.group(1));
            boolean isOutOfRange = (surfaceNo < -1 || 65535 < surfaceNo);
            return isOutOfRange ? Integer.MIN_VALUE : surfaceNo;
        } catch (NumberFormatException ex){
            return Integer.MIN_VALUE;
        }
    }

    public void execute(CommandType command, String script) {
        this.command = command;
        switch (command){
            case Hontai: actor = ActorType.Hontai; break;
            case Unyu: actor = ActorType.Unyu; break;
            case Synchronized: actor = ActorType.Synchronized; break;
            case Surface: surfaceNo = getSurfaceNo(getMatcher(command, script)); break;
        }
    }

    public boolean isQuickSession() {
        return quickSession;
    }

    public void setQuickSession(boolean quickSession) {
        this.quickSession = quickSession;
    }

    public int getSurfaceNo() {
        return getSurfaceNo(false);
    }

    public int getSurfaceNo(boolean isRaw) {
        if(isRaw) {
            return surfaceNo;
        }
        int retVal = surfaceNo;
        if(retVal == Surface.UNDEFINED) {
            Phrase phrase = this;
            while ((phrase = phrase.previous) != null) {
                if(!actor.equals(phrase.getActor())) {
                    continue;
                } else if(phrase.getSurfaceNo(true) == Surface.UNDEFINED) {
                    continue;
                }
                retVal = phrase.getSurfaceNo();
                break;
            }
        }
        return Math.max(retVal, -1);
    }

    public void setSurfaceNo(int surfaceNo) {
        this.surfaceNo = surfaceNo;
    }

    public ActorType getActor() {
        return actor;
    }

    public void setActor(ActorType actor) {
        this.actor = actor;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public ActorType getPrevActor() {
        return prevActor;
    }

    public void setPrevActor(ActorType prevActor) {
        this.prevActor = prevActor;
    }

    public CommandType getCommand() {
        return command;
    }

    public String getScript() {
        return "\\" + script;
    }

    public int getWaitTime() {
        if(command != CommandType.Wait) {
            return 0;
        }
        Matcher matcher = getMatcher(command, script);
        if(!matcher.find()) {
            return 0;
        }
        return Integer.parseInt(matcher.group(1));
    }

    public HashMap<String, String> getLinkMap() {
        Matcher matcher;
        switch (command) {
            case SimpleURL:
                matcher = getMatcher(command, script);
                break;
            case MultipleURL:
                matcher = getMatcher(command, script);
                break;
            default:
                return null;
        }
        HashMap<String, String> map = new HashMap<>();
        if(!matcher.find()) {
            return null;
        }
        if(command.equals(CommandType.SimpleURL)) {
            map.put("リンク", matcher.group(1));
            return map;
        }
        //Multiple
        for(int i = 0; i < matcher.groupCount() - 1; i++) {
            putUrlMap(matcher.group(i + 1), map);
        }
        return map;
    }

    private void putUrlMap(String text, HashMap<String, String> map) {
        //todo 定数化
        Pattern pattern = Pattern.compile("\\[(http[^\\]]+)\\]\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(text);
        if(!matcher.find()) {
            return;
        }
        map.put(matcher.group(2), matcher.group(1));
    }
}
