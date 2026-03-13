package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.family.AddChildRequest;
import com.nckh.genealogy.dto.request.family.AddParentRequest;
import com.nckh.genealogy.dto.request.family.AddSpouseRequest;
import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.response.family.FamilyResponse;
import com.nckh.genealogy.dto.response.family.PersonFamilyResponse;
import com.nckh.genealogy.dto.response.family.TreeGraphResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.entity.*;
import com.nckh.genealogy.enums.TreeMemberStatus;
import com.nckh.genealogy.enums.UnionType;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.mapper.PersonMapper;
import com.nckh.genealogy.repository.*;
import com.nckh.genealogy.service.FamilyService;
import com.nckh.genealogy.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyChildRepository familyChildRepository;
    private final PersonRepository personRepository;
    private final TreePersonRepository treePersonRepository;
    private final TreeMemberRepository treeMemberRepository;
    private final TreeRepository treeRepository;
    private final PersonMapper personMapper;
    private final PersonService personService;

    // ==================== Add Spouse ====================

    @Override
    @Transactional
    public FamilyResponse addSpouse(UUID treeId, UUID personId,
                                    UUID requesterId, AddSpouseRequest request) {
        // Kiểm tra requester là member của tree
        requireTreeMember(requesterId, treeId);

        // Kiểm tra person tồn tại và thuộc tree
        Person person = findPersonInTree(personId, treeId);

        // Tạo person mới (vợ/chồng)
        Person spouse = createAndAddPersonToTree(
                request.firstName(), request.lastName(), request.gender(),
                request.dateOfBirth(), request.dateOfDeath(),
                request.citizenIdentificationNumber(), request.avatarUrl(),
                treeId
        );

        // Kiểm tra 2 người chưa có family với nhau
        if (familyRepository.existsByParents(personId, spouse.getId())) {
            throw new AppException(ErrorCode.FAMILY_ALREADY_EXISTS);
        }

        // Tạo family
        Family family = Family.builder()
                .parent1(person)
                .parent2(spouse)
                .unionType(request.unionType())
                .fromDate(request.fromDate())
                .toDate(request.toDate())
                .build();
        familyRepository.save(family);

        return buildFamilyResponse(family, Collections.emptyList());
    }

    // ==================== Add Parent ====================

    @Override
    @Transactional
    public FamilyResponse addParent(UUID treeId, UUID personId,
                                    UUID requesterId, AddParentRequest request) {
        requireTreeMember(requesterId, treeId);
        Person person = findPersonInTree(personId, treeId);

        // Tìm family hiện tại của person (nếu có)
        Optional<Family> existingFamily = familyRepository.findFamilyAsChild(personId);

        // TH3 — Đã có đủ 2 cha mẹ
        if (existingFamily.isPresent()) {
            Family f = existingFamily.get();
            if (f.getParent1() != null && f.getParent2() != null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Nhân vật đã có đủ cha và mẹ");
            }
        }

        // Tạo person mới (cha/mẹ)
        Person parent = createAndAddPersonToTree(
                request.firstName(), request.lastName(), request.gender(),
                request.dateOfBirth(), request.dateOfDeath(),
                request.citizenIdentificationNumber(), request.avatarUrl(),
                treeId
        );

        // Kiểm tra vòng lặp — parent không được là con cháu của person
        if (isDescendant(parent.getId(), personId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Không thể thêm con cháu làm cha/mẹ — tạo vòng lặp trong gia phả");
        }

        Family family;

        if (existingFamily.isEmpty()) {
            // TH1 — Chưa có family → tạo mới
            family = Family.builder()
                    .parent1(parent)
                    .parent2(null)
                    .unionType(request.unionType() != null ? request.unionType() : UnionType.UNKNOWN)
                    .fromDate(request.fromDate())
                    .toDate(request.toDate())
                    .build();
            familyRepository.save(family);

            // Thêm person là con
            FamilyChild child = FamilyChild.builder()
                    .id(new FamilyChildId(family.getId(), personId))
                    .family(family)
                    .person(person)
                    .build();
            familyChildRepository.save(child);

        } else {
            // TH2 — Đã có family, còn 1 slot trống → điền vào
            family = existingFamily.get();
            if (family.getParent1() == null) {
                family.setParent1(parent);
            } else {
                family.setParent2(parent);
            }
            familyRepository.save(family);
        }

        List<FamilyChild> children = familyChildRepository.findChildrenByFamilyId(family.getId());
        return buildFamilyResponse(family, children);
    }

    // ==================== Add Child ====================

    @Override
    @Transactional
    public FamilyResponse addChild(UUID treeId, UUID familyId,
                                   UUID requesterId, AddChildRequest request) {
        requireTreeMember(requesterId, treeId);

        Family family = findFamilyInTree(familyId, treeId);

        // Tạo person mới (con)
        Person child = createAndAddPersonToTree(
                request.firstName(), request.lastName(), request.gender(),
                request.dateOfBirth(), request.dateOfDeath(),
                request.citizenIdentificationNumber(), request.avatarUrl(),
                treeId
        );

        // Kiểm tra person đã là con trong family nào trong tree chưa
        if (familyChildRepository.existsAsChildInTree(child.getId(), treeId)) {
            throw new AppException(ErrorCode.PERSON_ALREADY_A_CHILD);
        }

        // Kiểm tra con không phải là cha/mẹ trong family này
        UUID parent1Id = family.getParent1().getId();
        UUID parent2Id = family.getParent2() != null ? family.getParent2().getId() : null;

        if (child.getId().equals(parent1Id) || child.getId().equals(parent2Id)) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Nhân vật không thể vừa là cha/mẹ vừa là con trong cùng gia đình");
        }

        // Kiểm tra vòng lặp — child không được là tổ tiên của parent
        if (isAncestor(child.getId(), parent1Id)) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Không thể thêm tổ tiên làm con — tạo vòng lặp trong gia phả");
        }

        FamilyChild familyChild = FamilyChild.builder()
                .id(new FamilyChildId(familyId, child.getId()))
                .family(family)
                .person(child)
                .build();
        familyChildRepository.save(familyChild);

        List<FamilyChild> children = familyChildRepository.findChildrenByFamilyId(familyId);
        return buildFamilyResponse(family, children);
    }

    // ==================== Remove Child ====================

    @Override
    @Transactional
    public void removeChild(UUID treeId, UUID familyId, UUID personId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        findFamilyInTree(familyId, treeId);

        FamilyChildId id = new FamilyChildId(familyId, personId);
        if (!familyChildRepository.existsById(id)) {
            throw new AppException(ErrorCode.PERSON_NOT_FOUND);
        }
        familyChildRepository.deleteById(id);
    }

    // ==================== Delete Family ====================

    @Override
    @Transactional
    public void deleteFamily(UUID treeId, UUID familyId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        Family family = findFamilyInTree(familyId, treeId);
        family.setDeletedAt(java.time.LocalDateTime.now());
        familyRepository.save(family);
    }

    // ==================== Get Person Family ====================

    @Override
    @Transactional(readOnly = true)
    public PersonFamilyResponse getPersonFamily(UUID treeId, UUID personId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        Person person = findPersonInTree(personId, treeId);

        // Gia đình cha mẹ của person
        FamilyResponse parentFamily = familyRepository.findFamilyAsChild(personId)
                .map(f -> {
                    List<FamilyChild> children = familyChildRepository.findChildrenByFamilyId(f.getId());
                    return buildFamilyResponse(f, children);
                })
                .orElse(null);

        // Các gia đình mà person là cha/mẹ
        List<FamilyResponse> spouseFamilies = familyRepository.findFamiliesAsParent(personId)
                .stream()
                .map(f -> {
                    List<FamilyChild> children = familyChildRepository.findChildrenByFamilyId(f.getId());
                    return buildFamilyResponse(f, children);
                })
                .toList();

        return new PersonFamilyResponse(
                personMapper.toResponse(person),
                parentFamily,
                spouseFamilies
        );
    }

    // ==================== Get Tree Graph ====================

    @Override
    @Transactional(readOnly = true)
    public TreeGraphResponse getTreeGraph(UUID treeId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);

        // Lấy tất cả family trong tree
        List<Family> families = familyRepository.findAllByTreeId(treeId);

        // Lấy tất cả children
        Map<UUID, List<UUID>> childrenMap = new HashMap<>();
        for (Family f : families) {
            List<UUID> childIds = familyChildRepository.findChildrenByFamilyId(f.getId())
                    .stream()
                    .map(fc -> fc.getPerson().getId())
                    .toList();
            childrenMap.put(f.getId(), childIds);
        }

        // Lấy tất cả person trong tree
        List<Person> persons = treePersonRepository
                .findByTreeIdAndDeletedAtIsNull(treeId)
                .stream()
                .map(TreePerson::getPerson)
                .toList();

        // Tính generation cho từng person bằng BFS
        Map<UUID, Integer> generationMap = calculateGenerations(families, childrenMap, persons);

        // Build PersonNode list
        List<TreeGraphResponse.PersonNode> personNodes = persons.stream()
                .map(p -> new TreeGraphResponse.PersonNode(
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getLastName() + " " + p.getFirstName(),
                        p.getGender(),
                        p.getAvatarUrl(),
                        p.getDateOfBirth(),
                        p.getDateOfDeath(),
                        generationMap.getOrDefault(p.getId(), 1)
                ))
                .toList();

        // Build FamilyNode list
        List<TreeGraphResponse.FamilyNode> familyNodes = families.stream()
                .map(f -> new TreeGraphResponse.FamilyNode(
                        f.getId(),
                        f.getParent1().getId(),
                        f.getParent2() != null ? f.getParent2().getId() : null,
                        f.getUnionType(),
                        childrenMap.getOrDefault(f.getId(), Collections.emptyList())
                ))
                .toList();

        int totalGenerations = generationMap.values().stream()
                .mapToInt(Integer::intValue).max().orElse(1);

        // Root = person không có cha mẹ trong tree và có đời con
        UUID rootPersonId = findRootPerson(persons, families);

        return new TreeGraphResponse(
                personNodes,
                familyNodes,
                new TreeGraphResponse.Meta(persons.size(), totalGenerations, rootPersonId)
        );
    }

    @Transactional
    @Override
    public PersonResponse addFirstPersonIntoTree(UUID treeId, UUID requesterId, CreatePersonRequest request) {

        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(()-> new AppException(ErrorCode.TREE_NOT_FOUND));

        requireTreeMember(requesterId, treeId);

        // Kiểm tra CCCD trùng nếu có
        if (StringUtils.hasText(request.citizenIdentificationNumber())
                && personRepository.existsByCitizenIdentificationNumberAndDeletedAtIsNull(
                request.citizenIdentificationNumber())) {
            throw new AppException(ErrorCode.CITIZEN_ID_ALREADY_EXISTS);
        }

        Person person = personRepository.save(personMapper.toEntity(request));

        TreePerson treePerson = TreePerson.builder()
                .tree(tree)
                .person(person)
                .build();
        treePersonRepository.save(treePerson);

        return personMapper.toResponse(person);
    }

    // ==================== Helpers ====================

    private Person createAndAddPersonToTree(String firstName, String lastName,
                                            com.nckh.genealogy.enums.Gender gender,
                                            java.time.LocalDateTime dateOfBirth,
                                            java.time.LocalDateTime dateOfDeath,
                                            String cin, String avatarUrl,
                                            UUID treeId) {
        // Tạo person
        Person person = Person.builder()
                .firstName(firstName)
                .lastName(lastName)
                .gender(gender)
                .dateOfBirth(dateOfBirth)
                .dateOfDeath(dateOfDeath)
                .citizenIdentificationNumber(cin)
                .avatarUrl(avatarUrl)
                .build();
        personRepository.save(person);

        // Thêm vào tree
        Tree tree = new Tree();
        tree.setId(treeId);
        TreePerson treePerson = TreePerson.builder()
                .tree(tree)
                .person(person)
                .build();
        treePersonRepository.save(treePerson);

        return person;
    }

    private Person findPersonInTree(UUID personId, UUID treeId) {
        Person person = personRepository.findByIdAndDeletedAtIsNull(personId)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));

        if (!treePersonRepository.existsByTreeIdAndPersonIdAndDeletedAtIsNull(treeId, personId)) {
            throw new AppException(ErrorCode.PERSON_NOT_FOUND);
        }
        return person;
    }

    private Family findFamilyInTree(UUID familyId, UUID treeId) {
        Family family = familyRepository.findByIdAndDeletedAtIsNull(familyId)
                .orElseThrow(() -> new AppException(ErrorCode.FAMILY_NOT_FOUND));

        // Kiểm tra family thuộc tree (parent1 phải thuộc tree)
        if (!treePersonRepository.existsByTreeIdAndPersonIdAndDeletedAtIsNull(
                treeId, family.getParent1().getId())) {
            throw new AppException(ErrorCode.FAMILY_NOT_FOUND);
        }
        return family;
    }

    private void requireTreeMember(UUID userId, UUID treeId) {
        if (!treeMemberRepository.existsByUserIdAndTreeIdAndStatus(
                userId, treeId, TreeMemberStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TREE_ACCESS_DENIED);
        }
    }

    private FamilyResponse buildFamilyResponse(Family family, List<FamilyChild> children) {
        PersonResponse parent1Response = personMapper.toResponse(family.getParent1());
        PersonResponse parent2Response = family.getParent2() != null
                ? personMapper.toResponse(family.getParent2()) : null;
        List<PersonResponse> childResponses = children.stream()
                .map(fc -> personMapper.toResponse(fc.getPerson()))
                .toList();

        return new FamilyResponse(
                family.getId(),
                parent1Response,
                parent2Response,
                family.getUnionType(),
                family.getFromDate(),
                family.getToDate(),
                childResponses
        );
    }

    /**
     * Kiểm tra targetId có phải là tổ tiên của personId không (BFS)
     */
    private boolean isAncestor(UUID targetId, UUID personId) {
        Queue<UUID> queue = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();
        queue.add(personId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (current.equals(targetId)) return true;

            familyRepository.findFamilyAsChild(current).ifPresent(f -> {
                if (f.getParent1() != null && !visited.contains(f.getParent1().getId())) {
                    visited.add(f.getParent1().getId());
                    queue.add(f.getParent1().getId());
                }
                if (f.getParent2() != null && !visited.contains(f.getParent2().getId())) {
                    visited.add(f.getParent2().getId());
                    queue.add(f.getParent2().getId());
                }
            });
        }
        return false;
    }

    /**
     * Kiểm tra targetId có phải là con cháu của personId không (BFS)
     */
    private boolean isDescendant(UUID targetId, UUID personId) {
        Queue<UUID> queue = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();
        queue.add(personId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (current.equals(targetId)) return true;

            familyRepository.findFamiliesAsParent(current).forEach(f -> {
                familyChildRepository.findChildrenByFamilyId(f.getId()).forEach(fc -> {
                    UUID childId = fc.getPerson().getId();
                    if (!visited.contains(childId)) {
                        visited.add(childId);
                        queue.add(childId);
                    }
                });
            });
        }
        return false;
    }

    /**
     * Tính generation cho từng person bằng BFS từ root
     * Root = thế hệ 1
     */
    private Map<UUID, Integer> calculateGenerations(List<Family> families,
                                                    Map<UUID, List<UUID>> childrenMap,
                                                    List<Person> persons) {
        Map<UUID, Integer> generationMap = new HashMap<>();

        // Build parent map: personId → parentIds
        Map<UUID, Set<UUID>> parentMap = new HashMap<>();
        for (Family f : families) {
            List<UUID> childIds = childrenMap.getOrDefault(f.getId(), Collections.emptyList());
            for (UUID childId : childIds) {
                parentMap.computeIfAbsent(childId, k -> new HashSet<>());
                if (f.getParent1() != null) parentMap.get(childId).add(f.getParent1().getId());
                if (f.getParent2() != null) parentMap.get(childId).add(f.getParent2().getId());
            }
        }

        // Tìm root (không có cha mẹ trong tree)
        Set<UUID> allPersonIds = persons.stream().map(Person::getId).collect(Collectors.toSet());
        for (UUID personId : allPersonIds) {
            if (!parentMap.containsKey(personId) || parentMap.get(personId).isEmpty()) {
                generationMap.put(personId, 1);
            }
        }

        // BFS từ roots
        Queue<UUID> queue = new LinkedList<>(generationMap.keySet());
        // Build children map: personId → childIds
        Map<UUID, List<UUID>> personChildrenMap = new HashMap<>();
        for (Family f : families) {
            List<UUID> childIds = childrenMap.getOrDefault(f.getId(), Collections.emptyList());
            if (f.getParent1() != null) {
                personChildrenMap.computeIfAbsent(f.getParent1().getId(), k -> new ArrayList<>())
                        .addAll(childIds);
            }
            if (f.getParent2() != null) {
                personChildrenMap.computeIfAbsent(f.getParent2().getId(), k -> new ArrayList<>())
                        .addAll(childIds);
            }
        }

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            int currentGen = generationMap.get(current);
            List<UUID> children = personChildrenMap.getOrDefault(current, Collections.emptyList());
            for (UUID childId : children) {
                if (!generationMap.containsKey(childId)) {
                    generationMap.put(childId, currentGen + 1);
                    queue.add(childId);
                }
            }
        }

        // Những người chưa có generation (isolated) → set = 1
        allPersonIds.forEach(id -> generationMap.putIfAbsent(id, 1));

        return generationMap;
    }

    private UUID findRootPerson(List<Person> persons, List<Family> families) {
        Set<UUID> hasParent = new HashSet<>();
        for (Family f : families) {
            familyChildRepository.findChildrenByFamilyId(f.getId())
                    .forEach(fc -> hasParent.add(fc.getPerson().getId()));
        }

        return persons.stream()
                .map(Person::getId)
                .filter(id -> !hasParent.contains(id))
                .findFirst()
                .orElse(persons.isEmpty() ? null : persons.get(0).getId());
    }
}