package com.siakad.service;

import com.siakad.model.CourseGrade;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test murni tanpa mock untuk GradeCalculator
 */
class GradeCalculatorTest {

    private final GradeCalculator calculator = new GradeCalculator();

    @Test
    void testCalculateGPA_NormalCase() {
        List<CourseGrade> grades = Arrays.asList(
                new CourseGrade("CS101", 3, 4.0),
                new CourseGrade("MA102", 2, 3.0)
        );
        assertEquals(3.6, calculator.calculateGPA(grades));
    }

    @Test
    void testCalculateGPA_EmptyList() {
        assertEquals(0.0, calculator.calculateGPA(Collections.emptyList()));
    }

    @Test
    void testCalculateGPA_InvalidGrade() {
        List<CourseGrade> grades = List.of(new CourseGrade("CS101", 3, 5.0));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateGPA(grades));
    }

    @Test
    void testDetermineAcademicStatus_Semester1Active() {
        assertEquals("ACTIVE", calculator.determineAcademicStatus(2.5, 1));
        assertEquals("PROBATION", calculator.determineAcademicStatus(1.5, 2));
    }

    @Test
    void testDetermineAcademicStatus_Semester3and4() {
        assertEquals("ACTIVE", calculator.determineAcademicStatus(2.5, 3));
        assertEquals("PROBATION", calculator.determineAcademicStatus(2.1, 3));
        assertEquals("SUSPENDED", calculator.determineAcademicStatus(1.8, 4));
    }

    @Test
    void testDetermineAcademicStatus_Semester5Plus() {
        assertEquals("ACTIVE", calculator.determineAcademicStatus(3.0, 5));
        assertEquals("PROBATION", calculator.determineAcademicStatus(2.1, 5));
        assertEquals("SUSPENDED", calculator.determineAcademicStatus(1.5, 6));
    }

    @Test
    void testDetermineAcademicStatus_InvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> calculator.determineAcademicStatus(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.determineAcademicStatus(3.0, 0));
    }

    @Test
    void testCalculateMaxCredits() {
        assertEquals(24, calculator.calculateMaxCredits(3.5));
        assertEquals(21, calculator.calculateMaxCredits(2.6));
        assertEquals(18, calculator.calculateMaxCredits(2.2));
        assertEquals(15, calculator.calculateMaxCredits(1.9));
    }

    @Test
    void testCalculateMaxCredits_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateMaxCredits(5.0));
    }

    @Test
    void testCalculateGPA_TotalCreditsZero_ReturnsZero() {
        List<CourseGrade> grades = Arrays.asList(
                new CourseGrade("CS101", 0, 4.0),
                new CourseGrade("MA102", 0, 3.0)
        );

        double result = calculator.calculateGPA(grades);

        assertEquals(0.0, result, 0.0001,
                "Jika totalCredits = 0, maka GPA harus 0.0 sesuai guard clause");
    }
}
