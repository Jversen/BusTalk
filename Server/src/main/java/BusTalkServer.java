import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Kristoffer on 2015-09-29.
 */



@ServerEndpoint(value = "/chat", encoders = BusTalkJsonEncoder.class, decoders = BusTalkJsonDecoder.class)
public class BusTalkServer {

    // Message type constants

    private final int CHAT_MESSAGE = 11;

    private final int CREATE_ROOM_REQUEST = 21;
    private final int JOIN_ROOM_REQUEST = 22;
    private final int LIST_OF_USERS_IN_ROOM_REQUEST = 23;
    private final int LIST_OF_ALL_CHATROOMS_REQUEST = 26;
    private final int LEAVE_ROOM_REQUEST = 29;

    private final int CHOOSE_NICKNAME_REQUEST = 31;
    private final int NICKNAME_AVAILABLE_CHECK = 32;

    //Notification number that's being sent back
    private final int NEW_USER_IN_CHAT_NOTIFICATION = 101;
    private final int LIST_OF_CHATROOMS_NOTIFICATION = 102;
    private final int LIST_OF_USERS_IN_CHAT_NOTIFICATION = 103;
    private final int CREDENTIAL_CHANGE_NOTIFICATION = 104;
    private final int CHAT_MESSAGE_NOTIFICATION = 105;
    private final int USER_LEFT_ROOM_NOTIFICATION = 106;
    private final int ROOM_DELETED_NOTIFICATION = 107;
    private final int ROOM_CREATED_NOTIFICATION = 108;
    //TODO: Rensa och snygga upp bland dessa?

    private final Map<Integer, Chatroom> idToChatroom;
    private final BiMap<User, Session> userToSession;
    private final List<String> disallowedNames;
    private final Logger LOGGER;
    private final ChatroomFactory chatroomFactory;
    
    public BusTalkServer(){

        idToChatroom = new HashMap<Integer, Chatroom>();
        userToSession = HashBiMap.create();
        disallowedNames = new ArrayList<String>();

        disallowedNames.add("Alexander Kloutschek"); //TIHI
        LOGGER = Logger.getLogger(BusTalkServer.class.getName());
        chatroomFactory = ChatroomFactory.getFactory();

        //TEST ROOMS

        Chatroom testRoom1 = new Chatroom(1, "test1");
        Chatroom testRoom2 = new Chatroom(2, "test1");
        Chatroom testRoom3 = new Chatroom(3, "test1");
        Chatroom testRoom4 = new Chatroom(4, "test1");
        Chatroom testRoom5 = new Chatroom(5, "test1");

        idToChatroom.put(1, testRoom1);
        idToChatroom.put(2, testRoom2);
        idToChatroom.put(3, testRoom3);
        idToChatroom.put(4, testRoom4);
        idToChatroom.put(5, testRoom5);

    }

    @OnMessage
    public void onMessage(UserMessage userMessage, Session session){
        handleInput(userMessage, session);
    }

    @OnOpen
    public void onOpen(Session session){
        LOGGER.log(Level.INFO, String.format("[{0}] Connected to server.", session.getId()));
    }

    @OnError
    public void onError(Throwable exception, Session session){

    }


    @OnClose
    public void onClose(Session session){
        LOGGER.log(Level.INFO, String.format("[{0}] Disconnected from server."));
    }



    private void handleInput(UserMessage userMessage, Session session){
        try {
            int type = userMessage.getInt("type");

            switch(type){
                case CHAT_MESSAGE:
                    sendChatMessage(userMessage, session);
                    break;

                case CREATE_ROOM_REQUEST:
                {
                    String nameOfRoom = userMessage.getString("chatName");
                    Chatroom chatroom = chatroomFactory.createChatroom(nameOfRoom);
                    int chatId = chatroom.getIdNbr();

                    LOGGER.log(Level.INFO, String.format("[{0}] Created chat {1} with id {2}", new Object[]{session.getId(), nameOfRoom, chatId}));

                    idToChatroom.put(chatId, chatroom);
                    chatroom.subscribeToRoom(userToSession.inverse().get(session));

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", ROOM_CREATED_NOTIFICATION);
                    jsonObject.put("title", nameOfRoom);
                    jsonObject.put("id", chatId);
                    for (Session s : userToSession.values()) {
                        s.getAsyncRemote().sendObject(jsonObject);
                    }

                }
                    break;
                case JOIN_ROOM_REQUEST:
                {
                    int chatId = userMessage.getInt("chatId");
                    Chatroom chatRoom = idToChatroom.get(chatId);
                    chatRoom.subscribeToRoom(userToSession.inverse().get(session));

                    LOGGER.log(Level.INFO, String.format("[{0}] Joined room {1} ({2})"), new Object[]{session.getId(), chatRoom.getTitle(), chatId});

                    newUserInChatNotification(chatRoom, userToSession.inverse().get(session));
                }
                    break;
                case LIST_OF_USERS_IN_ROOM_REQUEST:
                    sendListOfUsersInChat(userMessage, session);
                    break;
                case LIST_OF_ALL_CHATROOMS_REQUEST:
                    sendListOfChatrooms(userMessage, session);
                    break;
                case LEAVE_ROOM_REQUEST: // Leave room
                {
                    int chatId = userMessage.getInt("chatId");
                    Chatroom chatroom = idToChatroom.get(chatId);

                    chatroom.unsubscribeToRoom(userToSession.inverse().get(session));

                    LOGGER.log(Level.INFO, String.format("[{0}] Left room {1} ({2})"), new Object[]{session.getId(), chatroom.getTitle(), chatId});

                    if(chatroom.getChatroomUsers().isEmpty() && chatroom.getIdNbr() > 100){
                        idToChatroom.remove(chatId);

                        LOGGER.log(Level.INFO, String.format("Chat room {0} ({1}) was removed."), new Object[]{chatroom.getTitle(), chatId});

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("type", ROOM_DELETED_NOTIFICATION);
                        jsonObject.put("chatId", chatId);

                        for (Session s : userToSession.values()) {
                            s.getAsyncRemote().sendObject(jsonObject);
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("type", USER_LEFT_ROOM_NOTIFICATION);
                        jsonObject.put("chatId", chatId);
                        User user = userToSession.inverse().get(session);
                        jsonObject.put("name", user.getName());

                        for (User u : chatroom.getChatroomUsers()) {
                            Session s = userToSession.get(u);
                            s.getAsyncRemote().sendObject(jsonObject);
                        }
                    }
                }

                    break;
                case CHOOSE_NICKNAME_REQUEST:
                    changeNickOrInterest(userMessage, session);
                    break;
                case NICKNAME_AVAILABLE_CHECK:
                    break;

                default:

            }
        }catch(IllegalArgumentException e){
            //TODO: Vi ska skicka tillbaka information om Vad som gick fel
            session.getAsyncRemote().sendText(e.getMessage());
        }
    }

    private void addDisallowedName(String name){
        if(!disallowedNames.contains(name)) {
            disallowedNames.add(name);
            LOGGER.log(Level.INFO, String.format("Added \"{0}\" to list of disallowed names", name));
        }
    }

    private void removeDisallowedName(String name){
        //if(disallowedNames.contains(name)){
            disallowedNames.remove(name);
            LOGGER.log(Level.INFO, String.format("Removed \"{0}\" from list of disallowed names", name));
        //}
    }

    private void addUser(User user, Session session){
        userToSession.put(user, session);
        LOGGER.log(Level.INFO, String.format("[{0}] \"{1}\" added to user list"), new Object[]{session.getId(), user.getName()});
        disallowedNames.add(user.getName());
    }

    private void removeUser(User user){
        disallowedNames.remove(user.getName());
        userToSession.remove(user);
        LOGGER.log(Level.INFO, String.format("[{0}] \"{1}\" removed from user list"), new Object[]{userToSession.get(user).getId(), user.getName()});
    }

    private void sendChatMessage(UserMessage userMessage, Session session) {
        User sender = userToSession.inverse().get(session);

        int chatId = userMessage.getInt("chatId");
        Chatroom chatRoom = idToChatroom.get(chatId);
        String message = userMessage.getString("message");

        for (User u : chatRoom.getChatroomUsers()) {
            Session s = userToSession.get(u);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", CHAT_MESSAGE_NOTIFICATION);
            jsonObject.put("chatId", chatId);
            jsonObject.put("sender", sender.getName());
            jsonObject.put("message", message);
            jsonObject.put("time", new Date().toString());

            s.getAsyncRemote().sendObject(jsonObject);
        }
    }

    private void changeNickOrInterest(UserMessage userMessage, Session session) {
        String newNickName = userMessage.getString("name");
        String newInterest = userMessage.getString("interests");
        int status = 0;

        if (!disallowedNames.contains(newNickName)){

            //DOES THE USER EXIST? IF NOT - DO THIS
            if(!userToSession.containsValue(session)) {
                User user = new User(newNickName, newInterest);
                addUser(user, session);
                LOGGER.log(Level.INFO, "[" + session.getId() + "]" + "User \"" + user.getName() + "created with interests \"" + newInterest + "\"");

                //THE USER EXIST - DO THIS
                //TODO: Maybe this should check if new interests = null and then leave the interests as they are?
            }else{
                User user = userToSession.inverse().get(session);

                //BEGONE WITH THE OLD
                String oldName = user.getName();
                String oldInterests = user.getInterests();
                removeDisallowedName(oldName);

                //...IN WITH THE NEW
                user.setName(newNickName);
                user.setInterests(newInterest);
                addDisallowedName(newNickName);

                LOGGER.log(Level.INFO, "[" + session.getId() + "] Name changed" + oldName + " -> " + newNickName
                        + " and interest " + oldInterests + " -> " + newInterest);
            }
            status = 1;
        /*
        TODO: Se till att två användare inte kan ha samma namn, och meddela användaren om det valt ett
        namn som är upptaget. Vad händer om en användare försöker byta till samma nick?
         */
        /*
        Här tar vi hand om situationen då en användare försöker byta till samma namn
        Vi plockar ut användaren
        Kollar om denna användares namn överensstämmer med det nya smeknamnet, till exempel då han vill ha en
        stor bokstav i mitten som en jävla chef, eller helt enkelt vill ha kvar namnet och enbart byta intressen

         */
        } else if (userToSession.inverse().get(session).getName().equalsIgnoreCase(newNickName)){ //Urgh
            User user = userToSession.inverse().get(session);
            //BEGONE WITH THE OLD
            String oldName = user.getName();
            String oldInterests = user.getInterests();
            removeDisallowedName(oldName);

            //...IN WITH THE NEW
            user.setName(newNickName);
            user.setInterests(newInterest);
            addDisallowedName(newNickName);

            status = 1;
            LOGGER.log(Level.INFO, "[" + session.getId() + "] Name changed" + oldName + " -> " + newNickName
                    + " and interest " + oldInterests + " -> " + newInterest);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", CREDENTIAL_CHANGE_NOTIFICATION);
        jsonObject.put("status", status);
        session.getAsyncRemote().sendObject(jsonObject);
    }

    private void sendListOfUsersInChat(UserMessage userMessage, Session session) {
        int chatId = userMessage.getInt("chatId");
        Chatroom chatRoom = idToChatroom.get(chatId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", LIST_OF_USERS_IN_CHAT_NOTIFICATION);
        for (User u : chatRoom.getChatroomUsers()) {
            Session s = userToSession.get(u);
            User user = userToSession.inverse().get(session);

            JSONObject jsonUser = new JSONObject();
            jsonUser.put("name", user.getName());
            jsonUser.put("interests", user.getInterests());

            jsonObject.append("users", user);
        }

        session.getAsyncRemote().sendObject(jsonObject);
    }

    private void sendListOfChatrooms(UserMessage userMessage, Session session) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", LIST_OF_CHATROOMS_NOTIFICATION);

        Iterator iterator = idToChatroom.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Chatroom chatroom = (Chatroom)pair.getValue();
            JSONObject jsonChatroom = new JSONObject();
            jsonChatroom.put("id", chatroom.getIdNbr());
            jsonChatroom.put("name", chatroom.getTitle());
            jsonObject.append("chatrooms", jsonChatroom);
        }

        session.getAsyncRemote().sendObject(jsonObject);
    }

    private void newUserInChatNotification(Chatroom chatroom, User user) {
        JSONObject objectToSend = new JSONObject();
        objectToSend.put("type", NEW_USER_IN_CHAT_NOTIFICATION); //What notification should be sent back?
        objectToSend.put("chatId", chatroom.getIdNbr());
        objectToSend.put("user", user.getName());
        objectToSend.put("interest", user.getInterests());

        for (User u : chatroom.getChatroomUsers()) {
            Session s = userToSession.get(u);
            s.getAsyncRemote().sendObject(objectToSend);
        }
    }
}
