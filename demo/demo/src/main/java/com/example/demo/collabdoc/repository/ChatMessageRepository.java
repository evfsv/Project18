package com.example.demo.collabdoc.repository;

import com.example.demo.collabdoc.entity.ChatMessage;
import com.example.demo.collabdoc.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByDocumentOrderByCreatedAtAsc(Document document);
    List<ChatMessage> findByDocumentIsNullOrderByCreatedAtAsc();
}
