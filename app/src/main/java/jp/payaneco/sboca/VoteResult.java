package jp.payaneco.sboca;

/**
 * Created by payaneco on 15/03/29.
 */
public class VoteResult extends SstpResult {
    private final String mid;
    private final String type;

    public VoteResult(String mid, String type) {
        this.mid = mid;
        this.type = type;
    }

    public boolean isReady() {
        if(mid == null || mid.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public String getExtraMessage() {
        if(!isReady()) {
            return String.format("この瓶には%sできません。", getPrefix());
        }
        return super.getExtraMessage();
    }

    @Override
    public String getMessage() {
        return String.format("%sしました！", getPrefix());
    }

    private String getPrefix() {
        return type.equalsIgnoreCase("Vote") ? "投票" : "同意";
    }
}
