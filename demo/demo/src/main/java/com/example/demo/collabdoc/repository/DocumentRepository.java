package com.example.demo.collabdoc.repository;

import com.example.demo.collabdoc.entity.Document;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwner(User owner);
    List<Document> findByFolder(Folder folder);
    List<Document> findByIsTemplate(Boolean isTemplate);
    
    @Query(value = "SELECT * FROM documents WHERE owner_id = :userId AND (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%')", nativeQuery = true)
    List<Document> searchByKeywordAndOwner(@Param("keyword") String keyword, @Param("userId") Long userId);
    
    @Query(value = "SELECT * FROM documents WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%'", nativeQuery = true)
    List<Document> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT d FROM Document d WHERE d.owner.id = :ownerId ORDER BY d.updatedAt DESC")
    List<Document> findByOwnerIdOrderByUpdatedAtDesc(@Param("ownerId") Long ownerId);
    
    @Query("SELECT DISTINCT d FROM Document d " +
           "JOIN DocumentPermission dp ON dp.document.id = d.id " +
           "WHERE dp.user.id = :userId AND d.owner.id <> :userId " +
           "ORDER BY d.updatedAt DESC")
    List<Document> findSharedDocuments(@Param("userId") Long userId);
}
