package com.siakad.service;

import com.siakad.exception.*;
import com.siakad.model.Course;
import com.siakad.model.Enrollment;
import com.siakad.model.Student;
import com.siakad.repository.CourseRepository;
import com.siakad.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test dengan MOCK dan STUB untuk EnrollmentService.
 * GradeCalculator real instance (bukan mock) agar aman di Java 25.
 */
class EnrollmentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private NotificationService notificationService;

    private GradeCalculator gradeCalculator;
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gradeCalculator = new GradeCalculator(); // pakai real class
        enrollmentService = new EnrollmentService(
                studentRepository, courseRepository, notificationService, gradeCalculator);

        student = new Student("S1", "Belva", "belva@mail.com",
                "Informatika", 3, 3.2, "ACTIVE");
        course = new Course("CS101", "Algoritma", 3, 30, 10, "Dosen A");
    }

    @Test
    void testEnrollCourse_Success() {
        when(studentRepository.findById("S1")).thenReturn(student);
        when(courseRepository.findByCourseCode("CS101")).thenReturn(course);
        when(courseRepository.isPrerequisiteMet("S1", "CS101")).thenReturn(true);

        Enrollment result = enrollmentService.enrollCourse("S1", "CS101");

        assertNotNull(result);
        assertEquals("S1", result.getStudentId());
        assertEquals("CS101", result.getCourseCode());
        verify(notificationService).sendEmail(
                eq("belva@mail.com"),
                eq("Enrollment Confirmation"),
                contains("Algoritma"));
    }

    @Test
    void testEnrollCourse_StudentNotFound() {
        when(studentRepository.findById("S2")).thenReturn(null);
        assertThrows(StudentNotFoundException.class,
                () -> enrollmentService.enrollCourse("S2", "CS101"));
    }

    @Test
    void testEnrollCourse_SuspendedStudent() {
        student.setAcademicStatus("SUSPENDED");
        when(studentRepository.findById("S1")).thenReturn(student);
        assertThrows(EnrollmentException.class,
                () -> enrollmentService.enrollCourse("S1", "CS101"));
    }

    @Test
    void testEnrollCourse_CourseNotFound() {
        when(studentRepository.findById("S1")).thenReturn(student);
        when(courseRepository.findByCourseCode("CS999")).thenReturn(null);
        assertThrows(CourseNotFoundException.class,
                () -> enrollmentService.enrollCourse("S1", "CS999"));
    }

    @Test
    void testEnrollCourse_CourseFull() {
        course.setEnrolledCount(30);
        when(studentRepository.findById("S1")).thenReturn(student);
        when(courseRepository.findByCourseCode("CS101")).thenReturn(course);
        assertThrows(CourseFullException.class,
                () -> enrollmentService.enrollCourse("S1", "CS101"));
    }

    @Test
    void testEnrollCourse_PrerequisiteNotMet() {
        when(studentRepository.findById("S1")).thenReturn(student);
        when(courseRepository.findByCourseCode("CS101")).thenReturn(course);
        when(courseRepository.isPrerequisiteMet("S1", "CS101")).thenReturn(false);
        assertThrows(PrerequisiteNotMetException.class,
                () -> enrollmentService.enrollCourse("S1", "CS101"));
    }

    @Test
    void testValidateCreditLimit_WithinLimit() {
        when(studentRepository.findById("S1")).thenReturn(student);
        boolean valid = enrollmentService.validateCreditLimit("S1", 20);
        assertTrue(valid);
    }

    @Test
    void testValidateCreditLimit_ExceedLimit() {
        when(studentRepository.findById("S1")).thenReturn(student);
        boolean valid = enrollmentService.validateCreditLimit("S1", 30);
        assertFalse(valid);
    }

    @Test
    void testValidateCreditLimit_StudentNotFound() {
        when(studentRepository.findById("X")).thenReturn(null);
        assertThrows(StudentNotFoundException.class,
                () -> enrollmentService.validateCreditLimit("X", 10));
    }

    @Test
    void testDropCourse_Success() {
        when(studentRepository.findById("S1")).thenReturn(student);
        when(courseRepository.findByCourseCode("CS101")).thenReturn(course);

        enrollmentService.dropCourse("S1", "CS101");

        verify(courseRepository).update(course);
        verify(notificationService).sendEmail(
                eq("belva@mail.com"),
                eq("Course Drop Confirmation"),
                contains("Algoritma"));
    }

    @Test
    void testDropCourse_StudentNotFound() {
        when(studentRepository.findById("X")).thenReturn(null);
        assertThrows(StudentNotFoundException.class,
                () -> enrollmentService.dropCourse("X", "CS101"));
    }

    @Test
    void testDropCourse_CourseNotFound() {
        when(studentRepository.findById("S1")).thenReturn(student);
        when(courseRepository.findByCourseCode("CS404")).thenReturn(null);
        assertThrows(CourseNotFoundException.class,
                () -> enrollmentService.dropCourse("S1", "CS404"));
    }
}
