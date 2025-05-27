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
        if (historicalData.length < seasonLength * 2) {
            throw new IllegalArgumentException("Need at least 2 years of data");
        }
        
        // Initialize level, trend, and seasonal components
        double[] seasons = initializeSeasonalComponents(historicalData);
        double level = calculateInitialLevel(historicalData);
        double trend = calculateInitialTrend(historicalData);
        
        // Generate forecast
        double[] forecast = new double[periodsAhead];
        int n = historicalData.length;
        
        for (int i = 0; i < periodsAhead; i++) {
            int season = (n + i) % seasonLength;
            forecast[i] = (level + trend * (i + 1)) * seasons[season];
        }
        
        return forecast;
    }
    
    private double[] initializeSeasonalComponents(double[] data) {
        double[] seasonalIndices = new double[seasonLength];
        int numSeasons = data.length / seasonLength;
        
        // Calculate average for each season
        for (int season = 0; season < seasonLength; season++) {
            double sum = 0;
            for (int year = 0; year < numSeasons; year++) {
                sum += data[year * seasonLength + season];
            }
            seasonalIndices[season] = sum / numSeasons;
        }
        
        // Normalize seasonal indices
        double seasonsSum = Arrays.stream(seasonalIndices).sum();
        for (int i = 0; i < seasonLength; i++) {
            seasonalIndices[i] = seasonalIndices[i] * seasonLength / seasonsSum;
        }
        
        return seasonalIndices;
    }
    
    private double calculateInitialLevel(double[] data) {
        return Arrays.stream(data, 0, seasonLength).average().orElse(0);
    }
    
    private double calculateInitialTrend(double[] data) {
        double sum = 0;
        for (int i = 0; i < seasonLength; i++) {
            sum += (data[seasonLength + i] - data[i]) / seasonLength;
        }
        return sum / seasonLength;
    }
    
    public static double calculateAccuracy(double[] actual, double[] forecast) {
        if (actual.length != forecast.length) {
            throw new IllegalArgumentException("Arrays must be of equal length");
        }
        
        double sumError = 0;
        for (int i = 0; i < actual.length; i++) {
            sumError += Math.abs((actual[i] - forecast[i]) / actual[i]);
        }
        
        return (1 - (sumError / actual.length)) * 100; // Return accuracy percentage
    }
}
