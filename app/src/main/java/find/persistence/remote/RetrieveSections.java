package find.persistence.remote;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import find.json.JsonParser;
import find.persistence.DatabaseManager;
import find.routes.Section;

public class RetrieveSections extends AsyncTask<String, Void, List<Section>> {

    DatabaseManager dbm;

    public RetrieveSections(DatabaseManager dbm) {
        this.dbm = dbm;
    }

    @Override
    protected List<Section> doInBackground(String... url) {
        List<Section> sections = null;

        try {
            URL sectionsUrl = new URL(url[0]);
            InputStream response = RemoteDatabaseAccess.requestServer(sectionsUrl);

            sections = JsonParser.readJsonSections(response);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sections;
    }

    @Override
    protected void onPostExecute(List<Section> sections) {
        if (sections != null) {
            dbm.onRemoteSectionsRetrieved(sections);
        }
    }

}
