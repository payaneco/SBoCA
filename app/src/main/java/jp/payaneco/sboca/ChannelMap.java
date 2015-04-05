package jp.payaneco.sboca;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by payaneco on 15/03/11.
 */
public class ChannelMap extends HashMap<Integer, Channel> {
    public ChannelMap() {
        super();
    }

    public Channel putCommand(String command) {

        Pattern pattern = Pattern.compile("^CH(\\d+)_(\\w+): *(.+)$");
        Matcher matcher = pattern.matcher(command);
        if(!matcher.find()) {
            return null;
        }
        Integer id = Integer.valueOf(matcher.group(1));
        Channel channel;
        if(containsKey(id)) {
            channel = get(id);
        } else {
            channel = new Channel(id);
            put(id, channel);
        }
        String value = matcher.group(3);
        switch (matcher.group(2)) {
            case "name": channel.setName(value); break;
            case "ghost": channel.setGhost(value); break;
            case "info": channel.setInfo(value); break;
            case "warnpost": channel.setWarnPost(value); break;
            case "nopost": channel.setNoPost(value); break;
            case "count": channel.setCount(value); break;
        }
        return channel;
    }

    public String[] getChannels() {
        Set<String> set = new TreeSet<>();
        for(Channel ch : values()) {
            if(ch.isNoPost()) {
                continue;
            }
            set.add(ch.getName());
        }
        return set.toArray(new String[]{});
    }
}
