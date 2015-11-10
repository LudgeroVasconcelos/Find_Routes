package find.map;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import find.MainActivity;
import find.routes.Occurrence;
import find.routes.Route;
import find.routes.Section;
import find.util.PolyUtil;

public class FindMap implements GoogleMap.OnMapClickListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int TOLERANCE = 50; // meters
    FindMapListener listener;
    private GoogleMap map;
    private GoogleApiClient gac;
    private Map<Integer, Polyline> polis;
    private Map<Integer, Marker> markers;
    private Map<Integer, Section> sections;
    private Map<Integer, Route> routes;
    private Polyline selectedPoly;
    private int oldColor;

    // initialize map
    public FindMap(MainActivity activity) {
        listener = activity;

        polis = new HashMap<>();
        markers = new HashMap<>();
        sections = new HashMap<>();
        routes = new HashMap<>();

        // add map
        SupportMapFragment map = (SupportMapFragment) activity.getSupportFragmentManager()
                .findFragmentById(R.id.map);

        map.getMapAsync(this);
    }

    private static boolean isConform(Section s, int[] risks) {
        Occurrence[] occurrences = Occurrence.values();

        for (int i = 0; i < occurrences.length; i++)
            if (s.getRisk(occurrences[i]) > risks[i])
                return false;

        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnCameraChangeListener(this);

        listener.onMapReady();
    }

    public void moveToLocation(GoogleApiClient gac) {
        this.gac = gac;
        gac.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(gac);

        if (mLastLocation != null) {
            LatLng position = new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12));
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        LatLng lisbon = new LatLng(38.7300754, -9.1811491);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lisbon, 12));
    }

    public void showSections() {
        for (Section s : sections.values()) {
            addSectionToMap(s);
        }
    }

    public void showRoutes() {
        for (Route r : routes.values()) {
            addRouteToMap(r);
        }
    }

    public void addNewSection(Section s, boolean addToMap) {
        if (!sections.containsKey(s.getId())) {
            if (addToMap)
                addSectionToMap(s);
            sections.put(s.getId(), s);
        }
    }

    public void addNewRoute(Route r, boolean addToMap) {
        if (!routes.containsKey(r.getId())) {
            if (addToMap)
                addRouteToMap(r);
            routes.put(r.getId(), r);
        }
    }

    private void addRouteToMap(Route r) {
        for (Integer sectionId : r.getPath()) {
            if (!polis.containsKey(sectionId)) { // do not add repeated sections
                addSectionToMap(sections.get(sectionId));
            }
        }
    }

    private void addSectionToMap(Section s) {

        double avg = calcAverageRisks(s);

        PolylineOptions plo = new PolylineOptions();
        plo.add(s.getPoints());
        plo.color(Color.rgb((int) avg * 255 / 100, 0, 0));
        plo.width(9);
        Polyline p = map.addPolyline(plo);
        polis.put(s.getId(), p);

        if (!s.isOpen())
            addMarker(s.getId(), s.getPoints());
    }

    @Override
    public void onCameraChange(CameraPosition cam) {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        listener.onCameraChange(bounds);
    }

    @Override
    public boolean onMarkerClick(Marker m) {
        for (Map.Entry<Integer, Marker> e : markers.entrySet())
            if (m.equals(e.getValue()))
                onSectionClick(sections.get(e.getKey()));
        return true;
    }

    @Override
    public void onMapClick(LatLng point) {

        for (Section section : sections.values()) {
            // check if the user clicked on a section
            if (PolyUtil.isLocationOnPath(point,
                    new ArrayList<>(Arrays.asList(section.getPoints())), true,
                    TOLERANCE)) {

                onSectionClick(section);
                break;
            }
        }
    }

    public void updateSection(Section section) {
        int id = section.getId();

        // check if section is on the map
        if (polis.containsKey(id)) {
            // update color
            double avg = calcAverageRisks(section);
            int newColor = Color.rgb((int) avg * 255 / 100, 0, 0);
            Polyline p = polis.get(id);
            p.setColor(newColor);

            // update marker
            if (!markers.containsKey(id) && !section.isOpen()) {
                addMarker(id, sections.get(id).getPoints());
            } else if (markers.containsKey(id) && section.isOpen()) {
                markers.get(id).remove();
                markers.remove(id);
            }
        }

        // update hashMap
        // a section may be in memory but not on map (ex: app is in routes mode)
        if (sections.containsKey(id))
            sections.put(id, section);
    }

    private void addMarker(int id, LatLng[] points) {
        LatLng middle = new LatLng(
                (points[0].latitude + points[1].latitude) / 2,
                (points[0].longitude + points[1].longitude) / 2);
        MarkerOptions markerOpt = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.x_wrong3))
                .position(middle).anchor(0.5f, 0.5f);
        Marker marker = map.addMarker(markerOpt);
        markers.put(id, marker);
    }

    private double calcAverageRisks(Section s) {
        int sum = 0;
        for (Occurrence oc : Occurrence.values())
            sum += s.getRisk(oc);

        return sum / Occurrence.values().length;
    }

    // TODO: check screen rotation
    private void onSectionClick(Section section) {
        // deselect former section
        deselectPoly();

        // set highlight color
        selectedPoly = polis.get(section.getId());
        oldColor = selectedPoly.getColor();
        selectedPoly.setColor(Color.rgb(0, 0, 255));

        listener.onSectionClick(section);
    }

    public boolean searchSections(int[] risks) {
        if (map == null)
            return true;

        clear();

        boolean added = false;
        for (Section s : sections.values()) {

            if (isConform(s, risks)) {
                addSectionToMap(s);
                added = true;
            }
        }

        return added;
    }

    public boolean searchRoutes(int[] risks) {
        if (map == null)
            return true;

        clear();

        boolean anyRouteShown = false;
        for (Route r : routes.values()) {
            boolean show = true;
            for (Integer sectionId : r.getPath()) {
                Section s = sections.get(sectionId);

                if (!isConform(s, risks)) {
                    show = false;
                    break;
                }
            }
            if (show) {
                addRouteToMap(r);
                anyRouteShown = true;
            }
        }

        return anyRouteShown;
    }

    public void deselectPoly() {
        if (selectedPoly != null) {
            selectedPoly.setColor(oldColor);
            selectedPoly = null;
        }
    }

    public void clear() {
        // TODO: deselect selected section ?
        polis.clear();
        markers.clear();
        map.clear();
    }

    public interface FindMapListener {
        void onMapReady();

        void onCameraChange(LatLngBounds bounds);

        void onSectionClick(Section section);
    }


}
