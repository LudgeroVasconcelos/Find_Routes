package find.persistence.remote;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import find.MainActivity;
import find.persistence.DatabaseManager;
import find.routes.Occurrence;
import find.routes.Section;

public class RemoteDatabaseAccess {

    private static final String SECTIONS_METHOD = "http://accessible-serv.lasige.di.fc.ul.pt/~lost/FindRoutes/index.php/sections";
    private static final String ROUTES_METHOD = "http://accessible-serv.lasige.di.fc.ul.pt/~lost/FindRoutes/index.php/routes";
    private static final String UPDATE_SECTION = SECTIONS_METHOD
            + "/modify-section";

    private static final int PERIOD = 20 * 1000; // 20 seconds

    private static List<Section> sectionsToUpdate = new ArrayList<>();
    private static Timer timer;

    private static String sectionsUrl = SECTIONS_METHOD;
    private static String routesUrl = ROUTES_METHOD
            + "?minLat=36.97622678&minLng=-9.7668457&maxLat=42.04929264&maxLng=-6.18530273&name=&showClosed=true&maxDistance=&maxRisks[]=100&maxRisks[]=100&maxRisks[]=100&maxRisks[]=100";

    public static void updateSection(Section section) {
        sectionsToUpdate.add(section);
    }

    public static void start(final DatabaseManager databaseManager) {

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Log.d(MainActivity.TAG, "Let's run");

                if (isOnline()) {
                    Log.d(MainActivity.TAG, "Is online");
                    if (!sectionsToUpdate.isEmpty()) {
                        Log.d(MainActivity.TAG, "Let's update");
                        for (Section s : sectionsToUpdate) {

                            UpdateSection us = new UpdateSection();
                            us.execute(getUpdateSectionUrl(s));
                        }
                        sectionsToUpdate.clear();
                    }

                    RetrieveSections rs = new RetrieveSections(databaseManager);
                    rs.execute(sectionsUrl);

                    RetrieveRoutes rr = new RetrieveRoutes(databaseManager);
                    rr.execute(routesUrl);
                }
            }
        }, PERIOD, PERIOD);
    }

    public static void stop() {
        Log.d(MainActivity.TAG, "Let's stop task");
        if (isActive())
            timer.cancel();
    }

    public static InputStream requestServer(URL url) {
        HttpURLConnection connection;
        InputStream is = null;
        try {

            // prepare GET method request
            connection = (HttpURLConnection) url.openConnection();

            is = new BufferedInputStream(connection.getInputStream());
            int responseCode = connection.getResponseCode();

            Log.d(MainActivity.TAG, "the response after " + url.getPath()
                    + " was " + responseCode);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return is;
    }

    private static boolean isActive() {
        return timer != null;
    }

    private static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();

        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static String getUpdateSectionUrl(Section section) {
        return UPDATE_SECTION + "?id=" + section.getId() + "&isOpen="
                + section.isOpen() + "&risks[]="
                + section.getRisk(Occurrence.EARTHQUAKE) + "&risks[]="
                + section.getRisk(Occurrence.TSUNAMI) + "&risks[]="
                + section.getRisk(Occurrence.FIRE) + "&risks[]="
                + section.getRisk(Occurrence.LANDSLIP);
    }

    public static void setArea(double minLat, double minLng, double maxLat,
                               double maxLng) {

        sectionsUrl = SECTIONS_METHOD + "?minLat=" + minLat + "&minLng="
                + minLng + "&maxLat=" + maxLat + "&maxLng=" + maxLng;

        routesUrl = ROUTES_METHOD
                + "?minLat="
                + minLat
                + "&minLng="
                + minLng
                + "&maxLat="
                + maxLat
                + "&maxLng="
                + maxLng
                + "&name=&showClosed=true&maxDistance=&maxRisks[]=100&maxRisks[]=100&maxRisks[]=100&maxRisks[]=100";

    }
}
