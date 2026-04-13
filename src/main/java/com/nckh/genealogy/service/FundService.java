package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.response.FundResponse;
import com.nckh.genealogy.entity.Event;
import com.nckh.genealogy.entity.Fund;
import com.nckh.genealogy.entity.Tree;
import com.nckh.genealogy.entity.TreeEvent;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.mapper.FundMapper;
import com.nckh.genealogy.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FundService {

    FundRepository fundRepository;
    TreeRepository treeRepository;
    TreeMemberRepository treeMemberRepository;
    TreeEventRepository treeEventRepository;
    EventRepository eventRepository;
    FundMapper fundMapper;

    @Transactional(readOnly = true)
    public List<FundResponse> findAllByTreeId(UUID treeId, UUID userId) {
        checkMemberTree(userId, treeId);
        return fundRepository.findByTreeId(treeId)
                .stream()
                .map(fundMapper::toResponse)
                .toList();
    }

    @Transactional
    public FundResponse createFund(UUID treeId, UUID userId, UUID eventId) {
        checkMemberTree(userId, treeId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        TreeEvent treeEvent = treeEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));

        if (fundRepository.existsByName(event.getName()))
            throw new AppException(ErrorCode.FUND_ALREADY_EXISTS);

        Fund fund = new Fund();
        fund.setName(event.getName());
        fund.setTreeEvent(treeEvent);
        fund.setTree(tree);

        return fundMapper.toResponse(fundRepository.save(fund));
    }

    public void deleteFund(UUID fundId, UUID treeId, UUID userId) {
        checkMemberTree(userId, treeId);
        fundRepository.deleteById(fundId);
    }

    private void checkMemberTree(UUID userId, UUID treeId) {
        if (!treeRepository.existsById(treeId))
            throw new AppException(ErrorCode.TREE_NOT_FOUND);

        if (!treeMemberRepository.existsByUserIdAndTreeIdAndStatusIsActive(userId, treeId))
            throw new AppException(ErrorCode.TREE_ACCESS_DENIED);
    }
}