package com.example.demo.collabdoc.repository;

import com.example.demo.collabdoc.entity.Task;
import com.example.demo.collabdoc.entity.Document;
import com.example.demo.collabdoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByDocument(Document document);
    List<Task> findByAssignedTo(User user);
    List<Task> findByAssignedToAndStatus(User user, Task.TaskStatus status);
    List<Task> findByDocumentOrderByCreatedAtDesc(Document document);
}
