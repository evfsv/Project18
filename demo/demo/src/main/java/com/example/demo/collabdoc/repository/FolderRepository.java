package com.example.demo.collabdoc.repository;

import com.example.demo.collabdoc.entity.Folder;
import com.example.demo.collabdoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByOwner(User owner);
    List<Folder> findByParent(Folder parent);
    List<Folder> findByOwnerAndParentIsNull(User owner);
}
