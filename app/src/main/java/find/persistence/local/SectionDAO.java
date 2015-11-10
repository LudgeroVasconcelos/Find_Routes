package find.persistence.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import find.routes.Occurrence;
import find.routes.Section;

public class SectionDAO {

    public static List<Section> findSections(SQLiteDatabase db, Context ctx) {
        List<Section> sectionsFromLocalDb = new ArrayList<>();

        Cursor cursor = db.query(RoutesOpenHelper.SECTION_TABLE_NAME, null,
                null, null, null, null, null);

        if (cursor.moveToFirst()) {

            do {
                Map<Occurrence, Integer> sectionRisks = new HashMap<>();
                int id = cursor.getInt(cursor.getColumnIndex("_id"));

                double startLat = cursor.getDouble(cursor
                        .getColumnIndex("startLat"));
                double startLng = cursor.getDouble(cursor
                        .getColumnIndex("startLng"));
                double endLat = cursor.getDouble(cursor
                        .getColumnIndex("endLat"));
                double endLng = cursor.getDouble(cursor
                        .getColumnIndex("endLng"));
                long timestamp = cursor.getLong(cursor
                        .getColumnIndex("timestamp"));
                boolean state = cursor.getInt(cursor.getColumnIndex("state")) == 1;

                LatLng pointA = new LatLng(startLat, startLng);
                LatLng pointB = new LatLng(endLat, endLng);

                Cursor riskCursor = db.query(RoutesOpenHelper.RISK_TABLE_NAME,
                        null, "section_id = ?",
                        new String[]{String.valueOf(id)}, null, null,
                        "occurrence_id asc");

                if (riskCursor.moveToFirst()) {

                    for (Occurrence oc : Occurrence.values()) {
                        int level = riskCursor.getInt(riskCursor
                                .getColumnIndex("level"));
                        sectionRisks.put(oc, level);
                        riskCursor.moveToNext();
                    }
                }
                riskCursor.close();
                Section s = new Section(id, pointA, pointB, sectionRisks,
                        state, timestamp);
                sectionsFromLocalDb.add(s);

            } while (cursor.moveToNext());

            cursor.close();
        }
        return sectionsFromLocalDb;
    }

    public static void insertSection(SQLiteDatabase db, Section s, Context ctx) {

        // check if this section already exists
        Cursor mCursor = db.rawQuery("SELECT 1 FROM "
                        + RoutesOpenHelper.SECTION_TABLE_NAME + " WHERE _id = ?",
                new String[]{String.valueOf(s.getId())});

        if (mCursor.getCount() == 0) {
            LatLng[] points = s.getPoints();

            ContentValues cvSection = new ContentValues();
            cvSection.put("_id", s.getId());
            cvSection.put("startLat", points[0].latitude);
            cvSection.put("startLng", points[0].longitude);
            cvSection.put("endLat", points[1].latitude);
            cvSection.put("endLng", points[1].longitude);
            cvSection.put("state", s.isOpen());
            cvSection.put("timestamp", s.getTimestamp());

            db.insert(RoutesOpenHelper.SECTION_TABLE_NAME, null, cvSection);

            for (Occurrence oc : Occurrence.values()) {
                ContentValues cvRisk = new ContentValues();
                cvRisk.put("section_id", s.getId());
                cvRisk.put("occurrence_id", oc.ordinal() + 1);
                cvRisk.put("level", s.getRisk(oc));

                db.insert(RoutesOpenHelper.RISK_TABLE_NAME, null, cvRisk);
            }
        }
        mCursor.close();
    }

    public static void updateSection(SQLiteDatabase db, Section section,
                                     Context ctx) {

        ContentValues cvSection = new ContentValues();
        cvSection.put("state", section.isOpen());
        cvSection.put("timestamp", section.getTimestamp());

        db.update(RoutesOpenHelper.SECTION_TABLE_NAME, cvSection, "_id = ?",
                new String[]{String.valueOf(section.getId())});

        for (Occurrence oc : Occurrence.values()) {
            ContentValues cvRisk = new ContentValues();
            cvRisk.put("level", section.getRisk(oc));

            db.update(
                    RoutesOpenHelper.RISK_TABLE_NAME,
                    cvRisk,
                    "section_id = ? and occurrence_id = ?",
                    new String[]{String.valueOf(section.getId()),
                            String.valueOf(oc.ordinal() + 1)});
        }
    }

    public static Section findSection(SQLiteDatabase db, int id, Context ctx) {
        Cursor cursor = db.query(RoutesOpenHelper.SECTION_TABLE_NAME, null,
                "_id = ?", new String[]{String.valueOf(id)}, null, null, null);

        Map<Occurrence, Integer> sectionRisks = new HashMap<>();

        if (!cursor.moveToFirst()) return null;

        double startLat = cursor.getDouble(cursor
                .getColumnIndex("startLat"));
        double startLng = cursor.getDouble(cursor
                .getColumnIndex("startLng"));
        double endLat = cursor.getDouble(cursor
                .getColumnIndex("endLat"));
        double endLng = cursor.getDouble(cursor
                .getColumnIndex("endLng"));
        long timestamp = cursor.getLong(cursor
                .getColumnIndex("timestamp"));
        boolean state = cursor.getInt(cursor.getColumnIndex("state")) == 1;

        LatLng pointA = new LatLng(startLat, startLng);
        LatLng pointB = new LatLng(endLat, endLng);

        Cursor riskCursor = db.query(RoutesOpenHelper.RISK_TABLE_NAME,
                null, "section_id = ?",
                new String[]{String.valueOf(id)}, null, null,
                "occurrence_id asc");

        if (riskCursor.moveToFirst()) {

            for (Occurrence oc : Occurrence.values()) {
                int level = riskCursor.getInt(riskCursor
                        .getColumnIndex("level"));
                sectionRisks.put(oc, level);
                riskCursor.moveToNext();
            }
        }
        cursor.close();
        riskCursor.close();
        return new Section(id, pointA, pointB, sectionRisks,
                state, timestamp);
    }
}
