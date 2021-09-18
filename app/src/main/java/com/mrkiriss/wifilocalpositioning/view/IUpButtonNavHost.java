package com.mrkiriss.wifilocalpositioning.view;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;

import java.util.Objects;

public interface IUpButtonNavHost {
    void useUpButton();
    void useHamburgerButton();
    void navigateTo(Fragment current, Fragment target, String fragmentName);
    void navigateBack(Fragment current, Fragment target);
}