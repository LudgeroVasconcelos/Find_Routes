package find.persistence.remote;

import java.util.List;

import find.routes.Route;
import find.routes.Section;

public class ServerResponse {

    private List<Section> sections;
    private List<Route> routes;

    public ServerResponse(List<Section> sections, List<Route> routes) {
        super();
        this.sections = sections;
        this.routes = routes;
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<Route> getRoutes() {
        return routes;
    }
}
