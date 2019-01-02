package com.example.wnsvy.kakaocalorie.Interface;

public interface AsyncTaskEventListener<T> {
    public void onSuccess(T object);
    public void onFailure(Exception e);
}
