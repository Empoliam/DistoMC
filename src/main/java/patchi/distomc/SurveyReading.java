package patchi.distomc;

public class SurveyReading {

    private enum readingType {
        LEG,
        SPLAY
    }

    private int start;
    private int end;
    private double heading;
    private double inclination;
    private double distance;


    public SurveyReading(double heading, double inclination, double distance) {

        this.heading = heading;
        this.inclination = inclination;
        this.distance = distance;

    }

    public String printShortString() {

        return "Compass: " + String.format("%.02f", heading) + ", Clino: " + String.format("%.02f", inclination) + ", Distance: " + String.format("%.02f", distance);

    }

}
