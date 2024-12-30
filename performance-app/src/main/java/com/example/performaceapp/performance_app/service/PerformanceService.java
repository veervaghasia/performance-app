package com.example.performaceapp.performance_app.service;

import com.example.performaceapp.performance_app.model.*;
import com.example.performaceapp.performance_app.repository.CategoryRepository;
import com.example.performaceapp.performance_app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    // to talk to employee data
    @Autowired
    private EmployeeRepository employeeRepository;

    // to talk to category data
    @Autowired
    private CategoryRepository categoryRepository;

    // Example thresholds
    private double positiveThreshold = 3; // +3%
    private double negativeThreshold = -3; // -3%

    // Save employee data
    public void saveEmployees(List<Employee> employees){
        employeeRepository.saveAll(employees);
    }

    // Update employee data
    public boolean updateEmployee(Long id, Employee updatedEmployee) {
        // Using id to fetch existing employee
        Optional<Employee> existingEmployee = employeeRepository.findById(id);

        if (existingEmployee.isPresent()) {
            // Get the employee object
            Employee employee = existingEmployee.get();

            // Check and update the fields only if they are not null or empty
            if (updatedEmployee.getName() != null && !updatedEmployee.getName().isEmpty()) {
                employee.setName(updatedEmployee.getName());
            }
            if (updatedEmployee.getCategory() != null && !updatedEmployee.getCategory().isEmpty()) {
                employee.setCategory(updatedEmployee.getCategory());
            }

            // Save the updated employee to the database
            employeeRepository.save(employee);
            return true;
        }

        return false;
    }

    // Get all employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Get employee by id
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    // Delete an employee by id
    public boolean deleteEmployee(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Delete a group of employees by ids
    public void deleteEmployeesByIds(List<Long> ids) {
        List<Employee> employeesToDelete = employeeRepository.findAllById(ids);
        if (employeesToDelete.isEmpty()) {
            System.out.println("No employees found for the given IDs: " + ids);
        }
        employeeRepository.deleteAll(employeesToDelete);
    }

    // Delete all employees
    public void deleteAllEmployees() {
        employeeRepository.deleteAll();
    }


    // Save category standards
    public void saveCategoryStandards(List<Category> categories){
        categoryRepository.saveAll(categories);
    }

    // Update category standards
    public String updateCategoryStandards(List<Category> updatedCategories) {
        // Fetch all categories from the database
        List<Category> existingCategories = categoryRepository.findAll();

        // Update the standard percentages of matching categories
        for (Category updated : updatedCategories) {
            Category categoryToUpdate = existingCategories.stream()
                    .filter(existing -> existing.getName().equalsIgnoreCase(updated.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Category not found: " + updated.getName()));

            categoryToUpdate.setStandardPercentage(updated.getStandardPercentage());
        }

        // Validate that the total standards sum to 100%
        double totalStandards = existingCategories.stream()
                .mapToDouble(Category::getStandardPercentage)
                .sum();

        if (totalStandards != 100.0) {
            throw new RuntimeException("Total standard percentage of all categories must be 100%. Current total: " + totalStandards);
        }

        // Save updated categories
        categoryRepository.saveAll(existingCategories);
        return "Category standards updated successfully. Total percentage: " + totalStandards + "%";
    }

    // Fetch all category details
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // fetch category details by name of category
    public Category getCategoryByName(String name) {
        List<Category> matchingCategories = categoryRepository.findAll()
                .stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());

        if (matchingCategories.isEmpty()) {
            throw new RuntimeException("Category not found with name: " + name);
        } else if (matchingCategories.size() > 1) {
            throw new RuntimeException("Multiple categories found with name: " + name);
        }
        return matchingCategories.get(0); // Return the first match only
    }


    // Calculate Bell Curve
    public BellCurve calculateBellCurve() {
        // Fetch all the employees
        List<Employee> employees = employeeRepository.findAll();

        // Fetch category standards
        List<Category> categories = categoryRepository.findAll();

        // Calculate actual percentages
        Map<String, Integer> categoryCounts = countEmployeesByCategory(employees);
        BellCurve bellCurve = calculatePercentagesAndDeviation(categories, categoryCounts);

        return bellCurve;
    }

    // Helper method: count employees by category
    private Map<String, Integer> countEmployeesByCategory(List<Employee> employees){
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (Employee employee : employees) {
            categoryCounts.put(employee.getCategory(), categoryCounts.getOrDefault(employee.getCategory(), 0) + 1);
        }
        return categoryCounts;
    }

    // Helper method: calculate percentages and deviations
    private BellCurve calculatePercentagesAndDeviation(List<Category> categories, Map<String, Integer> categoryCounts) {
        BellCurve bellCurve = new BellCurve();

        int totalEmployees = categoryCounts.values().stream().mapToInt(Integer::intValue).sum();

        if(totalEmployees == 0) {
            throw new RuntimeException("Number of total employees is 0");
        }

        for (Category category : categories) {
            int actualCount = categoryCounts.getOrDefault(category.getName(), 0);
            double actualPercentage = (double)(actualCount * 100) / totalEmployees;
            double deviation = actualPercentage - category.getStandardPercentage();

            String suggestion = null;
            if (!isDeviationWithinLimits(deviation, positiveThreshold, negativeThreshold)) {
                suggestion = "Deviation exceeds limits for category '" + category.getName() +
                        "'. Actual: " + actualPercentage + ", Standard: " + category.getStandardPercentage() +
                        ", Deviation: " + deviation + ". Suggest modifying employee ratings.";
            }

            bellCurve.addCategoryResult(new BellCurveResult(
                    category.getName(),
                    category.getStandardPercentage(),
                    actualCount,
                    actualPercentage,
                    deviation,
                    suggestion
            ));
        }

        return bellCurve;
    }

    // Helper method: to validate deviation
    public boolean isDeviationWithinLimits(double deviation, double positiveThreshold, double negativeThreshold) {
        return deviation >= negativeThreshold && deviation <= positiveThreshold;
    }

    // Generate suggestions
    public List<Suggestion> generateSuggestions(){
        BellCurve bellCurve = calculateBellCurve();
        return suggestCategoryAdjustments(bellCurve);
    }

    // Helper method: suggest category adjustments
    private List<Suggestion> suggestCategoryAdjustments(BellCurve bellCurve) {
        List<Suggestion> suggestions = new ArrayList<>();
        List<BellCurveResult> results = new ArrayList<>();

        // Create a copy of the original BellCurveResults to avoid modifying the original objects
        for (BellCurveResult originalResult : bellCurve.getResults()) {
            BellCurveResult resultCopy = new BellCurveResult(
                    originalResult.getName(),
                    originalResult.getStandardPercentage(),
                    originalResult.getActualCount(),
                    originalResult.getActualPercentage(),
                    originalResult.getDeviation(),
                    originalResult.getSuggestion()
            );
            results.add(resultCopy);
        }

        // Create a copy of the map to track the current number of employees in each category
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (BellCurveResult result : results) {
            categoryCounts.put(result.getName(), result.getActualCount());
        }

        int totalEmployees = categoryCounts.values().stream().mapToInt(Integer::intValue).sum();

        // Repeat adjustments until deviations are within acceptable limits
        int maxIterations = 5;
        int currentIterations = 0;

        boolean adjustmentsMade;
        do {
            currentIterations++;
            adjustmentsMade = false;

            for (int i = 0; i < results.size(); i++) {
                BellCurveResult currentCategoryResult = results.get(i);
                String currentCategoryName = currentCategoryResult.getName();
                double deviation = currentCategoryResult.getDeviation();

                // Check if the current category has a deviation beyond the threshold
                if (Math.abs(deviation) > 0) {
                    int employeesToMove = (int) Math.round(Math.abs(deviation) * totalEmployees);

                    // If deviation is positive (overrepresented category)
                    if (deviation > 0 && i < results.size() - 1) {
                        // Move employees from current category to the next (lower) category
                        BellCurveResult nextCategoryResult = results.get(i + 1);
                        int moveToNextCategory = Math.min(employeesToMove, nextCategoryResult.getActualCount());

                        // Adjust the counts after the move
                        categoryCounts.put(currentCategoryName, categoryCounts.get(currentCategoryName) - moveToNextCategory);
                        categoryCounts.put(nextCategoryResult.getName(), categoryCounts.get(nextCategoryResult.getName()) + moveToNextCategory);

                        suggestions.add(new Suggestion(
                                "Move " + moveToNextCategory + " employees from Category " + currentCategoryName +
                                        " to Category " + nextCategoryResult.getName() + "."
                        ));

                        adjustmentsMade = true;
                    }
                    // If deviation is negative (underrepresented category)
                    else if (deviation < 0 && i > 0) {
                        // Move employees from the previous (higher quality) category to the current category
                        BellCurveResult prevCategoryResult = results.get(i - 1);
                        int moveFromPrevCategory = Math.min(employeesToMove, prevCategoryResult.getActualCount());

                        // Adjust the counts after the move
                        categoryCounts.put(currentCategoryName, categoryCounts.get(currentCategoryName) + moveFromPrevCategory);
                        categoryCounts.put(prevCategoryResult.getName(), categoryCounts.get(prevCategoryResult.getName()) - moveFromPrevCategory);

                        suggestions.add(new Suggestion(
                                "Move " + moveFromPrevCategory + " employees from Category " + prevCategoryResult.getName() +
                                        " to Category " + currentCategoryName + "."
                        ));

                        adjustmentsMade = true;
                    }

                    // Recalculate the percentages and deviations dynamically after each adjustment
                    updateCategoryDeviations(results, categoryCounts, totalEmployees);
                }
            }
        } while (adjustmentsMade && currentIterations < maxIterations);  // Keep adjusting until no more changes are needed

        return suggestions;
    }

    // Helper method to recalculate the percentages and deviations for each category after adjustments
    private void updateCategoryDeviations(List<BellCurveResult> results, Map<String, Integer> categoryCounts, int totalEmployees) {
        for (BellCurveResult result : results) {
            String categoryName = result.getName();
            int actualCount = categoryCounts.getOrDefault(categoryName, 0);
            double actualPercentage = (double) (actualCount * 100) / totalEmployees;
            double deviation = (actualPercentage - result.getStandardPercentage());

            // Update the result with the new percentage and deviation
            result.setActualCount(actualCount);
            result.setActualPercentage(actualPercentage);
            result.setDeviation(deviation);
        }
    }

}
