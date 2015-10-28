package com.busgen.bustalk.service;

import com.busgen.bustalk.events.Event;
import com.busgen.bustalk.model.IEventBusListener;

import java.util.ArrayList;
import java.util.List;

public class EventBus {


    private static EventBus eventBus = null;
    private List<IEventBusListener> subscribers;


    private EventBus(){
        subscribers = new ArrayList<IEventBusListener>();
    }

    public static EventBus getInstance(){
        if(eventBus == null){
            eventBus = new EventBus();
        }return eventBus;
    }

    public void register(IEventBusListener subscriber) {
        if (!subscribers.contains(subscriber)) {
            this.subscribers.add(subscriber);
        }
    }

    public void unRegister(IEventBusListener subscriber){
        if (subscribers.contains(subscriber)){
            this.subscribers.remove(subscriber);
        }
    }

    public synchronized void postEvent(Event event){
        for (int i = 0; i < subscribers.size(); i++){

            System.out.println("skickar event till " + subscribers.size() + " subscribers...");
            subscribers.get(i).onEvent(event);
        }

    }


}
