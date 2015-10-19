package com.busgen.bustalk.service;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.busgen.bustalk.ConnectionsHandler;
import com.busgen.bustalk.PlatformCommunicator;
import com.busgen.bustalk.ServerCommunicator;
import com.busgen.bustalk.model.Client;
import com.busgen.bustalk.model.IClient;
import com.busgen.bustalk.model.IEventBusListener;

import java.net.URI;
import java.net.URISyntaxException;

public class MainService extends Service {

    private static MainService instance;

    private final IBinder binder = new MainBinder();
    private Client client;
    private EventBus eventBus;
    private ConnectionsHandler connectionsHandler;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate(){

        eventBus = EventBus.getInstance();
        client = new Client();
        connectionsHandler = new ConnectionsHandler();

        eventBus.register(client);

    }


    @Override
    public void onDestroy(){

    }

    public class MainBinder extends Binder {

        public MainService getService() {
            return MainService.this;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
