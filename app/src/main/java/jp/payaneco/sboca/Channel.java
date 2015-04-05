package jp.payaneco.sboca;

/**
 * Created by payaneco on 15/03/11.
 */
public class Channel {
    private int id;
    private String name;
    private String ghost;
    private String info;
    private String warnPost;
    private String noPost;
    private String count;

    public Channel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGhost() {
        return ghost;
    }

    public void setGhost(String ghost) {
        this.ghost = ghost;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getWarnPost() {
        return warnPost;
    }

    public void setWarnPost(String warnPost) {
        this.warnPost = warnPost;
    }

    public boolean isNoPost() {
        return "1".equalsIgnoreCase(noPost);
    }

    public void setNoPost(String noPost) {
        this.noPost = noPost;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
