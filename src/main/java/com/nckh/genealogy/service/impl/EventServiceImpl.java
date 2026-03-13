package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.event.AddPersonToEventRequest;
import com.nckh.genealogy.dto.request.event.AddTreeEventRequest;
import com.nckh.genealogy.dto.request.event.CreateEventRequest;
import com.nckh.genealogy.dto.response.address.AddressResponse;
import com.nckh.genealogy.dto.response.event.EventResponse;
import com.nckh.genealogy.entity.*;
import com.nckh.genealogy.enums.TreeMemberStatus;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.mapper.PersonMapper;
import com.nckh.genealogy.repository.*;
import com.nckh.genealogy.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final RoleInEventRepository roleInEventRepository;
    private final PersonEventRepository personEventRepository;
    private final TreeEventRepository treeEventRepository;
    private final TreeMemberRepository treeMemberRepository;
    private final TreePersonRepository treePersonRepository;
    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;
    private final TreeRepository treeRepository;
    private final UserRepository userRepository;
    private final PersonMapper personMapper;

    @Override
    @Transactional
    public EventResponse createEvent(UUID treeId, UUID requesterId,
                                     CreateEventRequest request,
                                     AddTreeEventRequest treeEventRequest) {
        requireTreeMember(requesterId, treeId);

        User creator = userRepository.findById(requesterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Address address = addressRepository.findById(treeEventRequest.addressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));

        // Tạo event
        Event event = Event.builder()
                .createdBy(creator)
                .name(request.name())
                .description(request.description())
                .startedAt(request.startedAt())
                .endedAt(request.endedAt())
                .status((short) 1)
                .build();
        eventRepository.save(event);

        // Gắn event vào tree
        TreeEvent treeEvent = TreeEvent.builder()
                .tree(tree)
                .event(event)
                .address(address)
                .name(treeEventRequest.name())
                .build();
        treeEventRepository.save(treeEvent);

        return buildEventResponse(event, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID treeId, UUID eventId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        Event event = findEventInTree(eventId, treeId);
        List<PersonEvent> participants = personEventRepository.findByEventId(eventId);
        return buildEventResponse(event, participants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getTreeEvents(UUID treeId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        return eventRepository.findAllByTreeId(treeId).stream()
                .map(e -> buildEventResponse(e, personEventRepository.findByEventId(e.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getPersonEvents(UUID treeId, UUID personId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        return eventRepository.findAllByPersonId(personId).stream()
                .map(e -> buildEventResponse(e, personEventRepository.findByEventId(e.getId())))
                .toList();
    }

    @Override
    @Transactional
    public EventResponse addPersonToEvent(UUID treeId, UUID eventId,
                                          UUID requesterId, AddPersonToEventRequest request) {
        requireTreeMember(requesterId, treeId);
        Event event = findEventInTree(eventId, treeId);

        if (!treePersonRepository.existsByTreeIdAndPersonIdAndDeletedAtIsNull(treeId, request.personId())) {
            throw new AppException(ErrorCode.PERSON_NOT_FOUND);
        }
        if (personEventRepository.existsByPersonIdAndEventId(request.personId(), eventId)) {
            throw new AppException(ErrorCode.CONFLICT);
        }

        Person person = personRepository.findByIdAndDeletedAtIsNull(request.personId())
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));

        EventType eventType = eventTypeRepository.findById(request.eventTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_TYPE_NOT_FOUND));

        RoleInEvent roleInEvent = roleInEventRepository.findById(request.roleInEventId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_IN_EVENT_NOT_FOUND));

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        PersonEvent personEvent = PersonEvent.builder()
                .person(person)
                .event(event)
                .eventType(eventType)
                .roleInEvent(roleInEvent)
                .address(address)
                .name(request.name())
                .build();
        personEventRepository.save(personEvent);

        return buildEventResponse(event, personEventRepository.findByEventId(eventId));
    }

    @Override
    @Transactional
    public void removePersonFromEvent(UUID treeId, UUID eventId, UUID personId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        findEventInTree(eventId, treeId);

        PersonEvent pe = personEventRepository.findByPersonId(personId).stream()
                .filter(p -> p.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));
        personEventRepository.deleteById(pe.getId());
    }

    @Override
    @Transactional
    public void deleteEvent(UUID treeId, UUID eventId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        findEventInTree(eventId, treeId);

        treeEventRepository.findByTreeIdAndEventId(treeId, eventId)
                .ifPresent(treeEventRepository::delete);

        personEventRepository.findByEventId(eventId)
                        .forEach(personEventRepository::delete);

        eventRepository.deleteById(eventId);
    }

    // ==================== Helpers ====================

    private Event findEventInTree(UUID eventId, UUID treeId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        if (!treeEventRepository.existsByTreeIdAndEventId(treeId, eventId)) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }
        return event;
    }

    private void requireTreeMember(UUID userId, UUID treeId) {
        if (!treeMemberRepository.existsByUserIdAndTreeIdAndStatus(userId, treeId, TreeMemberStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TREE_ACCESS_DENIED);
        }
    }

    private AddressResponse toAddressResponse(Address a) {
        if (a == null) return null;
        return new AddressResponse(
                a.getId(), a.getFormattedAddress(), a.getAddressLine(),
                a.getWard(), a.getDistrict(), a.getCity(), a.getProvince(),
                a.getCountry(), a.getLatitude(), a.getLongitude(),
                a.getPlaceId(), null, null, null, false, null
        );
    }

    private EventResponse buildEventResponse(Event event, List<PersonEvent> participants) {
        List<EventResponse.PersonInEventResponse> participantResponses = participants.stream()
                .map(pe -> new EventResponse.PersonInEventResponse(
                        pe.getId(),
                        personMapper.toResponse(pe.getPerson()),
                        pe.getEventType().getName(),
                        pe.getRoleInEvent().getName(),
                        toAddressResponse(pe.getAddress()),
                        pe.getName()
                ))
                .toList();

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartedAt(),
                event.getEndedAt(),
                event.getStatus(),
                event.getCreatedBy().getUserName(),
                participantResponses
        );
    }
}