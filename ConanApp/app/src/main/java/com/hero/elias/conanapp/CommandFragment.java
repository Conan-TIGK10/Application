package com.hero.elias.conanapp;

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
    ImageView image_joystick, image_border;
    TextView textView_x, textView_y, textView_angle, textView_distance, textView_direction;
    String message;
    int manualOrAutomatic = 0;
    Button togglebtn;
    
    public CommandFragment() {
        BluetoothHandler.getInstance().addCallback(this);
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
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        
        this.receiveTextView = view.findViewById(R.id.receive_text);
        super.onViewCreated(view, savedInstanceState);
        
        ball = BitmapFactory.decodeResource(getResources(), R.drawable.purple_ball);
        
        textView_x = view.findViewById(R.id.x_value);
        textView_y = view.findViewById(R.id.y_value);
        textView_angle = view.findViewById(R.id.angle);
        textView_distance = view.findViewById(R.id.distance);
        textView_direction = view.findViewById(R.id.direction);
        togglebtn = view.findViewById(R.id.toggle_btn);
        
        layout_joystick = view.findViewById(R.id.JoystickBackground);
        
        // send the image that pops up as the onclicklistener
        joystick = new JoyStickClass(getActivity().getApplicationContext(), layout_joystick, R.drawable.image_button);
        
        joystick.setStickSize(150, 150);
        joystick.setLayoutSize(500, 500);
        joystick.setLayoutAlpha(150);
        joystick.setStickAlpha(100);
        joystick.setOffset(90);
        joystick.setMinimumDistance(50);
        

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                
                // send information to bluetooth
                
                joystick.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    
                    message = "/" + manualOrAutomatic + "," + joystick.getX() + "," + joystick.getY() + "&";
                    System.out.println(message);
                    
                    try {
                        BluetoothHandler.getInstance().write(message.getBytes("US-ASCII"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    
                    textView_x.setText("X : " + joystick.getX());
                    textView_y.setText("Y : " + joystick.getY());
                    textView_angle.setText("Angle : " + joystick.getAngle());
                    textView_distance.setText("Distance : " + joystick.getDistance());
                    
                    int direction = joystick.get8Direction();
                    if(direction == JoyStickClass.STICK_UP) {
                        textView_direction.setText("Direction : Up");
                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                        textView_direction.setText("Direction : Up Right");
                    } else if(direction == JoyStickClass.STICK_RIGHT) {
                        textView_direction.setText("Direction : Right");
                    } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
                        textView_direction.setText("Direction : Down Right");
                    } else if(direction == JoyStickClass.STICK_DOWN) {
                        textView_direction.setText("Direction : Down");
                    } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
                        textView_direction.setText("Direction : Down Left");
                    } else if(direction == JoyStickClass.STICK_LEFT) {
                        textView_direction.setText("Direction : Left");
                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
                        textView_direction.setText("Direction : Up Left");
                    } else if(direction == JoyStickClass.STICK_NONE) {
                        textView_direction.setText("Direction : Center");
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView_x.setText("X :");
                    textView_y.setText("Y :");
                    textView_angle.setText("Angle :");
                    textView_distance.setText("Distance :");
                    textView_direction.setText("Direction :");
                }
                return true;
            }
        });

        togglebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(togglebtn.getText() == "AUTOMATIC"){
                    togglebtn.setText("MANUAL");
                    manualOrAutomatic = 1;
                }
                else{
                    togglebtn.setText("AUTOMATIC");
                    manualOrAutomatic = 0;
                }
            }
        });
    }
    
    @Override
    public void onDestroy() {
        BluetoothHandler.getInstance().removeCallback(this);
        super.onDestroy();
    }
}
