package com.example.key.my_carpathians.interfaces;

/**
 * Created by key on 08.08.17.
 */

public interface CommunicatorStartActivity {
    void putStringNameRout(String name, int type);
    void putStringNamePlace(String name, int type);
    void deletedFromFavoriteList(String name, int type);
    void deletedFromCreatedList(String name, int type);
}