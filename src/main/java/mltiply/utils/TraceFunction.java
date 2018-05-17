package mltiply.utils;


import java.io.*;
import java.util.ArrayList;

public class TraceFunction implements Function {

    public String logFileName;
    private File logFile;
    private FileReader logFileReader;
    private BufferedReader buffLogFileReader;
    private ArrayList<Double> losses;

    public TraceFunction(String logFileName) {

        this.losses = new ArrayList<Double>();

        this.logFileName = logFileName;
        this.logFile = new File(this.logFileName);
        try {
            this.logFileReader = new FileReader(this.logFile);
            this.buffLogFileReader = new BufferedReader(this.logFileReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public double getValue(int iteration) {
        String[] line;
        int num = this.losses.size();
        while (num < iteration) {
            try {
                line = this.buffLogFileReader.readLine().split("\\s+");
                this.losses.add(Double.parseDouble(line[1]));
                num += 1;
                assert(num == Integer.parseInt(line[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.losses.get(iteration - 1);
    }

    public double getSlope(int iteration) {
        return getValue(iteration) - getValue(iteration + 1);
    }
}

