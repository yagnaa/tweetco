package com.tweetco.interfaces;

/**
 * Created by kirankum on 6/13/2015.
 */
public interface EasyObservable<T> {
    void addListener(OnChangeListener<T> listener);
    void removeListener(OnChangeListener<T> listener);
}
