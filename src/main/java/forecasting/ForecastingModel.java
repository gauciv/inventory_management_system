package forecasting;

import java.util.ArrayList;
import java.util.List;

public class ForecastingModel {

    private double alpha; // Smoothing factor for level (0 < alpha < 1)
    private double beta;  // Smoothing factor for trend (0 < beta < 1)
    private double gamma; // Smoothing factor for seasonality (0 < gamma < 1)

    public ForecastingModel(double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    /**
     * Calculates Simple Average Forecast.
     */
    public List<Double> calculateSimpleAverage(double[] historicalData, int monthsToForecast) {
        List<Double> forecast = new ArrayList<>();
        if (historicalData == null || historicalData.length == 0) return forecast;

        double sum = 0;
        for (double val : historicalData) {
            sum += val;
        }
        double average = sum / historicalData.length;

        for (int i = 0; i < monthsToForecast; i++) {
            forecast.add(average);
        }
        return forecast;
    }

    /**
     * Calculates Moving Average Forecast (Window = 3).
     */
    public List<Double> calculateMovingAverage(double[] historicalData, int monthsToForecast) {
        List<Double> forecast = new ArrayList<>();
        if (historicalData == null || historicalData.length == 0) return forecast;

        int windowSize = 3;
        if (historicalData.length < windowSize) windowSize = historicalData.length;

        double sum = 0;
        for (int i = historicalData.length - windowSize; i < historicalData.length; i++) {
            sum += historicalData[i];
        }
        double movingAvg = sum / windowSize;

        for (int i = 0; i < monthsToForecast; i++) {
            forecast.add(movingAvg);
        }
        return forecast;
    }

    /**
     * Calculates Linear Regression Forecast.
     */
    public List<Double> calculateLinearRegression(double[] historicalData, int monthsToForecast) {
        List<Double> forecast = new ArrayList<>();
        if (historicalData == null || historicalData.length < 2) return forecast;

        int n = historicalData.length;
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += historicalData[i];
            sumXY += i * historicalData[i];
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        for (int i = 1; i <= monthsToForecast; i++) {
            double nextX = (n - 1) + i;
            double prediction = slope * nextX + intercept;
            forecast.add(Math.max(0, prediction));
        }
        return forecast;
    }

    /**
     * Calculates Holt-Winters (Triple Exponential) or fallback to Holt's Linear.
     */
    public List<Double> calculateHoltWinters(double[] historicalData, int monthsToForecast) {
        if (historicalData == null || historicalData.length == 0) return new ArrayList<>();
        // Fallback to Holt's Linear as full Seasonality requires 24+ months of data
        return calculateHoltsLinear(historicalData, monthsToForecast);
    }

    private List<Double> calculateHoltsLinear(double[] data, int forecastPeriod) {
        List<Double> result = new ArrayList<>();
        int n = data.length;
        
        double level = data[0];
        double trend = data[1] - data[0];

        for (int i = 1; i < n; i++) {
            double lastLevel = level;
            double lastTrend = trend;

            level = alpha * data[i] + (1 - alpha) * (lastLevel + lastTrend);
            trend = beta * (level - lastLevel) + (1 - beta) * lastTrend;
        }

        for (int m = 1; m <= forecastPeriod; m++) {
            double forecast = level + m * trend;
            result.add(Math.max(0, forecast));
        }

        return result;
    }
}