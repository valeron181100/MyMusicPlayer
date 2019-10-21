package com.example.mymusicplayer.models;

import java.util.ArrayList;
import java.util.Observable;
import java.util.UUID;

public class ObservableBoolean {

    private Boolean value;
    private ArrayList<OnChangeValueListener> listeners;


    public ObservableBoolean(boolean value) {
        listeners = new ArrayList<>();
        this.value = value;
    }

    public ObservableBoolean() {
        listeners = new ArrayList<>();
        this.value = null;
    }


    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        for(OnChangeValueListener p : listeners){
            p.onChange(this.value, value);
        }
        this.value = value;
    }

    public void addOnChangeValueListener(OnChangeValueListener listener){
        listeners.add(listener);
    }

    public interface OnChangeValueListener{
        void onChange(Boolean oldVal, Boolean newVal);
    }
}
