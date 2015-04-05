package jp.payaneco.sboca;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by payaneco on 15/03/01.
 */
public class Stage {
    private final ActorType actor;
    private final Handler handler;
    private final Ghost ghost;
    private final Activity activity;
    private StringBuilder textBuilder;
    private boolean cancel;

    public Stage(ActorType actor, Handler handler, Ghost ghost, Activity activity) {
        this.actor = actor;
        this.handler = handler;
        this.ghost = ghost;
        this.activity = activity;
        setCancel(false);
        textBuilder = new StringBuilder();
    }

    public void play(final Phrase phrase) {
        if(isCancel()) {
            return;
        }
        if(phrase.getActor() != actor && phrase.getActor() != ActorType.Synchronized) {
            //ActorTypeが関係ない場合、何もしない
            return;
        }
        switch (phrase.getCommand()) {
            case Surface:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = ghost.getBitmap(phrase.getSurfaceNo());
                        setImageView(bitmap);
                    }
                });
                break;
            case NewHalfLine:
                //todo 簡易モードとの区分をしっかり
            case NewLine:
                textBuilder.append("\r\n");
                break;
            case Clear:
                textBuilder = new StringBuilder();
                break;
            case Wait:
                for(int i = 0; i < phrase.getWaitTime(); i++) {
                    if(isCancel()) {
                        break;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Synchronized:
                break;
            case QuickSession:
                break;
            case MultipleURL:
                break;
            case SimpleURL:
                break;
            case Escape:
                break;
            case Unhandled:
                break;
        }
        textBuilder.append(phrase.getContext());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(isCancel()) {
                    return;
                }
                setTextView(textBuilder.toString());
                ScrollView scrollView = getScrollView();
                if(scrollView != null) {
                    getScrollView().fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
        });
    }

    private ImageView getImageView() {
        return (ImageView)getActorsView(R.id.sakuraImage, R.id.unyuImage);
    }

    private void setImageView(Bitmap bitmap) {
        ImageView imageView = getImageView();
        if(imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private TextView getTextView() {
        return (TextView)getActorsView(R.id.sakuraText, R.id.unyuText);
    }

    private void setTextView(String text) {
        TextView textView = getTextView();
        if(textView != null) {
            textView.setText(text);
        }
    }

    private ScrollView getScrollView() {
        return (ScrollView)getActorsView(R.id.sakuraScroll, R.id.unyuScroll);
    }

    private View getActorsView(int sakuraId, int unyuId) {
        int id = actor.equals(ActorType.Hontai) ? sakuraId : unyuId;
        return activity.findViewById(id);
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
