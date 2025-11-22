
package models;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author engom
 */
public class Student extends User{
    private List<String> enrolledCourseIds;
    private List<String> completedLessonIds;
    

    public Student(List<String> enrolledCourseIds, List<String> completedLessonIds,
               int userId, String username, String email,
               String passwordHash, String role, boolean isAlreadyHashed) {

    super(userId, username, email, passwordHash, role, isAlreadyHashed);

    this.enrolledCourseIds = new ArrayList<>();
    this.enrolledCourseIds.addAll(enrolledCourseIds);

    this.completedLessonIds = new ArrayList<>();
    this.completedLessonIds.addAll(completedLessonIds);
}

    public List<String> getEnrolledCourdseIds() {
        return enrolledCourseIds;
    }
    public void enrollInCourse(String CourseId){
        if(CourseId == null || CourseId.trim().isEmpty())
            return;
        if(!enrolledCourseIds.contains(CourseId))
            enrolledCourseIds.add(CourseId);
    }

    public List<String> getCompletedLesssonIds() {
        return completedLessonIds;
    }
    public boolean isEnrolledinCourse(String CourseId){
        return enrolledCourseIds.contains(CourseId);
    }
    public boolean hasCompletedLesson(String LessonId){
        return completedLessonIds.contains(LessonId);
    }
    public void markLessonCompleted(String lessonId){
        if(lessonId==null || lessonId.trim().isEmpty())
            return;
        if(!completedLessonIds.contains(lessonId))
            completedLessonIds.add(lessonId);
    }
    
    
}
