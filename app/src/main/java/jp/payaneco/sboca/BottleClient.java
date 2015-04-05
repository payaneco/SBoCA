package jp.payaneco.sboca;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by payaneco on 15/03/04.
 */
public class BottleClient {
    public ArrayList<Message> getLog(int minutes) {
        String url = String.format("http://bottle.mikage.to/fetchlog.cgi?recent=%d", minutes);
        HttpResponse response = null;
        try {
            response = getHttpResponse(url);
            InputStream input = response.getEntity().getContent();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, "SJIS"));

                String line;
                line = reader.readLine();
                if(!line.equalsIgnoreCase("Result: OK")) {
                    return null;
                }
                line = reader.readLine();
                ArrayList<Message> list = new ArrayList<>();
                Pattern pattern = Pattern.compile("^\\d{14}\t");
                while(line != null){
                    if(!pattern.matcher(line).find()) {
                        line = reader.readLine();
                        continue;
                    }
                    list.add(new Message(line));
                    line = reader.readLine();
                }
                return list;
            } finally {
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpResponse getHttpResponse(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        return client.execute(request);
    }
}
