package jp.payaneco.sboca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by payaneco on 15/01/16.
 */
public class SbocaClient {
    public static HashMap<String, String> mWebMap;

    static {
        mWebMap = null;
    }

    public String getLog() throws IOException{
        return getWholeText("test1.html", "SJIS");
    }

    public static String getWholeText(String fileName, String charsetName) throws IOException {
        HttpResponse response = getHttpResponse(fileName);
        InputStream input = response.getEntity().getContent();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, charsetName));
            return  getWholeText(reader);
        } finally {
            input.close();
        }
    }

    public static String getWholeText(BufferedReader reader) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        line = reader.readLine();
        while(line != null){
            sb.append(line);
            line = reader.readLine();
            if(line != null) {
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }

    public static byte[] getImage(String fileName) throws IOException {
        HttpResponse response = getHttpResponse(fileName);
        return EntityUtils.toByteArray(response.getEntity());
    }

    private static HttpResponse getHttpResponse(String fileName) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(getUrl(fileName));
        return client.execute(request);
    }

    public static String getUrl(String fileName) {
        String encodedName = fileName.replace('\\', '/').replace(" ", "%20");
        String url = String.format("%s/%s", "http://www.payaneco.jp/sboca", encodedName);
        return url;
    }

    public static HashMap<String, String> getGhostMap() {
        if(mWebMap != null){
            //ロード済みの場合、ロード済みのものを直接返す
            return mWebMap;
        }
        try {
            InputStream input = getGhostInputStream();
            mWebMap = getGhostMap(input);
            return mWebMap;
        } catch (IOException e) {
            e.printStackTrace();
            mWebMap = new HashMap<>();
            return mWebMap;
        }
    }

    private static InputStream getGhostInputStream() throws IOException {
        //テキストから直接所得する場合はこちら
        HttpResponse response = getHttpResponse("ghost.txt");
        return response.getEntity().getContent();

        //zipを展開
        /*HttpResponse response = getHttpResponse("ghost.zip");
        ZipInputStream input = new ZipInputStream(response.getEntity().getContent());
        ZipEntry zipEntry = null;
        int len = 0;
        while ((zipEntry = input.getNextEntry()) != null) {
            if(!zipEntry.getName().equalsIgnoreCase("ghost.txt"))
            {
                continue;
            }
            byte[] data=new byte[(int)zipEntry.getSize()];
            input.read(data,0,(int)zipEntry.getSize());
            return new ByteArrayInputStream(data);
        }
        return null;
        */
    }

    private static HashMap<String, String> getGhostMap(InputStream input) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "SJIS"));
            String line;
            Pattern p = Pattern.compile("^GHOST *,([^,]+),[^,]*, *\"?(.+?)\"? *$");
            while ((line = reader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if(!m.matches()){
                    continue;
                }
                map.put(m.group(1).trim(), m.group(2).trim());
            }
            return map;
        } finally {
            input.close();
        }
    }
}
