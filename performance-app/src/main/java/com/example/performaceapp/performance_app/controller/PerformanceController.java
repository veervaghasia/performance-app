package com.example.performaceapp.performance_app.controller;

import com.example.performaceapp.performance_app.model.BellCurve;
import com.example.performaceapp.performance_app.model.Category;
import com.example.performaceapp.performance_app.model.Employee;
import com.example.performaceapp.performance_app.model.Suggestion;
import com.example.performaceapp.performance_app.service.PerformanceService;
import jdk.jshell.SourceCodeAnalysis;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    // Test API
    @GetMapping("/hello")
    public String sayHello(){
        return "Hello, Spring Boot :)";
    }

    // Upload employees details
    @PostMapping("/upload-employees")
    public ResponseEntity<String> uploadEmployees(@RequestBody List<Employee> employees) {
        // Call service layer to save employees
        performanceService.saveEmployees(employees);
        return ResponseEntity.ok("Employees uploaded successfully");
    }

    // Get employee details
    @GetMapping("employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = performanceService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        Optional<Employee> employee = performanceService.getEmployeeById(id);

        if (employee.isPresent()) {
            return ResponseEntity.ok(employee.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or a meaningful error message
        }
    }

    // Update employees details based on ID
    @PutMapping("/employees/{id}")
    public ResponseEntity<String> updateEmployee(@PathVariable Long id, @RequestBody Employee updatedEmployee) {
        boolean success = performanceService.updateEmployee(id, updatedEmployee);
        return success ? ResponseEntity.ok("Employee updated successfully") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found with ID: " + id);
    }

    // Delete employee details based on ID
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        boolean success = performanceService.deleteEmployee(id);
        return success ? ResponseEntity.ok("Employee deleted successfully!") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found with ID: " + id);
    }

    // Delete employee details based on group of ID
    @DeleteMapping("/employees")
    public ResponseEntity<String> deleteEmployees(@RequestParam List<Long> ids) {
        performanceService.deleteEmployeesByIds(ids);
        return ResponseEntity.ok("Employees with IDs " + ids + " have been deleted successfully!");
    }

    // Delete all employees
    @DeleteMapping("/employees/all")
    public ResponseEntity<String> deleteAllEmployees() {
        performanceService.deleteAllEmployees();
        return ResponseEntity.ok("All employees have been deleted successfully!");
    }


    // Upload category standards
    @PostMapping("/upload-category-standard")
    public ResponseEntity<String> uploadCategoryStandard(@RequestBody List<Category> categories) {
        // Call service layer to save category standards
        performanceService.saveCategoryStandards(categories);
        return ResponseEntity.ok("Category standards uploaded successfully");
    }

    // Get category details
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = performanceService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        Category category = performanceService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }

    // Update category standards
    @PutMapping("/categories/update")
    public ResponseEntity<String> updateCategoryStandards(@RequestBody List<Category> updatedCategories) {
        String result = performanceService.updateCategoryStandards(updatedCategories);
        return ResponseEntity.ok(result);
    }

    // Calculate the bell curve
    @GetMapping("/calculate-curve")
    public ResponseEntity<BellCurve> calculateCurve() {
        // Call service layer to calculate curve
        BellCurve bellCurve = performanceService.calculateBellCurve();
        return ResponseEntity.ok(bellCurve);
    }

    // Suggest Changes for ratings
    @GetMapping("/suggest-changes")
    public ResponseEntity<List<Suggestion>> suggestChanges() {
        // Call service layer to generate suggestions
        List<Suggestion> suggestions = performanceService.generateSuggestions();
        return ResponseEntity.ok(suggestions);
    }
}
