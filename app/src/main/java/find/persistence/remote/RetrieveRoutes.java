package find.persistence.remote;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import find.json.JsonParser;
import find.persistence.DatabaseManager;

public class RetrieveRoutes extends AsyncTask<String, Void, ServerResponse> {

    DatabaseManager dbm;

    public RetrieveRoutes(DatabaseManager dbm) {
        this.dbm = dbm;
    }

    @Override
    protected ServerResponse doInBackground(String... url) {
        ServerResponse sr = null;

        try {
            URL routesUrl = new URL(url[0]);
            InputStream response = RemoteDatabaseAccess
                    .requestServer(routesUrl);

            sr = JsonParser.readJsonRoutes(response);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sr;
    }

    @Override
    protected void onPostExecute(ServerResponse response) {
        if (response != null) {
            dbm.onRemoteSectionsRetrieved(response.getSections());
            dbm.onRemoteRoutesRetrieved(response.getRoutes());
        }
    }

}
