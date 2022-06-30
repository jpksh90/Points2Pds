package de.unipotsdam.se.papds.pds;

public class Location {

    private String loc;

    public Location(String loc) {
        this.loc = loc;
    }

    public String getLoc() {
        return loc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return loc != null ? loc.equals(location.loc) : location.loc == null;
    }

    @Override
    public int hashCode() {
        return loc != null ? loc.hashCode() : 0;
    }

    @Override
    public String toString() {
        return loc;
    }

}
