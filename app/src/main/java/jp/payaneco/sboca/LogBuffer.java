package jp.payaneco.sboca;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by payaneco on 15/03/25.
 */
public class LogBuffer {

    private ActorType actor;
    private LinkedList<Phrase> phraseList;
    private boolean current;

    public LogBuffer(ActorType actor) {
        this.actor = actor;
        this.current = false;
        this.phraseList = new LinkedList<>();
    }

    public void tryAdd(Phrase phrase) {
        if(phrase.getActor() != actor && phrase.getActor() != ActorType.Synchronized) {
            //ActorTypeが関係ない場合
            current = false;
            return;
        }
        current = true;
        phraseList.add(phrase);
    }

    public int getSurfaceNo() {
        if(phraseList.isEmpty()) {
            return Surface.UNDEFINED;
        }
        return phraseList.getLast().getSurfaceNo();
    }

    public boolean isCurrent() {
        return current;
    }

    public String getPrefix(int surfaceNo, boolean isScript) {
        if(isScript) {
            return "";
        }
        if(getSurfaceNo() != Surface.UNDEFINED) {
            return "";
        }
        return getActorScript() + getSurfaceScript(surfaceNo);
    }

    public String getScript(int position, int surfaceNo, String text, String opposition, boolean isScript) {
        StringBuilder sb = new StringBuilder();
        String script = getScript(text, isScript);
        if(!opposition.isEmpty() && !script.isEmpty()) {
            String lc = opposition.substring(opposition.length() - 1);
            if("。？！!?".contains(lc)) {
                sb.append("\\w9");
            }
            if(!isCurrent()) {
                sb.append("\\w9");
            }
        }
        sb.append(tryGetActorScript(position));
        sb.append(tryGetSurfaceScript(surfaceNo));
        sb.append(script);

        return sb.toString();
    }

    public static String getScript(String text, boolean isScript) {
        if(isScript) {
            //最低限の処理
            return getSafeScript(text);
        }
        //テキスト処理
        String script = replaceAll(text, "^(http://.+)$", "\\\\URL[]", Pattern.MULTILINE);    //ダミーURLを挿入
        script = getSafeScript(script);
        script = replaceAll(script, "(、|…|‥|・)", "$1\\\\w5");
        script = replaceAll(script, "([。？！!?]+)", "$1\\\\w9");
        Pattern p = Pattern.compile("^(http://.+)$", Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if(m.find()) {
            String url = m.group(1);
            script = script.replace("\\URL[]", String.format("\\URL[%s]", url));
        }

        return script;
    }

    public static String getSafeScript(String text) {
        return replaceAll(text, "(\r\n|\r|\n)", "\\\\n");
    }

    public String getContext() {
        StringBuilder sb = new StringBuilder();
        for(Phrase phrase : phraseList) {
            sb.append(phrase.getContext());
        }
        return sb.toString();
    }

    private String tryGetActorScript(int position) {
        if(position == 0 && getSurfaceNo() == Surface.UNDEFINED) {
            //サーフィス未定義なので先頭に\\u\\s[10]的なものを挿入済み
            return "";
        } else if(isCurrent()) {
            //変わってない
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getActorScript());
        if(!getContext().isEmpty()) {
            sb.append("\\n\\n");
        }
        return sb.toString();
    }

    private String getActorScript() {
        return ActorType.Hontai.equals(actor) ? "\\h" : "\\u";
    }

    private String tryGetSurfaceScript(int surfaceNo) {
        if(getSurfaceNo() == Surface.UNDEFINED) {
            //サーフィス未定義なので先頭に\\u\\s[10]的なものを挿入済み
            return "";
        } else if (getSurfaceNo() == surfaceNo) {
            //前回と同じサーフィス
            return "";
        }
        return getSurfaceScript(surfaceNo);
    }

    private String getSurfaceScript(int surfaceNo) {
        return String.format("\\s[%d]", surfaceNo);
    }

    private static String replaceAll(String input, String pattern, String replacement) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        return m.replaceAll(replacement);
    }

    private static String replaceAll(String input, String pattern, String replacement, int flags) {
        Pattern p = Pattern.compile(pattern, flags);
        Matcher m = p.matcher(input);
        return m.replaceAll(replacement);
    }
}
