package find.persistence.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import find.routes.Route;

public class RouteDAO {

    public static List<Route> findRoutes(SQLiteDatabase db, Context ctx) {
        List<Route> routesFromLocalDb = new ArrayList<>();

        Cursor cursor = db.query(RoutesOpenHelper.ROUTE_TABLE_NAME, null, null,
                null, null, null, null);

        if (cursor.moveToFirst()) {

            do {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String description = cursor.getString(cursor
                        .getColumnIndex("description"));
                List<Integer> path = new ArrayList<>();

                Cursor pathCursor = db.query(
                        RoutesOpenHelper.COLLECTION_TABLE_NAME, null,
                        "route_id = ?", new String[]{String.valueOf(id)},
                        null, null, null);

                if (pathCursor.moveToFirst()) {
                    do {
                        path.add(pathCursor.getInt(pathCursor.getColumnIndex("section_id")));
                    } while (pathCursor.moveToNext());
                }
                pathCursor.close();
                routesFromLocalDb.add(new Route(id, name, description, path));
            } while (cursor.moveToNext());

            cursor.close();
        }
        return routesFromLocalDb;
    }

    public static void insertRoute(SQLiteDatabase db, Route r, Context ctx) {

        // check if this route already exists
        Cursor mCursor = db.rawQuery("SELECT 1 FROM "
                        + RoutesOpenHelper.ROUTE_TABLE_NAME + " WHERE _id = ?",
                new String[]{String.valueOf(r.getId())});

        if (mCursor.getCount() == 0) {

            ContentValues cvRoute = new ContentValues();
            cvRoute.put("_id", r.getId());
            cvRoute.put("name", r.getName());
            cvRoute.put("description", r.getDescription());

            db.insert(RoutesOpenHelper.ROUTE_TABLE_NAME, null, cvRoute);

            insertPath(db, r);
        }
        mCursor.close();
    }

    private static void insertPath(SQLiteDatabase db, Route r) {
        for (Integer sectionId : r.getPath()) {
            ContentValues cvCollection = new ContentValues();
            cvCollection.put("route_id", r.getId());
            cvCollection.put("section_id", sectionId);

            db.insert(RoutesOpenHelper.COLLECTION_TABLE_NAME, null,
                    cvCollection);
        }
    }

    public static Route findRoute(SQLiteDatabase db, int id, Context ctx) {
        Cursor cursor = db.query(RoutesOpenHelper.ROUTE_TABLE_NAME, null,
                "_id = ?", new String[]{String.valueOf(id)}, null, null,
                null);

        List<Integer> path = new ArrayList<>();

        if (!cursor.moveToFirst())
            return null;

        String name = cursor.getString(cursor.getColumnIndex("name"));
        String description = cursor.getString(cursor
                .getColumnIndex("description"));

        Cursor collectionCursor = db.query(
                RoutesOpenHelper.COLLECTION_TABLE_NAME, null, "route_id = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (collectionCursor.moveToFirst()) {

            do {
                path.add(collectionCursor.getInt(collectionCursor
                        .getColumnIndex("section_id")));
            } while (collectionCursor.moveToNext());
        }
        cursor.close();
        collectionCursor.close();

        return new Route(id, name, description, path);
    }

    public static void updateRoute(SQLiteDatabase db, Route route, Context ctx) {

        ContentValues cvRoute = new ContentValues();
        cvRoute.put("name", route.getName());
        cvRoute.put("description", route.getDescription());

        db.update(RoutesOpenHelper.ROUTE_TABLE_NAME, cvRoute, "_id = ?",
                new String[]{String.valueOf(route.getId())});

        db.delete(RoutesOpenHelper.COLLECTION_TABLE_NAME, "route_id = ?",
                new String[]{String.valueOf(route.getId())});

        insertPath(db, route);
    }
}
