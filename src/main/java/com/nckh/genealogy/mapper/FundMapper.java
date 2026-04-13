package com.nckh.genealogy.mapper;

import com.nckh.genealogy.dto.response.FundResponse;
import com.nckh.genealogy.entity.Fund;
import org.springframework.stereotype.Component;

@Component
public class FundMapper {

    public FundResponse toResponse(Fund fund) {
        FundResponse.FundResponseBuilder builder = FundResponse.builder()
                .id(fund.getId())
                .name(fund.getName());

        if (fund.getTree() != null) {
            builder
                    .treeId(fund.getTree().getId())
                    .treeName(fund.getTree().getName());
        }

        if (fund.getTreeEvent() != null) {
            builder
                    .treeEventId(fund.getTreeEvent().getId())
                    .treeEventName(fund.getTreeEvent().getName())
                    .treeEventCreatedAt(fund.getTreeEvent().getCreatedAt());

            if (fund.getTreeEvent().getEvent() != null) {
                builder
                        .eventId(fund.getTreeEvent().getEvent().getId())
                        .eventName(fund.getTreeEvent().getEvent().getName())
                        .eventDescription(fund.getTreeEvent().getEvent().getDescription())
                        .eventStartedAt(fund.getTreeEvent().getEvent().getStartedAt())
                        .eventEndedAt(fund.getTreeEvent().getEvent().getEndedAt())
                        .eventStatus(fund.getTreeEvent().getEvent().getStatus());
            }
        }

        return builder.build();
    }
}