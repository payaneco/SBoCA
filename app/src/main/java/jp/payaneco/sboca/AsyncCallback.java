package jp.payaneco.sboca;

/**
 * http://qiita.com/otoyo/items/70cfa89bfa93ffdf6c9a
 * Created by payaneco on 15/03/10.
 */

public interface AsyncCallback {

    void onPreExecute();
    void onPostExecute(int result);
    void onProgressUpdate(int progress);
    void onCancelled();

}
