package com.example.demo.collabdoc.repository;

import com.example.demo.collabdoc.entity.DocumentPermission;
import com.example.demo.collabdoc.entity.Document;
import com.example.demo.collabdoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, Long> {
    List<DocumentPermission> findByDocument(Document document);
    List<DocumentPermission> findByUser(User user);
    Optional<DocumentPermission> findByDocumentAndUser(Document document, User user);
}
