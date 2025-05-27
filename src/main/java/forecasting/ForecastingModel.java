package forecasting;

import java.util.Arrays;

public class ForecastingModel {
    private final double alpha; // Level smoothing factor
    private final double beta;  // Trend smoothing factor
    private final double gamma; // Seasonal smoothing factor
    private final int seasonLength; // Number of months in a season (12 for yearly)
    
    public ForecastingModel(double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.seasonLength = 12; // Monthly data
    }
    
    public double[] forecast(double[] historicalData, int periodsAhead) {
        if (historicalData.length < seasonLength) {
            throw new IllegalArgumentException("Need at least 1 year of data");
        }
        
        // Initialize seasonal components using the available data
        double[] seasons = initializeSeasonalComponents(historicalData);
        double level = calculateInitialLevel(historicalData);
        double trend = calculateInitialTrend(historicalData);
        
        // Generate forecast
        double[] forecast = new double[periodsAhead];
        int n = historicalData.length;
        
        for (int i = 0; i < periodsAhead; i++) {
            int season = (n + i) % seasonLength;
            forecast[i] = Math.max(0, (level + trend * (i + 1)) * seasons[season]); // Ensure non-negative values
        }
        
        return forecast;
    }
    
    private double[] initializeSeasonalComponents(double[] data) {
        double[] seasonalIndices = new double[seasonLength];
        int numPeriods = data.length;
        
        // Calculate average for each season using available data
        double[] seasonSums = new double[seasonLength];
        int[] seasonCounts = new int[seasonLength];
        
        for (int i = 0; i < numPeriods; i++) {
            int season = i % seasonLength;
            seasonSums[season] += data[i];
            seasonCounts[season]++;
        }
        
        // Calculate seasonal indices
        double totalAverage = Arrays.stream(data).sum() / numPeriods;
        if (totalAverage == 0) totalAverage = 1; // Avoid division by zero
        
        for (int i = 0; i < seasonLength; i++) {
            double seasonAverage = seasonCounts[i] > 0 ? seasonSums[i] / seasonCounts[i] : totalAverage;
            seasonalIndices[i] = seasonAverage / totalAverage;
        }
        
        return seasonalIndices;
    }
    
    private double calculateInitialLevel(double[] data) {
        // Use the average of first season as initial level
        return Arrays.stream(data, 0, Math.min(data.length, seasonLength)).average().orElse(0);
    }
    
    private double calculateInitialTrend(double[] data) {
        if (data.length <= seasonLength) {
            // With only one season, estimate trend using first and last months
            return (data[data.length - 1] - data[0]) / (data.length - 1);
        }
        
        // With more data, use average change between seasons
        double sum = 0;
        int n = Math.min(data.length - seasonLength, seasonLength);
        for (int i = 0; i < n; i++) {
            sum += (data[seasonLength + i] - data[i]) / seasonLength;
        }
        return sum / n;
    }
    
    public static double calculateAccuracy(double[] actual, double[] forecast) {
        if (actual.length != forecast.length || actual.length == 0) {
            throw new IllegalArgumentException("Arrays must be of equal non-zero length");
        }
        
        double sumAbsPercentError = 0.0;
        int validPoints = 0;
        
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != 0) {  // Avoid division by zero
                double absPercentError = Math.abs((actual[i] - forecast[i]) / actual[i]) * 100;
                sumAbsPercentError += absPercentError;
                validPoints++;
            }
        }
        
        if (validPoints == 0) {
            return 0.0;
        }
        
        // Return accuracy as 100 - MAPE (Mean Absolute Percentage Error)
        return Math.min(100.0, Math.max(0.0, 100.0 - (sumAbsPercentError / validPoints)));
    }
}
