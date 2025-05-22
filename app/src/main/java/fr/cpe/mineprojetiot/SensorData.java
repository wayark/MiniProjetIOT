package fr.cpe.mineprojetiot;

public class SensorData {
    public String id;
    public String temperature;
    public String humidity;
    public String luminosity;
    public String pressure;

    public static SensorData fromString(String data) {
        SensorData sd = new SensorData();
        String[] parts = data.split(";");
        for (String part : parts) {
            if (part.startsWith("T:")) sd.temperature = part.substring(2);
            else if (part.startsWith("H:")) sd.humidity = part.substring(2);
            else if (part.startsWith("L:")) sd.luminosity = part.substring(2);
            else if (part.startsWith("P:")) sd.pressure = part.substring(2);
            else if (part.startsWith("ID:")) sd.id = part.substring(3);
        }
        return sd;
    }
}
