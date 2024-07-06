package openblocks.lib.utils;

public class Units {
    public enum SpeedUnit {
        M_PER_TICK("m/tick", 1, "%.2f %s"),
        M_PER_S("m/s", 20, "%.2f %s"), // probably not working
        KM_PER_H("km/h", 72, "%.2f %s"),
        BROKEN("mph", 44.7388, "%.2f %s");

        public final String name;
        public final double factor;
        public final String format;

        SpeedUnit(String name, double factor, String format) {
            this.name = name;
            this.factor = factor;
            this.format = format;
        }

        public String format(double value) {
            return String.format(format, value * factor, name);
        }
    }

    public enum DistanceUnit {
        M("m", 1, "%.0f %s"),
        KM("km", 0.001, "%.2f %s"),
        SILLY("miles", 0.000621371, "%.f %s");

        public final String name;
        public final double factor;
        public final String format;

        DistanceUnit(String name, double factor, String format) {
            this.name = name;
            this.factor = factor;
            this.format = format;
        }

        public String format(double value) {
            return String.format(format, value * factor, name);
        }
    }
}
