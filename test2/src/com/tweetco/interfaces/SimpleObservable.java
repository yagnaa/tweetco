package com.tweetco.interfaces;

import java.util.ArrayList;

/**
 * Created by kirankum on 6/13/2015.
 */
public class SimpleObservable<T> {

    private final ArrayList<OnChangeListener<T>> listeners = new ArrayList<OnChangeListener<T>>();


    public void addListener(OnChangeListener<T> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    public void removeListener(OnChangeListener<T> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected void notifyObservers(final T model) {
        synchronized (listeners) {
            for (OnChangeListener<T> listener : listeners) {
                listener.onChange(model);
            }
        }
    }

}
