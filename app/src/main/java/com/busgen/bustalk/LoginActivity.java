package com.busgen.bustalk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.busgen.bustalk.events.Event;
import com.busgen.bustalk.events.ToActivityEvent;
import com.busgen.bustalk.events.ToClientEvent;
import com.busgen.bustalk.events.ToServerEvent;
import com.busgen.bustalk.model.Chatroom;
import com.busgen.bustalk.model.Client;
import com.busgen.bustalk.model.IChatroom;
import com.busgen.bustalk.model.IServerMessage;
import com.busgen.bustalk.model.IUser;
import com.busgen.bustalk.model.ServerMessages.MsgAvailableRooms;
import com.busgen.bustalk.model.ServerMessages.MsgAvailableRoomsRequest;
import com.busgen.bustalk.model.ServerMessages.MsgChatMessage;
import com.busgen.bustalk.model.ServerMessages.MsgChooseNickname;
import com.busgen.bustalk.model.ServerMessages.MsgConnectToServer;
import com.busgen.bustalk.model.ServerMessages.MsgConnectionLost;
import com.busgen.bustalk.model.ServerMessages.MsgConnectionStatus;
import com.busgen.bustalk.model.ServerMessages.MsgCreateRoom;
import com.busgen.bustalk.model.ServerMessages.MsgJoinRoom;
import com.busgen.bustalk.model.ServerMessages.MsgLeaveRoom;
import com.busgen.bustalk.model.ServerMessages.MsgLostChatRoom;
import com.busgen.bustalk.model.ServerMessages.MsgLostUserInChat;
import com.busgen.bustalk.model.ServerMessages.MsgNewChatRoom;
import com.busgen.bustalk.model.ServerMessages.MsgNewUserInChat;
import com.busgen.bustalk.model.ServerMessages.MsgNicknameAvailable;
import com.busgen.bustalk.model.ServerMessages.MsgSetGroupId;
import com.busgen.bustalk.model.User;
import com.busgen.bustalk.service.EventBus;
import com.busgen.bustalk.service.MainService;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BindingActivity {
    private EditText userNameInput;
    private EditText interestInput;
    private Button loginButton;
    private Toast loginToast;
    private Toast noConnectionToast;
    private Toast nameUnavailableToast;
    private Toast testToast;
    private String interest;
    private String userName;
    ProgressDialog progress;
    //private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        startService(new Intent(this, MainService.class));
//        client = mainService.getClient();

        /*String groupId = "1";
        IServerMessage serverMessage = new MsgSetGroupId(groupId);
        Event event = new ToServerEvent(serverMessage);
        eventBus.postEvent(event);*/

        initViews();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new ProgressDialog(LoginActivity.this);
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                    /*progress.setButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // Use either finish() or return() to either close the activity or just the dialog
                            return;
                        }
                    });*/
                progress.show();
                eventBus.postEvent(new ToServerEvent(new MsgConnectToServer()));
                //For testing purposes
                /*
                Chatroom testChatroom = new Chatroom(10, "Mainchat");
                MsgAvailableRooms testMessage = new MsgAvailableRooms("1");
                testMessage.addRoomToList(testChatroom);
                Event testEvent = new ToActivityEvent(testMessage);
                onEvent(testEvent);
                */
            }
        });
    }

    private void initViews() {
        userNameInput = (EditText) findViewById(R.id.user_name_input);
        interestInput = (EditText) findViewById(R.id.interest_input);
        loginButton = (Button) findViewById(R.id.login_button);
        loginToast = Toast.makeText(LoginActivity.this, "You have to choose a nickname",
                Toast.LENGTH_SHORT);
        noConnectionToast = Toast.makeText(LoginActivity.this, "Connection to server failed", Toast.LENGTH_SHORT);
        nameUnavailableToast = Toast.makeText(LoginActivity.this, "Name was already taken", Toast.LENGTH_SHORT);
    }

    @Override
    public void onEvent(Event event){
        IServerMessage message = event.getMessage();

        if (event instanceof ToActivityEvent) {
            Log.d("MyTag", "Activity received some sort of event");
            if (message instanceof MsgChatMessage) {
            } else if (message instanceof MsgChooseNickname) {
               /* testToast = Toast.makeText(LoginActivity.this, "The chosen username is: " + ((MsgChooseNickname) message).getNickname(),
                        Toast.LENGTH_SHORT);
                testToast.show();*/
            } else if (message instanceof MsgCreateRoom) {
            } else if (message instanceof MsgJoinRoom) {
                /*
                MsgJoinRoom joinRoomMessage = (MsgJoinRoom) message;
                Chatroom myChatroom = (Chatroom) joinRoomMessage.getChatroom();
                Intent intent = new Intent(LoginActivity.this, MainChatActivity.class);
                intent.putExtra("Chatroom", myChatroom);
                intent.putExtra("Username", client.getUserName());
                intent.putExtra("Interest", client.getInterest());
                startActivity(intent);
                LoginActivity.this.finish();
                */
            } else if (message instanceof MsgLeaveRoom) {
            } else if (message instanceof MsgLostChatRoom) {
            } else if (message instanceof MsgLostUserInChat) {
            } else if (message instanceof MsgNewChatRoom) {
            } else if (message instanceof MsgNewUserInChat) {
            } else if (message instanceof MsgNicknameAvailable) {
                MsgNicknameAvailable nicknameAvailableMessage = (MsgNicknameAvailable) message;
                if(nicknameAvailableMessage.getAvailability()){
                    Log.d("MyTag", "nickname is available, setting user info...");
                    client.setUserName(userName);
                    client.setInterest(interest);

                    IServerMessage serverMessage = new MsgSetGroupId(client.getGroupId());
                    Event setGroupIdEvent = new ToServerEvent(serverMessage);
                    eventBus.postEvent(setGroupIdEvent);

                    serverMessage = new MsgAvailableRoomsRequest();
                    Event requestEvent = new ToServerEvent(serverMessage);
                    eventBus.postEvent(requestEvent);

                    /*
                    Intent intent = new Intent(LoginActivity.this, MainChatActivity.class);
                    intent.putExtra("Username", client.getUserName());
                    intent.putExtra("Interest", client.getInterest());
                    Log.d("MyTag", "Trying to start Mainchatactivity");
                    progress.dismiss();
                    startActivity(intent);
                    LoginActivity.this.finish();
                    */

                } else{
                    Log.d("MyTag", "Tried to start Mainchatactivity");
                    progress.dismiss();
                    nameUnavailableToast.show();
                    userNameInput.setText("");
                    interestInput.setText("");

                }
            } else if (message instanceof MsgAvailableRooms) {
                Log.d("MyTag", "Final stage");
                MsgAvailableRooms availableRoomsMesssage = (MsgAvailableRooms) message;
                List<IChatroom> chatrooms = availableRoomsMesssage.getRoomList();
                IChatroom myChatroom = chatrooms.get(0);
<<<<<<< 21bac057cb9d370e2c3152fe84fd28a34769926b
=======
                IServerMessage joinMessage = new MsgJoinRoom(myChatroom);
                Event joinEvent = new ToServerEvent(joinMessage);
                eventBus.postEvent(joinEvent);

>>>>>>> Continued on testbranch
                Intent intent = new Intent(LoginActivity.this, MainChatActivity.class);
                intent.putExtra("Chatroom", myChatroom);
                intent.putExtra("Username", client.getUserName());
                intent.putExtra("Interest", client.getInterest());
                startActivity(intent);
                LoginActivity.this.finish();

<<<<<<< 21bac057cb9d370e2c3152fe84fd28a34769926b
            } else if (message instanceof MsgConnectionStatus){
                MsgConnectionStatus connectionMessage = (MsgConnectionStatus)message;
                System.out.println("Got message status: " + connectionMessage.isConnected());
                if(!connectionMessage.isConnected()){
                    System.out.println("Wasn't connected");
                    progress.dismiss();
                    noConnectionToast.show();
                }else {
                    System.out.println("Connected to server! Set username and interests");
                    userName = userNameInput.getText().toString();
                    if (TextUtils.isEmpty(userName)) {
                        loginToast.show();
                        return;
                    }
                    interest = interestInput.getText().toString();

                    IServerMessage serverMessage = new MsgChooseNickname(userName, interest);
                    Event nameEvent = new ToServerEvent(serverMessage);
                    eventBus.postEvent(nameEvent);
                }


=======
                /*
>>>>>>> Continued on testbranch
                Intent intent = new Intent(LoginActivity.this, MainChatActivity.class);
                intent.putExtra("Username", client.getUserName());
                intent.putExtra("Interest", client.getInterest());
                Log.d("MyTag", "Trying to start Mainchatactivity");
                progress.dismiss();
                startActivity(intent);
                LoginActivity.this.finish();
                */
            }
        }
    }
}
