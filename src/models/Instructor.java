
package models;

import java.util.ArrayList;
import java.util.List;

public class Instructor extends User {
    private List<Integer> createdCourses;

    public Instructor(int userId, String username, String email, String rawPassword, boolean isAlreadyHashed) {
        super(userId, username, email, rawPassword, ROLE_INSTRUCTOR, isAlreadyHashed);
        this.createdCourses = new ArrayList<>();
    }

    public Instructor(int userId, String username, String email, String rawPassword) {
        super(userId, username, email, rawPassword, ROLE_INSTRUCTOR);
        this.createdCourses = new ArrayList<>();
    }

    public void addCreatedCourse(int courseId) {
        if (courseId <= 0) throw new IllegalArgumentException("Course ID must be positive.");
        createdCourses.add(courseId);
    }

    public void removeCreatedCourse(int courseId) {
        createdCourses.remove(Integer.valueOf(courseId));
    }

    public List<Integer> getCreatedCourses() { return createdCourses; }
}
