package com.example.performaceapp.performance_app.model;

/*
BellCurve encapsulates results of all categories
 */

import java.util.ArrayList;
import java.util.List;

public class BellCurve {

    private List<BellCurveResult> results = new ArrayList<>();

    // Add a new result to the list
    public void addCategoryResult(BellCurveResult result) {
        results.add(result);
    }

    // Get all results
    public List<BellCurveResult> getResults() {
        return results;
    }
}
