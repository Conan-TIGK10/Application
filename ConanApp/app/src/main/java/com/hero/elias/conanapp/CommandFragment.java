package com.hero.elias.conanapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class CommandFragment extends Fragment implements BluetoothHandler.BluetoothCallback {
    
    private TextView receiveTextView;
    private int messagesReceived;
    
    Bitmap ball;
    JoyStickClass joystick;
    RelativeLayout layout_joystick;
    String message;
    int manualOrAutomatic ;
    private Button toggle_button;
    int currentDirection = -1;
    int newDirection = -1;
    
    public CommandFragment() {
    }
    
    public static CommandFragment newInstance() {
        return new CommandFragment();
    }
    
    @Override
    public void bluetoothMessage(byte[] bytes) {
        this.messagesReceived++;
        this.receiveTextView.setText(this.messagesReceived + ": " + bytes.toString());
    }
    
    @Override
    public void onStateChange(BluetoothHandler.BluetoothInState state) {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_command, container, false);
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        BluetoothHandler.getInstance().addCallback(this);
    
        this.receiveTextView = view.findViewById(R.id.receive_text);
        super.onViewCreated(view, savedInstanceState);
        
        ball = BitmapFactory.decodeResource(getResources(), R.drawable.purple_ball);
        toggle_button = view.findViewById(R.id.toggle_btn);
        manualOrAutomatic = 0;
        
        layout_joystick = view.findViewById(R.id.JoystickBackground);
        
        // send the image that pops up as the onclicklistener
        joystick = new JoyStickClass(getActivity().getApplicationContext(), layout_joystick, R.drawable.image_button);
        
        joystick.setStickSize(150, 150);
        joystick.setLayoutSize(500, 500);
        joystick.setLayoutAlpha(150);
        joystick.setStickAlpha(100);
        joystick.setOffset(90);
        joystick.setMinimumDistance(50);
        

        layout_joystick.setOnTouchListener((arg0, arg1) -> {

            joystick.drawStick(arg1);
            if(arg1.getAction() == MotionEvent.ACTION_DOWN
            || arg1.getAction() == MotionEvent.ACTION_MOVE) {

                int direction = joystick.get8Direction();
                newDirection = direction;
                if(newDirection != currentDirection & direction != 1337){
                    switch (direction)
                    {
                        case JoyStickClass.STICK_UP:
                            message = "/" + manualOrAutomatic + "," + 0 + "," + 1 + "&";
                            break;
                        case JoyStickClass.STICK_RIGHT:
                            message = "/" + manualOrAutomatic + "," + 2 + "," + 0 + "&";
                            break;
                        case JoyStickClass.STICK_DOWN:
                            message = "/" + manualOrAutomatic + "," + 0 + "," + 2 + "&";
                            break;
                        case JoyStickClass.STICK_LEFT:
                            message = "/" + manualOrAutomatic + "," + 1 + "," + 0 + "&";
                            break;
                        case JoyStickClass.STICK_NONE:
                            message = "/" + manualOrAutomatic + "," + 0 + "," + 0 + "&";
                            break;
                    }
                    sendToRobot(message);
                    currentDirection = direction;
                }
            }

            if(arg1.getAction() == MotionEvent.ACTION_UP) {
                message = "/" + manualOrAutomatic + "," + 0 + "," + 0 + "&";
                sendToRobot(message);
                currentDirection = -1;
                newDirection = -1;
            }
            return true;
        });

        toggle_button.setOnClickListener(view1 -> {
            if (toggle_button.getText().equals("AUTOMATIC")) {
                toggle_button.setText("MANUAL");
                manualOrAutomatic = 1;
            } else {
                toggle_button.setText("AUTOMATIC");
                manualOrAutomatic = 0;
            }
            message = "/" + manualOrAutomatic + "," + 0 + "," + 0 + "&";
            sendToRobot(message);
        });
    }
    
    @Override
    public void onDestroy() {
        BluetoothHandler.getInstance().removeCallback(this);
        message = "/" + 0 + "," + 0 + "," + 0 + "&";
        sendToRobot(message);
        super.onDestroy();
    }

    private void sendToRobot(String message){
        try {
            BluetoothHandler.getInstance().write(message.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
