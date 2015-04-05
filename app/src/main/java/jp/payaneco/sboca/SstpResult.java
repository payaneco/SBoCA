package jp.payaneco.sboca;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by payaneco on 15/03/29.
 */
public abstract class SstpResult {
    private String command;
    private String result;
    private String extraMessage;
    protected List<String> commandList;

    public void execute(String command) {
        try {
            BufferedReader reader = SstpClient.post(command);
            parse(reader);
        } catch (IOException e) {
            e.printStackTrace();
            result = "Err";
            extraMessage = e.getMessage();
        }
    }

    public void parse(BufferedReader reader) throws IOException {
        extraMessage = "";
        commandList = new ArrayList<>();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                AbstractMap.SimpleEntry<String, String> entry = getCommandEntry(line);
                if(entry == null) {
                    continue;
                }
                switch (entry.getKey()) {
                    case "Command": command = entry.getValue(); break;
                    case "Result": result = entry.getValue(); break;
                    case "ExtraMessage": extraMessage = entry.getValue(); break;
                    default: commandList.add(line);
                }
            }
        } finally {
            reader.close();
        }
    }

    protected AbstractMap.SimpleEntry<String, String> getCommandEntry(String command) {
        return SstpClient.getCommandEntry(command);
    }

    protected String getValue(String key) {
        for (String line : commandList) {
            AbstractMap.SimpleEntry<String, String> entry = getCommandEntry(line);
            if(entry == null) {
                continue;
            }
            if(entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return "";
    }

    public boolean success() {
        return "OK".equalsIgnoreCase(result);
    }

    public String getExtraMessage() {
        return extraMessage;
    }

    public abstract String getMessage();
}
