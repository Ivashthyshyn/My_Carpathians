package com.keyVas.key.my_carpathians.interfaces;

import com.keyVas.key.my_carpathians.models.Place;
import com.keyVas.key.my_carpathians.models.Rout;

import java.util.List;

/**
 * Created by key on 08.08.17.
 */

public interface CommunicatorStartActivity {
    void putStringNameRout(Rout rout);
    void putStringNamePlace(Place place );
    void deletedFromFavoriteList(List<String> name, int type);
    void deletedFromCreatedList(List<String> name, int type);
}
