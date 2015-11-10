package find.persistence.remote;

import android.os.AsyncTask;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import find.MainActivity;

public class UpdateSection extends AsyncTask<String, Void, Integer> {

    @Override
    protected Integer doInBackground(String... url) {

        int response = -1;
        try {
            URL modifySectionUrl = new URL(url[0]);
            RemoteDatabaseAccess.requestServer(modifySectionUrl);
//
//			BufferedReader in = new BufferedReader(
//			        new InputStreamReader(response));
//			
//			String line = in.readLine();
//			Log.d(MainActivity.TAG, line);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    protected void onPostExecute(Integer response) {
        Log.d(MainActivity.TAG, "Done updating section");
    }

}
