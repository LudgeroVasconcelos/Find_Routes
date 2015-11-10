package find.persistence;

import android.app.Activity;

import java.util.List;

import find.persistence.local.LocalDatabaseAccess;
import find.persistence.remote.RemoteDatabaseAccess;
import find.routes.Route;
import find.routes.Section;

public class DatabaseManager {

    private Activity activity;

    public DatabaseManager(Activity activity) {
        this.activity = activity;
    }

    public void start() {
        LocalDatabaseAccess.start(activity);
        RemoteDatabaseAccess.start(this);
    }

    public List<Section> findSections() {
        return LocalDatabaseAccess.findSections();
    }

    public List<Route> findRoutes() {
        return LocalDatabaseAccess.findRoutes();
    }

//	private void insertSections(List<Section> sections) {
//		LocalDatabaseAccess.insertSections(sections);
//	}

    public void updateSection(Section updatedSection) {
        LocalDatabaseAccess.updateSection(updatedSection);
        RemoteDatabaseAccess.updateSection(updatedSection);
    }

    public void updateRoute(Route updatedRoute) {
        LocalDatabaseAccess.updateRoute(updatedRoute);
//		RemoteDatabaseAccess.updateRoute(updatedRoute);

    }

    public void updateLastServerAccess(long date) {
        LocalDatabaseAccess.updateLastServerAccess(date);
        // RemoteDatabaseAccess.setLastServerAccess(date);
    }

    public long getLastServerAccess() {
        return LocalDatabaseAccess.getLastServerAccess();
    }

    public boolean needsUpdate(Section s) {
        return LocalDatabaseAccess.isNewer(s);
    }

    public boolean needsUpdate(Route r) {
        return LocalDatabaseAccess.hasChanged(r);
    }

    public void stop() {
        LocalDatabaseAccess.stop();
        RemoteDatabaseAccess.stop();
    }

    public void setArea(double minLat, double minLng, double maxLat,
                        double maxLng) {
        // increase the square by roughly 10km on each side
        double offset = 0.05;

        minLat -= offset;
        minLng -= offset;
        maxLat += offset;
        maxLng += offset;

        RemoteDatabaseAccess.setArea(minLat, minLng, maxLat, maxLng);
    }

    public void onRemoteSectionsRetrieved(List<Section> sections) {
        LocalDatabaseAccess.insertSections(sections);
    }

    public void onRemoteRoutesRetrieved(List<Route> routes) {
        LocalDatabaseAccess.insertRoutes(routes);

    }
}
