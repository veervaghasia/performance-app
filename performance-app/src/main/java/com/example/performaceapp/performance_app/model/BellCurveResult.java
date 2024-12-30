package com.example.performaceapp.performance_app.model;

/*
BellCurveResult represents details of one category
- Name of category
- Standard percentage
- Actual percentage
- Deviation
 */

public class BellCurveResult {

    private String name; // Category: A, B, C, etc.
    private double standardPercentage;
    private int actualCount;
    private double actualPercentage;
    private double deviation;
    private String suggestion;

    // Constructor
    public BellCurveResult(String name, double standardPercentage, int actualCount, double actualPercentage, double deviation, String suggestion) {
        this.name = name;
        this.standardPercentage = standardPercentage;
        this.actualCount = actualCount;
        this.actualPercentage = actualPercentage;
        this.deviation = deviation;
        this.suggestion = suggestion;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getStandardPercentage() {
        return standardPercentage;
    }

    public void setStandardPercentage(double standardPercentage) {
        this.standardPercentage = standardPercentage;
    }

    public int getActualCount() {
        return actualCount;
    }

    public void setActualCount (int actualCount) {
        this.actualCount = actualCount;
    }

    public double getActualPercentage() {
        return actualPercentage;
    }

    public void setActualPercentage(double actualPercentage) {
        this.actualPercentage = actualPercentage;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
