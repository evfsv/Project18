package com.example.demo.collabdoc.repository;

import com.example.demo.collabdoc.entity.Comment;
import com.example.demo.collabdoc.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocument(Document document);
    List<Comment> findByDocumentOrderByCreatedAtDesc(Document document);
    List<Comment> findByParent(Comment parent);
}
