package find.json;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import find.persistence.remote.ServerResponse;
import find.routes.Occurrence;
import find.routes.Route;
import find.routes.Section;

public class JsonParser {

    private JsonParser() {
    }

    public static ServerResponse readJsonRoutes(InputStream in) {
        List<Route> routes = null;
        List<Section> sections = null;


        try {
            JsonReader reader = new JsonReader(new InputStreamReader(in,
                    "UTF-8"));

            reader.beginObject();
            while (reader.hasNext()) {
                String next = reader.nextName();
                if (next.equals("sections")) {
                    sections = readSectionArray(reader);
                } else if (next.equals("routes")) {
                    routes = readRoutesArray(reader);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            reader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return new ServerResponse(sections, routes);
    }

    public static List<Section> readJsonSections(InputStream in) {
        List<Section> sections = null;

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(in,
                    "UTF-8"));
            sections = readSectionArray(reader);
            reader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return sections;
    }

    private static List<Route> readRoutesArray(JsonReader reader)
            throws IOException, ParseException {
        List<Route> routes = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            routes.add(readRoute(reader));
        }
        reader.endArray();
        return routes;
    }

    private static Route readRoute(JsonReader reader) throws IOException, ParseException {
        int id = 0;
        String name = null;
        String description = null;
        List<Integer> path = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String next = reader.nextName();
            if (next.equals("id")) {
                id = reader.nextInt();
            } else if (next.equals("name")) {
                name = reader.nextString();
            } else if (next.equals("desc")) {
                description = reader.nextString();
            } else if (next.equals("sections") && reader.peek() != JsonToken.NULL) {
                reader.beginArray();
                while (reader.hasNext())
                    path.add(reader.nextInt());
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Route(id, name, description, path);
    }

    private static List<Section> readSectionArray(JsonReader reader)
            throws IOException, ParseException {
        List<Section> sections = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            sections.add(readSection(reader));
        }
        reader.endArray();
        return sections;
    }

    private static Section readSection(JsonReader reader) throws IOException, ParseException {
        int id = 0;
        double startLat = 0;
        double startLng = 0;
        double endLat = 0;
        double endLng = 0;
        Map<Occurrence, Integer> risks = null;
        boolean state = false;
        long timestamp = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String next = reader.nextName();
            if (next.equals("id")) {
                id = reader.nextInt();
            } else if (next.equals("startLat")) {
                startLat = reader.nextDouble();
            } else if (next.equals("startLng")) {
                startLng = reader.nextDouble();
            } else if (next.equals("endLat")) {
                endLat = reader.nextDouble();
            } else if (next.equals("endLng")) {
                endLng = reader.nextDouble();
            } else if (next.equals("risks")) {
                risks = readRiskArray(reader);
            } else if (next.equals("isOpen")) {
                state = reader.nextBoolean();
            } else if (next.equals("timestamp")) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                timestamp = format.parse(reader.nextString()).getTime();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        LatLng pointA = new LatLng(startLat, startLng);
        LatLng pointB = new LatLng(endLat, endLng);

        return new Section(id, pointA, pointB, risks, state, timestamp);
    }

    private static Map<Occurrence, Integer> readRiskArray(JsonReader reader)
            throws IOException {
        Occurrence[] occurrences = {Occurrence.EARTHQUAKE, Occurrence.TSUNAMI, Occurrence.FIRE, Occurrence.LANDSLIP};
        Map<Occurrence, Integer> risks = new HashMap<>();

        int index = 0;
        reader.beginArray();
        while (reader.hasNext()) {
            int level = reader.nextInt();
            risks.put(occurrences[index], level);
            index++;
        }
        reader.endArray();
        return risks;
    }

//	public static LatLng readCoordinates(JsonReader reader) throws IOException {
//		double latitude = -1;
//		double longitude = -1;
//
//		reader.beginObject();
//		while (reader.hasNext()) {
//			String next = reader.nextName();
//			if (next.equals("latitude")) {
//				latitude = reader.nextDouble();
//			} else if (next.equals("longitude")) {
//				longitude = reader.nextDouble();
//			} else {
//				reader.skipValue();
//			}
//		}
//		reader.endObject();
//		return new LatLng(latitude, longitude);
//	}

}
