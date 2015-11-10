package find;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLngBounds;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import find.fragments.AppFragmentManager;
import find.fragments.InfoFragment;
import find.fragments.SearchFragment;
import find.map.FindMap;
import find.map.R;
import find.persistence.DatabaseManager;
import find.persistence.local.LocalDatabaseAccess.OnLocalDBListener;
import find.routes.Route;
import find.routes.Section;

public class MainActivity extends ActionBarActivity implements FindMap.FindMapListener,
        SearchFragment.OnSeekBarReleasedListener,
        InfoFragment.OnSectionUpdateListener,
        OnLocalDBListener {

    public static final String TAG = "route";

    private AppFragmentManager fragmentManager;

    private SimpleDateFormat simpleDateFormat;
    private String appName;

    private FindMap map;

    private DatabaseManager dbm;

    private boolean mode; // true if sections mode, false otherwise

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.getDefault());
        appName = getResources().getString(R.string.app_name);

        fragmentManager = new AppFragmentManager(getSupportFragmentManager(), new SearchFragment(), new InfoFragment());
        map = new FindMap(this);
        dbm = new DatabaseManager(this);

        /*
        Button button = (Button) findViewById(R.id.button_db);

        if (button != null) {
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {

                    Intent dbmanager = new Intent(getApplicationContext(),
                            AndroidDatabaseManager.class);
                    startActivity(dbmanager);
                }
            });
        }
        */

        final ActionBar actionBar = getSupportActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        TabListener tabListener = new TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
                if (tab.getText().equals("Sections")) {
                    mode = true;
                    map.showSections();

                } else {
                    mode = false;
                    map.showRoutes();
                }
                fragmentManager.setMode(mode);
            }

            public void onTabUnselected(ActionBar.Tab tab,
                                        FragmentTransaction ft) {
                map.clear();
            }

            public void onTabReselected(ActionBar.Tab tab,
                                        FragmentTransaction ft) {
                // ignore this event
            }
        };

        actionBar.addTab(actionBar.newTab().setText("Sections")
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Routes")
                .setTabListener(tabListener));

        getSupportFragmentManager().popBackStack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            fragmentManager.showSearchFrag();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        dbm.stop();
        super.onDestroy();
    }

    @Override
    public void onMapReady() {
        // get user last known location and move map camera to that location
        // https://developer.android.com/training/location/retrieve-current.html#last-known
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(map)
                .addOnConnectionFailedListener(map)
                .addApi(LocationServices.API).build();

        map.moveToLocation(mGoogleApiClient);

        // start remote and local databases
        dbm.start();
    }

    @Override
    public void onDatabaseReady() {

        // get last server access time
        long date = dbm.getLastServerAccess();
        if (date != -1)
            setTimeUI(date);

        // get sections and routes from local db
        List<Section> sections = dbm.findSections();
        onSectionsRetrieved(sections, false);

        List<Route> routes = dbm.findRoutes();
        onRoutesRetrieved(routes, false);
    }

    // TODO create class to do all the operations to map. contains all the hashmaps
    @Override
    public void onSectionsRetrieved(List<Section> retrievedSections,
                                    boolean fromServer) {

        // all new sections were already inserted to the local db
        if (fromServer)
            updateLastServerAccess();

        for (Section s : retrievedSections) {

            // check if section was updated
            // TODO fragment closes
            if (fromServer && dbm.needsUpdate(s))
                updateSection(s);

            // TODO add only sections inside square
            // add new sections to the map
            map.addNewSection(s, mode);
        }
    }

    // TODO change this method after adding routes drawer list
    public void onRoutesRetrieved(List<Route> retrievedRoutes,
                                  boolean fromServer) {
        if (fromServer)
            updateLastServerAccess();

        for (Route r : retrievedRoutes) {
            if (fromServer && dbm.needsUpdate(r)) {
                updateRoute(r);
//                if (routes.containsKey(r.getId()) && !mode) {
//                    // updateRoute(r);
//                } else {
//                    if (routes.containsKey(r.getId()))
//                        routes.put(r.getId(), r);
//                    dbm.updateRoute(r);
//                }
            }
            map.addNewRoute(r, !mode);
        }
    }

    @Override
    public void onCameraChange(LatLngBounds bounds) {
        dbm.setArea(bounds.southwest.latitude, bounds.southwest.longitude,
                bounds.northeast.latitude, bounds.northeast.longitude);
    }

    // TODO move this to other class
    private void updateLastServerAccess() {
        long date = System.currentTimeMillis();
        setTimeUI(date);
        dbm.updateLastServerAccess(date);
    }

    private void setTimeUI(long date) {
        String dateString = simpleDateFormat.format(date);
        setTitle(appName + ".    Last server update " + dateString);
    }

    @Override
    public void onInfoAttached() {
        View v = findViewById(R.id.details);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onInfoClosed() {
        closeView();

        if (map != null) // check if map is ready
            map.deselectPoly();
    }

    @Override
    public void onSearchAttached() {
        View v = findViewById(R.id.details);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSearchClosed() {
        closeView();
    }

    private void closeView() {
        View c = findViewById(R.id.details);
        View p = ((ViewGroup) c.getParent());

        String name = getResources().getResourceEntryName(p.getId());

        if (name.equals("frags"))
            findViewById(R.id.details).setVisibility(View.GONE);
    }

    @Override
    public void onSectionClick(Section section) {
        // show section details
        fragmentManager.showSectionInfo(section);
    }

    @Override
    public void updateSection(Section section) {
        fragmentManager.closeSectionInfo();

        // update on the map
        map.updateSection(section);

        // update in the database
        dbm.updateSection(section);
    }

    private void updateRoute(Route route) {
//        map.updateRoute(route);
        dbm.updateRoute(route);
    }

    @Override
    public void search(int[] risks) {
        boolean success;

        if (mode)
            success = map.searchSections(risks);
        else
            success = map.searchRoutes(risks);

        if (!success) {
            Toast.makeText(this, "No " + (mode ? "sections" : "routes") + " matched your criteria",
                    Toast.LENGTH_LONG).show();
        }
    }

    // private boolean isTablet() {
    // return (getResources().getConfiguration().screenLayout
    // & Configuration.SCREENLAYOUT_SIZE_MASK)
    // >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    // }
}
