package jp.payaneco.sboca;

/**
 * Created by payaneco on 15/03/29.
 */
public class NewIdResult extends SstpResult {
    public String getNewId() {
        return getValue("NewID");
    }

    @Override
    public String getMessage() {
        return "LUIDを取得しました！";
    }
}
