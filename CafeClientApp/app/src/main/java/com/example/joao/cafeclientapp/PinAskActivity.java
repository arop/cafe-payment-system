package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.cart.QrCodeCheckoutActivity;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;

public abstract class PinAskActivity extends AppCompatActivity {

    private TextView pin_view;
    private LinearLayout pin_view_container;
    private Activity currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_ask);
        this.currentActivity = this;

        pin_view = (TextView) findViewById(R.id.pin_hidden_text);
        pin_view_container = (LinearLayout) findViewById(R.id.pin_hidden_text_container);

        for(int i = 0; i < 10; i++){
            int id = getResources().getIdentifier("numeric_pad_"+i, "id", getPackageName());
            Button b = (Button) findViewById(id);
            b.setOnTouchListener( new CustomHapticListener( 30, 100 ) );
        }
        ((Button) findViewById(R.id.numeric_pad_backspace)).setOnTouchListener(new CustomHapticListener(30 , 100));
        ((Button) findViewById(R.id.numeric_pad_empty)).setOnTouchListener(new CustomHapticListener(30 , 100));

    }

    /**
     *
     * @param v
     * @return 0 if input accepted or 1 if not, 2 if pin is complete and incorrect, -1 if pin is complete and correct and should exit activity.
     */
    public int onClickPinPadButton(View v){
        Button b = (Button) v;

        String pin = pin_view.getText().toString();
        int return_val = 0;

        String button_tag = b.getTag().toString();
        switch(button_tag){
            case "backspace":{
                if(pin.length() > 0)
                    pin = pin.substring(0, pin.length()-1);
                else return_val = 1;
                break;
            }

            case "clear":{
                pin = "";
                break;
            }

            default:{
                if(pin.length() < 4) {
                    pin = pin + button_tag;
                }

                if(pin.length() == 4){
                    //verify if pin's correct
                    if(checkPin(pin)){
                        return_val = -1;
                    }
                    else{
                        return_val = 2;
                    }
                }
            }
        }

        pin_view.setText(pin);

        return return_val;
    }

    public boolean checkPin(String pin){
        return CustomLocalStorage.getString(this, "pin").equals(pin);
    }

    private class CustomHapticListener implements View.OnTouchListener {

        // Duration in milliseconds to vibrate
        private final int durationMs;
        private final int durationMsError;

        public CustomHapticListener( int ms, int error_ms ) {
            durationMs = ms;
            durationMsError = error_ms;
        }

        @Override
        public boolean onTouch( View v, MotionEvent event ) {
            if( event.getAction() == MotionEvent.ACTION_DOWN ){
                Vibrator vibe = ( Vibrator ) getSystemService( VIBRATOR_SERVICE );

                switch(onClickPinPadButton(v)){
                    case 0: vibe.vibrate( durationMs ); break;
                    case 1: vibe.vibrate( durationMs ); break;
                    case 2:
                        vibe.vibrate( durationMsError*3 );
                        Animation shake = AnimationUtils.loadAnimation(currentActivity, R.anim.shake);
                        pin_view_container.startAnimation(shake);
                        break;
                    case -1: nextActivity(); break;
                    default: break;
                }
            }
            return true;
        }
    }

    public abstract void nextActivity();
}
