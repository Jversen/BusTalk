package com.busgen.bustalk.model;

import android.util.Log;

import com.busgen.bustalk.model.ServerMessages.MsgChatMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;

/**
 * Created by Johan on 2015-10-02.
 */
public class Chatroom implements IChatroom {

    private int chatID;
    private String title;
    private List<IUser> users;
    private List<MsgChatMessage> messages;

    public Chatroom(int chatID, String title){
        this.chatID = chatID;
        this.title = title;
        users = new ArrayList<IUser>();
        messages = new ArrayList<MsgChatMessage>();
    }


    @Override
    public int getChatID() {
        return chatID;
    }

    @Override
    public void setID(int chatID) {
        this.chatID = chatID;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void addUser(IUser user) {
        users.add(user);
    }

    @Override
    public void removeUser(IUser user) {
        users.remove(user);
    }

    @Override
    public boolean containsUser(IUser user) {
        return users.contains(user);
    }

    @Override
    public int getNbrOfUsers() {
        return users.size();
    }

    @Override
    public boolean isEmpty() {
        return (users.size() == 0);
    }

    @Override
    public Collection<IUser> getUsers() {
        return users;
    }

    @Override
    public List<MsgChatMessage> getMessages() {
        return messages;
    }

    @Override
    public void terminate() {
    }

    @Override
    public void addMessage(MsgChatMessage message) {
       // Log.d("MyTag", "Inside addMessage in Chatroom");
       // messages.add(message);
    }
}
