package find.persistence.local;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import find.routes.Route;
import find.routes.Section;

public class LocalDatabaseAccess {

    private static OnLocalDBListener listener;
    private static Context ctx;
    private static SQLiteDatabase db;

    public static void start(Activity activity) {
        listener = (OnLocalDBListener) activity;
        ctx = activity.getApplicationContext();

        OpenDatabaseTask odt = new OpenDatabaseTask();
        odt.execute();
    }

    public static void stop() {
        db.close();
    }

    public static List<Section> findSections() {
        return SectionDAO.findSections(db, ctx);
    }

    public static List<Route> findRoutes() {
        return RouteDAO.findRoutes(db, ctx);
    }

    public static void insertSections(final List<Section> sections) {
        // convert list of sections to array of sections
        Section[] secArray = new Section[sections.size()];
        secArray = sections.toArray(secArray);

        // if db is still active then insert sections in a new thread
        InsertSections is = new InsertSections();
        is.execute(secArray);
    }

    public static void insertRoutes(List<Route> routes) {
        // convert list of routes to array of routes
        Route[] routesArray = new Route[routes.size()];
        routesArray = routes.toArray(routesArray);

        // if db is still active then insert routes in a new thread
        InsertRoutes ir = new InsertRoutes();
        ir.execute(routesArray);
    }

    public static void updateSection(Section updatedSection) {
        SectionDAO.updateSection(db, updatedSection, ctx);
    }

    public static void updateRoute(Route updatedRoute) {
        RouteDAO.updateRoute(db, updatedRoute, ctx);
    }

    public static void updateLastServerAccess(long date) {
        ContentValues cv = new ContentValues();
        cv.put("lastAccess", date);
        db.delete(RoutesOpenHelper.SERVER_TABLE_NAME, null, null);
        db.insert(RoutesOpenHelper.SERVER_TABLE_NAME, null, cv);
    }

    public static long getLastServerAccess() {
        long date = -1;

        String[] columns = {"lastAccess"};
        Cursor cursor = db.query(RoutesOpenHelper.SERVER_TABLE_NAME, columns,
                null, null, null, null, null);

        if (cursor.moveToFirst())
            date = cursor.getLong(cursor.getColumnIndex("lastAccess"));

        cursor.close();
        return date;
    }

    public static boolean isNewer(Section s) {
        Section localSection = SectionDAO.findSection(db, s.getId(), ctx);
        return localSection != null
                && s.getTimestamp() > localSection.getTimestamp();
    }

    public static boolean hasChanged(Route r) {
        Route localRoute = RouteDAO.findRoute(db, r.getId(), ctx);
        return localRoute != null && !localRoute.equals(r);
    }

    public interface OnLocalDBListener {
        void onDatabaseReady();

        void onSectionsRetrieved(List<Section> sections,
                                 boolean fromServer);

        void onRoutesRetrieved(List<Route> routes, boolean fromServer);
    }

    private static class OpenDatabaseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RoutesOpenHelper dbHelper = new RoutesOpenHelper(ctx);
            db = dbHelper.getWritableDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            listener.onDatabaseReady();
        }

    }

    private static class InsertSections extends
            AsyncTask<Section, Void, List<Section>> {

        @Override
        protected List<Section> doInBackground(Section... sections) {
            List<Section> retrievedSections = new ArrayList<>();

            for (Section s : sections) {
                SectionDAO.insertSection(db, s, ctx);
                retrievedSections.add(s);
            }

            return retrievedSections;
        }

        @Override
        protected void onPostExecute(List<Section> sections) {
            if (sections != null && !sections.isEmpty())
                listener.onSectionsRetrieved(sections, true);
        }
    }

    private static class InsertRoutes extends
            AsyncTask<Route, Void, List<Route>> {

        @Override
        protected List<Route> doInBackground(Route... routes) {
            List<Route> retrievedRoutes = new ArrayList<>();

            for (Route r : routes) {
                RouteDAO.insertRoute(db, r, ctx);
                retrievedRoutes.add(r);
            }

            return retrievedRoutes;
        }

        @Override
        protected void onPostExecute(List<Route> routes) {
            if (routes != null && !routes.isEmpty())
                listener.onRoutesRetrieved(routes, true);
        }
    }
}
