package find.fragments;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import find.map.R;
import find.routes.Section;

public class AppFragmentManager {

    private FragmentManager fragmentManager;

    private SearchFragment searchFrag;
    private InfoFragment infoFrag;

    public AppFragmentManager(FragmentManager fragmentManager, SearchFragment searchFrag, InfoFragment infoFrag) {
        this.fragmentManager = fragmentManager;
        this.searchFrag = searchFrag;
        this.infoFrag = infoFrag;

        // hide fragments
        /*
        setSearchFragmentVisibility(false);
        setInfoFragmentVisibility(false);*/
    }

    public void showSectionInfo(Section section) {
        changeVisibility(false, searchFrag);
        changeVisibility(true, infoFrag);
        infoFrag.updateView(section);
    }

    public void showSearchFrag() {
        changeVisibility(false, infoFrag);
        changeVisibility(!searchFrag.isAdded(), searchFrag);
    }

    public void setMode(boolean mode) {
        searchFrag.setMode(mode);
    }

    private void changeVisibility(boolean visibility, Fragment frag) {

        if (frag.isAdded() && !visibility) {
            fragmentManager.beginTransaction().remove(frag).commit();
            fragmentManager.popBackStack();
        } else if (!frag.isAdded() && visibility) {
            fragmentManager.beginTransaction().add(R.id.details, frag)
                    .addToBackStack(null).commit();
        }

        fragmentManager.executePendingTransactions();
    }

    public void closeSectionInfo() {
        changeVisibility(false, infoFrag);
    }
}
