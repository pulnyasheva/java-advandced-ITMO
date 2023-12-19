package info.kgeorgiy.ja.pulnikova.student;

import info.kgeorgiy.java.advanced.student.StudentQuery;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.GroupName;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {

    private static final Comparator<Student> STUDENT_COMPARATOR =
            Comparator.comparing(Student::getLastName, Comparator.reverseOrder())
            .thenComparing(Student::getFirstName, Comparator.reverseOrder())
            .thenComparing(Student::compareTo);
    private static <T> List<T> getParameter(List<Student> students, Function<Student, T> function){
        return students.stream()
                .map(function)
                .collect(Collectors.toList());
    }

    private static <T> List<Student> findStudentParameter(Collection<Student> students,
                                                          T parameter,
                                                          Function<Student, T> function){
        return students.stream()
                .filter(student -> function.apply(student).equals(parameter))
                .sorted(STUDENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    private static List<Student> sortByParameter(Collection<Student> students,
                                                 Comparator<Student> comparator){
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getParameter(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getParameter(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getParameter(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getParameter(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getFirstNames(students).stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortByParameter(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortByParameter(students, STUDENT_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentParameter(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentParameter(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentParameter(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName ,
                        (oldValue, newValue) -> oldValue.compareTo(newValue) < 0 ? oldValue : newValue));
    }
}
