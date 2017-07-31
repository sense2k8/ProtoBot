package com.hcsdsg.asengu02.proto;



import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hcsdsg.asengu02.botframeworkDirectLineChatInterface.ChatView;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements ISpeechRecognitionServerEvents {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    private ChatView mChatView;

    //keep the last Response MsgId, to check if the last response is already printed or not

    private Synthesizer m_syn;
    MicrophoneRecognitionClient micClient = null;

    public String getPrimaryKey() {
        return this.getString(R.string.primaryKey);
    }

    public String getDirectLineKey(){
        return this.getString(R.string.botDirectLineSecret);
    }

    public String getBotName() {
        return this.getString(R.string.botName);
    }

    /**
     * Gets the LUIS application identifier.
     * @return The LUIS application identifier.
     */
    private String getLuisAppId() {
        return this.getString(R.string.luisAppID);
    }

    /**
     * Gets the LUIS subscription identifier.
     * @return The LUIS subscription identifier.
     */
    private String getLuisSubscriptionID() {
        return this.getString(R.string.luisSubscriptionID);
    }

    private String getAuthenticationUri() {
        return this.getString(R.string.authenticationUri);
    }

    public void setContentViewText(String msg){
        TextView view = (TextView)(findViewById(R.id.fullscreen_content));
        view.setText(msg);
    }


    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    Button _talkButton;

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                //delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    String welcomeText = "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xml:lang=\"en-US\"><voice xml:lang=\"en-IN\" name=\"Microsoft Server Speech Text to Speech Voice (en-IN, PriyaRUS)\">Proto Here <Break time=\"500ms\"/>. How can I help you today.</voice></speak>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        this._talkButton = (Button) findViewById(R.id.talk_button);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.talk_button).setOnTouchListener(mDelayHideTouchListener);
        ((Button) findViewById(R.id.talk_button)).setTextColor(Color.GREEN);

        final MainActivity This = this;





        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer(getString(R.string.bing_api_key));
        }

        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);

        Voice v = new Voice("en-IN", "Microsoft Server Speech Text to Speech Voice (en-IN, PriyaRUS)", Voice.Gender.Female, true);

        m_syn.SetVoice(v, null);


        this._talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                This.TalkButton_Click(arg0);
            }
        });

        m_syn.SpeakSSMLToAudio(welcomeText);


        final String botName= this.getBotName();
        final String directlinePrimaryKey=this.getDirectLineKey();
        //View chatView = new ChatView(mContentView,"Ping",this,botName,directlinePrimaryKey);
        mChatView = new ChatView(mContentView,this,botName,directlinePrimaryKey);

        int secs = 2; // Delay in seconds

        Utils.delay(secs, new Utils.DelayCallback() {
            @Override
            public void afterDelay() {
                mChatView.sendSingleMsgToBot("Ping");

            }
        });

    }

    private void TalkButton_Click(View arg0) {

        this._talkButton.setEnabled(false);
        //String listeningText = "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xml:lang=\"en-US\"><voice xml:lang=\"en-IN\" name=\"Microsoft Server Speech Text to Speech Voice (en-IN, PriyaRUS)\">Listening.</voice></speak>";
        //m_syn.SpeakSSMLToAudio(listeningText);
        MediaPlayer mp = MediaPlayer.create(this,R.raw.beep);
        mp.start();
        ((Button) findViewById(R.id.talk_button)).setTextColor(Color.RED);
        ((Button) findViewById(R.id.talk_button)).setText(R.string.listen_button);


        ((TextView)mContentView).setText("Listening.....");
        ((TextView)mContentView).append("\nSpeak loud and clear.....");

        /*
        this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                this,
                SpeechRecognitionMode.LongDictation,
                "en-us",
                this,
                this.getPrimaryKey());
        */
        this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(
                this,
                "en-us",
                this,
                this.getPrimaryKey(),
                this.getLuisAppId(),
                this.getLuisSubscriptionID());

        this.micClient.setAuthenticationUri(this.getAuthenticationUri());
        this.micClient.startMicAndRecognition();

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(10000);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onPartialResponseReceived(String s) {

        ((TextView)mContentView).setText("\nPartial Response  ");
        ((TextView)mContentView).append(s);
        mChatView.sendSingleMsgToBot(s);

    }

    @Override
    public void onFinalResponseReceived(RecognitionResult response) {
        boolean isFinalDicationMessage =
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);

        if (isFinalDicationMessage) {
            this._talkButton.setEnabled(true);
            ((TextView)mContentView).setText("\nFinal Response  ");
            //((TextView)mContentView).append(response.Results[0].DisplayText);

        }


    }

    @Override
    public void onIntentReceived(String payload) {
        //((TextView)mContentView).setText("\nIntent Recognized....  ");
        //((TextView)mContentView).setText(payload);
    }

    @Override
    public void onError(int i, String s) {
        ((TextView)mContentView).append(s);
    }

    @Override
    public void onAudioEvent(boolean recording) {

        if (recording) {
            ((TextView)mContentView).setText("\nRecording....");
        }
        if (!recording) {
            this.micClient.endMicAndRecognition();
            this._talkButton.setEnabled(true);
        }

    }
}
