package find.routes;

import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

public class Section {

    private int id;

    private LatLng pointA;
    private LatLng pointB;
    private boolean state;
    private long timestamp;

    private Map<Occurrence, Integer> risks;

    public Section(int id, LatLng pointA, LatLng pointB,
                   Map<Occurrence, Integer> risks, boolean state, long timestamp) {
        super();
        this.id = id;
        this.pointA = pointA;
        this.pointB = pointB;
        this.risks = risks;
        this.state = state;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public LatLng[] getPoints() {
        return new LatLng[]{pointA, pointB};
    }

    public int getRisk(Occurrence occurrence) {
        return risks.get(occurrence);
    }

    public void setRisk(Occurrence occurrence, int level) {
        risks.put(occurrence, level);
    }

    public boolean isOpen() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Section other = (Section) obj;
        if (id != other.id)
            return false;
        return true;
    }


}
