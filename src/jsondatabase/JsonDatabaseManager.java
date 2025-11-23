package jsondatabase;

import models.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JsonDatabaseManager - simplified, robust loader/saver for users & courses (with quizzes & attempts)
 */
public class JsonDatabaseManager {
    private static final String DATA_FOLDER = "data";
    private static final String USERS_FILE = DATA_FOLDER + "/users.json";
    private static final String COURSES_FILE = DATA_FOLDER + "/courses.json";

    // Ensure data folder and files exist
    private void ensureDataFilesExist() {
        try {
            File folder = new File(DATA_FOLDER);
            if (!folder.exists()) folder.mkdirs();

            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile))) {
                pw.println("[]");
            }

            File coursesFile = new File(COURSES_FILE);
            if (!coursesFile.exists()) try (PrintWriter pw = new PrintWriter(new FileWriter(coursesFile))) {
                pw.println("[]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ----------------- USERS -----------------
    public List<User> loadUsers() {
        ensureDataFilesExist();
        List<User> users = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            JSONArray arr = new JSONArray(content);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String role = obj.optString("role", "STUDENT");
                int userId = obj.optInt("userId", -1);
                String username = obj.optString("username", "");
                String email = obj.optString("email", "");
                String passwordHash = obj.optString("passwordHash", "");

                if ("STUDENT".equalsIgnoreCase(role)) {
                    // enrolled/completed
                    List<String> enrolled = new ArrayList<>();
                    List<String> completed = new ArrayList<>();
                    JSONArray enrolledArr = obj.optJSONArray("enrolledCourseIds");
                    JSONArray completedArr = obj.optJSONArray("completedLessonIds");
                    if (enrolledArr != null) for (int j = 0; j < enrolledArr.length(); j++) enrolled.add(enrolledArr.optString(j));
                    if (completedArr != null) for (int j = 0; j < completedArr.length(); j++) completed.add(completedArr.optString(j));

                    // create student - note: constructor expects hashed password flag as last param in your code
                    Student s = new Student(enrolled, completed, userId, username, email, passwordHash, role, true);

                    // certificates
                    JSONArray certsArr = obj.optJSONArray("certificates");
                    if (certsArr != null) {
                        for (int j = 0; j < certsArr.length(); j++) {
                            JSONObject cObj = certsArr.optJSONObject(j);
                            if (cObj == null) continue;
                            Certificate cert = new Certificate(
                                    cObj.optString("certificateId", UUID.randomUUID().toString()),
                                    cObj.optInt("studentId", s.getUserId()),
                                    cObj.optString("courseId", ""),
                                    cObj.optString("issueDate", "")
                            );
                            s.getCertificates().add(cert);
                        }
                    }

                    // quiz attempts
                    JSONArray attemptsArr = obj.optJSONArray("quizAttempts");
                    if (attemptsArr != null) {
                        for (int j = 0; j < attemptsArr.length(); j++) {
                            JSONObject at = attemptsArr.optJSONObject(j);
                            if (at == null) continue;
                            String lessonId = at.optString("lessonId", "");
                            QuizAttempt qa = new QuizAttempt(
                                    lessonId,
                                    at.optLong("timestamp", System.currentTimeMillis()),
                                    at.optInt("score", 0),
                                    at.optInt("correctCount", 0),
                                    at.optInt("totalQuestions", 0)
                            );
                            s.addQuizAttempt(lessonId, qa);
                        }
                    }

                    users.add(s);
                } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {
                    Instructor ins = new Instructor(userId, username, email, passwordHash, true);
                    users.add(ins);
                } else if ("ADMIN".equalsIgnoreCase(role)) {
                    Admin admin = new Admin(userId, username, email, passwordHash, true);
                    users.add(admin);
                } else {
                    // Unknown role - create generic User? (skip)
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void saveUsers(List<User> users) {
        ensureDataFilesExist();
        JSONArray arr = new JSONArray();

        for (User u : users) {
            JSONObject obj = new JSONObject();
            obj.put("userId", u.getUserId());
            obj.put("username", u.getUsername());
            obj.put("email", u.getEmail());
            obj.put("passwordHash", u.getPasswordHash());
            obj.put("role", u.getRole());

            if (u instanceof Student s) {
                obj.put("enrolledCourseIds", new JSONArray(s.getEnrolledCourseIds()));
                obj.put("completedLessonIds", new JSONArray(s.getCompletedLessonIds()));

                // certificates
                JSONArray certsArr = new JSONArray();
                if (s.getCertificates() != null) {
                    for (Certificate c : s.getCertificates()) {
                        JSONObject co = new JSONObject();
                        co.put("certificateId", c.getCertificateId());
                        co.put("studentId", c.getStudentId());
                        co.put("courseId", c.getCourseId());
                        co.put("issueDate", c.getIssueDate());
                        certsArr.put(co);
                    }
                }
                obj.put("certificates", certsArr);

                // quiz attempts
                JSONArray attemptsArr = new JSONArray();
                if (s.getQuizAttemptsByLesson() != null) {
                    for (Map.Entry<String, List<QuizAttempt>> e : s.getQuizAttemptsByLesson().entrySet()) {
                        String lessonId = e.getKey();
                        for (QuizAttempt a : e.getValue()) {
                            JSONObject at = new JSONObject();
                            at.put("lessonId", lessonId);
                            at.put("timestamp", a.getTimestamp());
                            at.put("score", a.getScore());
                            at.put("correctCount", a.getCorrectCount());
                            at.put("totalQuestions", a.getTotalQuestions());
                            attemptsArr.put(at);
                        }
                    }
                }
                obj.put("quizAttempts", attemptsArr);
            }

            arr.put(obj);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            pw.println(arr.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUserByEmail(String email) {
        for (User u : loadUsers()) if (u.getEmail().equalsIgnoreCase(email)) return u;
        return null;
    }

    public void addUser(User user) {
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    public void updateUser(User updatedUser) {
        List<User> users = loadUsers();
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId() == updatedUser.getUserId()) {
                users.set(i, updatedUser);
                found = true;
                break;
            }
        }
        if (!found) users.add(updatedUser);
        saveUsers(users);
    }

    // ----------------- COURSES -----------------
    public List<Course> loadCourses() {
        ensureDataFilesExist();
        List<Course> courses = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(COURSES_FILE)));
            JSONArray arr = new JSONArray(content);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String courseId = obj.optString("courseId", "");
                String title = obj.optString("title", "");
                String description = obj.optString("description", "");
                String instructorId = obj.optString("instructorId", "");
                String status = obj.optString("status", "PENDING");

                Course c = new Course(courseId, title, description, instructorId, status);

                // students
                JSONArray studentsArr = obj.optJSONArray("students");
                if (studentsArr != null) for (int j = 0; j < studentsArr.length(); j++) c.getStudents().add(studentsArr.optString(j));

                // lessons
                JSONArray lessonsArr = obj.optJSONArray("lessons");
                if (lessonsArr != null) {
                    for (int j = 0; j < lessonsArr.length(); j++) {
                        JSONObject lObj = lessonsArr.optJSONObject(j);
                        if (lObj == null) continue;

                        Lesson l = new Lesson();
                        l.setLessonId(lObj.optString("lessonId", ""));
                        l.setTitle(lObj.optString("title", ""));
                        l.setContent(lObj.optString("content", ""));

                        // quiz
                        if (lObj.has("quiz")) {
                            JSONObject qObj = lObj.optJSONObject("quiz");
                            if (qObj != null) {
                                Quiz quiz = new Quiz();
                                quiz.setPassingPercentage(qObj.optInt("passingPercentage", 60));
                                quiz.setMaxAttempts(qObj.optInt("maxAttempts", 0));

                                List<Question> questions = new ArrayList<>();
                                JSONArray qArr = qObj.optJSONArray("questions");
                                if (qArr != null) {
                                    for (int k = 0; k < qArr.length(); k++) {
                                        JSONObject qItem = qArr.optJSONObject(k);
                                        if (qItem == null) continue;

                                        String qText = qItem.optString("questionText", "");
                                        JSONArray opts = qItem.optJSONArray("options");
                                        List<String> options = new ArrayList<>();
                                        if (opts != null) for (int m = 0; m < opts.length(); m++) options.add(opts.optString(m, ""));

                                        int correctIndex = qItem.optInt("correctOptionIndex", 0);
                                        Question question = new Question(qText, options, correctIndex);
                                        questions.add(question);
                                    }
                                }

                                quiz.setQuestions(questions);
                                l.setQuiz(quiz);
                            }
                        }

                        c.getLessons().add(l);
                    }
                }

                courses.add(c);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public void saveCourses(List<Course> courses) {
        ensureDataFilesExist();
        JSONArray arr = new JSONArray();

        for (Course c : courses) {
            JSONObject obj = new JSONObject();
            obj.put("courseId", c.getCourseId());
            obj.put("title", c.getTitle());
            obj.put("description", c.getDescription());
            obj.put("instructorId", c.getInstructorId());
            obj.put("status", c.getStatus());
            obj.put("students", new JSONArray(c.getStudents()));

            JSONArray lessonsArr = new JSONArray();
            for (Lesson l : c.getLessons()) {
                JSONObject lObj = new JSONObject();
                lObj.put("lessonId", l.getLessonId());
                lObj.put("title", l.getTitle());
                lObj.put("content", l.getContent());

                // quiz
                Quiz quiz = l.getQuiz();
                if (quiz != null) {
                    JSONObject qObj = new JSONObject();
                    qObj.put("passingPercentage", quiz.getPassingPercentage());
                    qObj.put("maxAttempts", quiz.getMaxAttempts());

                    JSONArray qArr = new JSONArray();
                    if (quiz.getQuestions() != null) {
                        for (Question q : quiz.getQuestions()) {
                            JSONObject qItem = new JSONObject();
                            qItem.put("questionText", q.getQuestionText());
                            qItem.put("options", new JSONArray(q.getOptions()));
                            qItem.put("correctOptionIndex", q.getCorrectOptionIndex());
                            qArr.put(qItem);
                        }
                    }
                    qObj.put("questions", qArr);
                    lObj.put("quiz", qObj);
                }

                lessonsArr.put(lObj);
            }
            obj.put("lessons", lessonsArr);

            arr.put(obj);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(COURSES_FILE))) {
            pw.println(arr.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Course getCourseById(String courseId) {
        for (Course c : loadCourses()) {
            if (c.getCourseId() != null && c.getCourseId().equals(courseId)) return c;
        }
        return null;
    }

    public void addCourse(Course course) {
        List<Course> courses = loadCourses();
        courses.add(course);
        saveCourses(courses);
    }

    public void updateCourse(Course updatedCourse) {
        List<Course> courses = loadCourses();
        boolean found = false;
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseId().equals(updatedCourse.getCourseId())) {
                courses.set(i, updatedCourse);
                found = true;
                break;
            }
        }
        if (!found) courses.add(updatedCourse);
        saveCourses(courses);
    }

    // Visible / Approved courses for students
    public List<Course> getVisibleCoursesForStudents() {
        return loadCourses().stream()
                .filter(c -> "APPROVED".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    // Admin actions
    public void approveCourse(String courseId) {
        List<Course> courses = loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                c.setStatus("APPROVED");
                break;
            }
        }
        saveCourses(courses);
    }

    public void rejectCourse(String courseId) {
        List<Course> courses = loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                c.setStatus("REJECTED");
                break;
            }
        }
        saveCourses(courses);
    }

    // Course statistics (lesson-level)
    public Map<String, JSONObject> computeCourseStatistics(String courseId) {
        Map<String, JSONObject> out = new LinkedHashMap<>();
        List<Course> courses = loadCourses();
        Course target = null;
        for (Course c : courses) if (c.getCourseId().equals(courseId)) {
            target = c;
            break;
        }
        if (target == null) return out;

        List<String> lessonIds = target.getLessons().stream().map(Lesson::getLessonId).collect(Collectors.toList());
        List<Student> allStudents = loadUsers().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());

        for (String lid : lessonIds) {
            // gather all attempts scores for this lesson from all students
            List<Integer> scores = new ArrayList<>();
            int completedCount = 0;
            for (Student s : allStudents) {
                List<QuizAttempt> attempts = s.getAttemptsForCourseLesson(null, lid); // implement if needed
                for (QuizAttempt a : attempts) scores.add(a.getScore());
                if (s.hasCompletedLesson(lid)) completedCount++;
            }
            double avg = scores.isEmpty() ? 0.0 : scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            double completionPct = allStudents.isEmpty() ? 0.0 : (completedCount * 100.0 / allStudents.size());
            JSONObject stat = new JSONObject();
            stat.put("lessonId", lid);
            stat.put("averageScore", avg);
            stat.put("completionPercentage", completionPct);
            stat.put("attemptCount", scores.size());
            out.put(lid, stat);
        }
        return out;
    }

    // Record a quiz attempt for a student and persist
    public void recordQuizAttempt(int studentId, String lessonId, QuizAttempt attempt) {
        List<User> users = loadUsers();
        for (User u : users) {
            if (u instanceof Student s && s.getUserId() == studentId) {
                s.addQuizAttempt(lessonId, attempt);
                saveUsers(users);
                return;
            }
        }
        throw new IllegalArgumentException("Student not found: " + studentId);
    }

    // Check if student passed specific lesson
    public boolean hasStudentPassedLesson(int studentId, String lessonId, int passingPercentage) {
        List<User> users = loadUsers();
        for (User u : users) {
            if (u instanceof Student s && s.getUserId() == studentId) {
                return s.hasPassedLesson(lessonId, passingPercentage);
            }
        }
        return false;
    }

    // Is course completed by the student (all lessons completed)
    public boolean isCourseCompleted(Student s, Course c) {
        for (Lesson lesson : c.getLessons()) {
            if (!s.hasCompletedLesson(lesson.getLessonId())) return false;
        }
        return true;
    }

    // Generate certificate and persist to user
    public Certificate generateCertificate(Student s, Course c) {
        String certId = UUID.randomUUID().toString();
        String date = java.time.LocalDate.now().toString();
        Certificate cert = new Certificate(certId, s.getUserId(), c.getCourseId(), date);
        s.addCertificate(cert);
        updateUser(s);
        return cert;
    }
}