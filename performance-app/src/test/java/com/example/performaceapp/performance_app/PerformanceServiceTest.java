package com.example.performaceapp.performance_app;

import com.example.performaceapp.performance_app.model.*;
import com.example.performaceapp.performance_app.repository.CategoryRepository;
import com.example.performaceapp.performance_app.repository.EmployeeRepository;
import com.example.performaceapp.performance_app.service.PerformanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PerformanceServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PerformanceService performanceService;

    // Test saving employees with missing fields
    @Test
    void testSaveEmployeesWithMissingFields() {
        List<Employee> employees = Arrays.asList(
                new Employee(null, "", "A"),
                new Employee(null, "John", null)
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> performanceService.saveEmployees(employees));
        assertEquals("Employee name and category cannot be null or empty.", exception.getMessage());
    }

    // Test updating a non-existent employee
    @Test
    void testUpdateNonExistentEmployee() {
        Employee updatedEmployee = new Employee(null, "John Doe", "B");
        Long nonExistentId = 999L;

        when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        boolean result = performanceService.updateEmployee(nonExistentId, updatedEmployee);

        assertFalse(result);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // Test deleting employees by IDs with partial matches
    @Test
    void testDeleteEmployeesByPartialIds() {
        List<Long> idsToDelete = Arrays.asList(1L, 99L);
        Employee existingEmployee = new Employee(1L, "Harry", "A");

        when(employeeRepository.findAllById(idsToDelete)).thenReturn(Collections.singletonList(existingEmployee));

        assertDoesNotThrow(() -> performanceService.deleteEmployeesByIds(idsToDelete));

        verify(employeeRepository, times(1)).deleteAll(Collections.singletonList(existingEmployee));
    }

    // Test updating category standards that do not sum to 100%
    @Test
    void testUpdateCategoriesInvalidTotal() {
        List<Category> categories = Arrays.asList(
                new Category(null, "A", 40.0),
                new Category(null, "B", 40.0),
                new Category(null, "C", 30.0)
        );

        when(categoryRepository.findAll()).thenReturn(categories);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> performanceService.updateCategoryStandards(categories));
        assertEquals("Total standard percentage of all categories must be 100%. Current total: 110.0", exception.getMessage());

        verify(categoryRepository, never()).saveAll(any());
    }

    // Test fetching a non-existent category by name
    @Test
    void testGetNonExistentCategoryByName() {
        String nonExistentCategory = "NonExistent";

        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> performanceService.getCategoryByName(nonExistentCategory));
        assertEquals("Category not found with name: NonExistent", exception.getMessage());
    }

    // Test calculating bell curve with no employees
    @Test
    void testCalculateBellCurveNoEmployees() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(new Category(null, "A", 100.0)));

        BellCurve bellCurve = performanceService.calculateBellCurve();

        assertTrue(bellCurve.getResults().isEmpty());
    }

    // Test generating suggestions with no deviations
    @Test
    void testGenerateSuggestionsNoDeviations() {
        Category categoryA = new Category(null, "A", 100.0);
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(new Employee(1L, "Harry", "A")));
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(categoryA));

        List<Suggestion> suggestions = performanceService.generateSuggestions();

        assertTrue(suggestions.isEmpty());
    }

    ///////////////////////

    // Test attempting to save duplicate employees
    @Test
    void testSaveDuplicateEmployees() {
        Employee employee1 = new Employee(1L, "John", "A");
        Employee employee2 = new Employee(2L, "John", "A"); // Duplicate

        List<Employee> employees = Arrays.asList(employee1, employee2);

        doThrow(new RuntimeException("Duplicate employees are not allowed"))
                .when(employeeRepository).saveAll(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> performanceService.saveEmployees(employees));
        assertEquals("Duplicate employees are not allowed", exception.getMessage());
    }


    // Test deleting non-existant group of employees
    @Test
    void testDeleteNonExistentEmployeeIds() {
        List<Long> nonExistentIds = Arrays.asList(100L, 101L);

        when(employeeRepository.findAllById(nonExistentIds)).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> performanceService.deleteEmployeesByIds(nonExistentIds));
        assertEquals("No employees found for the given IDs: [100, 101]", exception.getMessage());
    }

    // Test fetching employees when database is empty
    @Test
    void testFetchEmployeesWhenDatabaseIsEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Employee> employees = performanceService.getAllEmployees();

        assertTrue(employees.isEmpty());
        verify(employeeRepository, times(1)).findAll();
    }

    // Test saving categories with null/invalid standard percentages
    @Test
    void testSaveCategoriesWithInvalidStandardPercentage() {
        Category category1 = new Category(null, "A", 110.0); // Exceeds 100%
        Category category2 = new Category(null, "B", null);  // Null value

        List<Category> categories = Arrays.asList(category1, category2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performanceService.saveCategoryStandards(categories));
        assertEquals("Category standards must be valid and percentages must not exceed 100%", exception.getMessage());
    }


    //////////////////////

    // Test updating categories with partial matches
    @Test
    void testUpdateCategoriesWithPartialMatches() {
        Category existingCategory = new Category(1L, "A", 50.0);
        Category newCategory = new Category(null, "X", 30.0);

        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(existingCategory));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> performanceService.updateCategoryStandards(Arrays.asList(existingCategory, newCategory)));
        assertEquals("Category not found: X", exception.getMessage());
    }

    // Test calculating Bell Curve when all employees belong to one category
    @Test
    void testCalculateBellCurveSingleCategory() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "Harry", "A"),
                new Employee(2L, "Ron", "A")
        );

        List<Category> categories = Arrays.asList(
                new Category(null, "A", 100.0),
                new Category(null, "B", 0.0)
        );

        when(employeeRepository.findAll()).thenReturn(employees);
        when(categoryRepository.findAll()).thenReturn(categories);

        BellCurve bellCurve = performanceService.calculateBellCurve();

        assertEquals(2, bellCurve.getResults().size());
        assertEquals(100.0, bellCurve.getResults().get(0).getActualPercentage());
        assertEquals(0.0, bellCurve.getResults().get(1).getActualPercentage());
    }

    // Test handling bell curve without category standards
    @Test
    void testCalculateBellCurveWithoutStandards() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "Harry", "A")
        );

        when(employeeRepository.findAll()).thenReturn(employees);
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        BellCurve bellCurve = performanceService.calculateBellCurve();

        assertTrue(bellCurve.getResults().isEmpty(), "Bell curve should be empty when no category standards are defined.");
    }

    // Test when deviations are below threshold
    @Test
    void testGenerateSuggestionsBelowThreshold() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "Harry", "A"),
                new Employee(2L, "Ron", "B")
        );

        List<Category> categories = Arrays.asList(
                new Category(null, "A", 50.0),
                new Category(null, "B", 50.0)
        );

        when(employeeRepository.findAll()).thenReturn(employees);
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Suggestion> suggestions = performanceService.generateSuggestions();

        assertTrue(suggestions.isEmpty());
    }

    // test when one category cause effects on other categories
    @Test
    void testCascadingAdjustments() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "Harry", "A"),
                new Employee(2L, "Ron", "A"),
                new Employee(3L, "Hermione", "A"),
                new Employee(4L, "Neville", "B"),
                new Employee(5L, "Luna", "C"),
                new Employee(6L, "Draco", "C")
        );

        List<Category> categories = Arrays.asList(
                new Category(null, "A", 50.0),
                new Category(null, "B", 30.0),
                new Category(null, "C", 20.0)
        );

        when(employeeRepository.findAll()).thenReturn(employees);
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Suggestion> suggestions = performanceService.generateSuggestions();

        assertFalse(suggestions.isEmpty());
        assertEquals(2, suggestions.size());  // Cascading adjustments
    }

    // test when employee distribution matches standard percentages exactly
    @Test
    void testNoAdjustmentsNeeded() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "Harry", "A"),
                new Employee(2L, "Ron", "B"),
                new Employee(3L, "Luna", "C")
        );

        List<Category> categories = Arrays.asList(
                new Category(null, "A", 33.3),
                new Category(null, "B", 33.3),
                new Category(null, "C", 33.3)
        );

        when(employeeRepository.findAll()).thenReturn(employees);
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Suggestion> suggestions = performanceService.generateSuggestions();

        assertTrue(suggestions.isEmpty());
    }

    // Test to ensure precision issues in floating point occurs
    @Test
    void testPercentagePrecisionHandling() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "Harry", "A"),
                new Employee(2L, "Ron", "A"),
                new Employee(3L, "Luna", "B")
        );

        List<Category> categories = Arrays.asList(
                new Category(null, "A", 66.6667),
                new Category(null, "B", 33.3333)
        );

        when(employeeRepository.findAll()).thenReturn(employees);
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Suggestion> suggestions = performanceService.generateSuggestions();

        // Adding tolerance for floating-point precision
        assertTrue(suggestions.isEmpty(), "Suggestions should be empty as deviations are within the precision tolerance.");
    }
}
