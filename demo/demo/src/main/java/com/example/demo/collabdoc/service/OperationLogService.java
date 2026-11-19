package com.example.demo.collabdoc.service;

import com.example.demo.collabdoc.entity.OperationLog;
import com.example.demo.collabdoc.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Transactional
    public void log(Long userId, String username, String operation, String target, 
                   Long targetId, String detail, String ipAddress) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setTarget(target);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setIpAddress(ipAddress);

        operationLogRepository.save(log);
    }

    public List<OperationLog> getUserLogs(Long userId) {
        return operationLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<OperationLog> getOperationLogs(String operation) {
        return operationLogRepository.findByOperationOrderByCreatedAtDesc(operation);
    }

    public List<OperationLog> getAllLogs() {
        return operationLogRepository.findAll();
    }
}
