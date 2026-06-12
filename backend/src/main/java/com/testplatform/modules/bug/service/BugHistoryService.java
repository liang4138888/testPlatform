package com.testplatform.modules.bug.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.testplatform.modules.bug.entity.BugHistory;
import com.testplatform.modules.bug.mapper.BugHistoryMapper;

@Service
public class BugHistoryService {

    private final BugHistoryMapper bugHistoryMapper;

    public BugHistoryService(BugHistoryMapper bugHistoryMapper) {
        this.bugHistoryMapper = bugHistoryMapper;
    }

    @Transactional
    public void record(Long bugId, String actionType, String fieldName, String oldValue, String newValue, Long operatorId) {
        BugHistory history = new BugHistory();
        history.setBugId(bugId);
        history.setActionType(actionType);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setOperatorId(operatorId);
        bugHistoryMapper.insert(history);
    }
}
