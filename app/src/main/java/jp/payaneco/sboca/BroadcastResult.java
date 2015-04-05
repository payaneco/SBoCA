package jp.payaneco.sboca;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.AbstractMap;

/**
 * Created by payaneco on 15/03/29.
 */
public class BroadcastResult extends SstpResult {

    private final String channel;
    private final String ghost;
    private final Message message;

    public BroadcastResult(String channel, String ghost, Message message) {
        this.channel = channel;
        this.ghost = ghost;
        this.message = message;
    }

    public boolean isReady() {
        if(channel.isEmpty()) {
            return false;
        }
        if(ghost.isEmpty()) {
            return false;
        }
        if(!message.isPostEnabled()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean success() {
        if(!isReady()) {
            return false;
        }
        return super.success();
    }

    @Override
    public String getExtraMessage() {
        if(channel.isEmpty()) {
            return "設定画面でチャンネルを設定してください。";
        }
        if(ghost.isEmpty()) {
            return "設定画面でゴーストを設定してください。";
        }
        if(!message.isPostEnabled()) {
            return "スクリプトが正しくありません。";
        }
        return super.getExtraMessage();
    }

    @Override
    public String getMessage() {
        //int num = Integer.parseInt(getValue("Num"));;
        //String channel = getValue("Channel");

        //return String.format("%sの%d人に投瓶しました！", channel, num);
        //todo 人数とチャンネルを返す
        return "投瓶しました！";
    }
}
