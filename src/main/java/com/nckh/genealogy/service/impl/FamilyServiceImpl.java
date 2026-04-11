package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.family.AddChildRequest;
import com.nckh.genealogy.dto.request.family.AddParentRequest;
import com.nckh.genealogy.dto.request.family.AddSpouseRequest;
import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.response.family.CheckDeletableResponse;
import com.nckh.genealogy.dto.response.family.FamilyResponse;
import com.nckh.genealogy.dto.response.family.PersonFamilyResponse;
import com.nckh.genealogy.dto.response.family.TreeGraphResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.entity.*;
import com.nckh.genealogy.enums.TreeMemberStatus;
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
        requireTreeMember(requesterId, treeId);
        Person person = findPersonInTree(personId, treeId);

        Person spouse = createAndAddPersonToTree(
                request.firstName(), request.lastName(), request.gender(),
                request.dateOfBirth(), request.dateOfDeath(),
                request.citizenIdentificationNumber(), request.avatarUrl(),
                treeId
        );

        if (familyRepository.existsByParents(personId, spouse.getId())) {
            throw new AppException(ErrorCode.FAMILY_ALREADY_EXISTS);
        }

        // Tìm các family chỉ có 1 cha/mẹ của person
        List<Family> singleParentFamilies = familyRepository.findSingleParentFamilies(personId);

        Family family;

        if (!singleParentFamilies.isEmpty()) {
            // Điền spouse vào family đơn đã có → KHÔNG tạo thêm family mới
            family = singleParentFamilies.get(0); // chỉ lấy family đầu tiên
            if (family.getParent2() == null) {
                family.setParent2(spouse);
            } else {
                family.setParent1(spouse);
            }
            if (request.fromDate() != null) family.setFromDate(request.fromDate());
            if (request.toDate() != null) family.setToDate(request.toDate());
            familyRepository.save(family);
        } else {
            // Không có family đơn nào → tạo family mới
            family = Family.builder()
                    .parent1(person)
                    .parent2(spouse)
                    .fromDate(request.fromDate())
                    .toDate(request.toDate())
                    .build();
            familyRepository.save(family);
        }

        List<FamilyChild> children = familyChildRepository.findChildrenByFamilyId(family.getId());
        return buildFamilyResponse(family, children);
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
        return buildTreeGraph(treeId);
    }

    @Override
    @Transactional(readOnly = true)
    public TreeGraphResponse getTreeGraphPublic(UUID treeId) {
        treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));
        return buildTreeGraph(treeId); // dùng chung
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
        if (!treeMemberRepository.existsByUserIdAndTreeIdAndStatusIsActive(
                userId, treeId)) {
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

    private Map<UUID, Integer> calculateGenerations(List<Family> families,
                                                    Map<UUID, List<UUID>> childrenMap,
                                                    List<Person> persons) {
        Map<UUID, Integer> generationMap = new HashMap<>();

        // parentMap: childId → Set<parentId>
        Map<UUID, Set<UUID>> parentMap = new HashMap<>();
        for (Family f : families) {
            for (UUID childId : childrenMap.getOrDefault(f.getId(), List.of())) {
                parentMap.computeIfAbsent(childId, k -> new HashSet<>());
                parentMap.get(childId).add(f.getParent1().getId());
                if (f.getParent2() != null) parentMap.get(childId).add(f.getParent2().getId());
            }
        }

        // spouseMap: personId → Set<spouseId>
        Map<UUID, Set<UUID>> spouseMap = new HashMap<>();
        for (Family f : families) {
            if (f.getParent2() != null) {
                spouseMap.computeIfAbsent(f.getParent1().getId(), k -> new HashSet<>())
                        .add(f.getParent2().getId());
                spouseMap.computeIfAbsent(f.getParent2().getId(), k -> new HashSet<>())
                        .add(f.getParent1().getId());
            }
        }

        Set<UUID> allPersonIds = persons.stream().map(Person::getId).collect(Collectors.toSet());
        Set<UUID> hasParent = parentMap.keySet();

        // personChildrenMap: parentId → childIds
        Map<UUID, List<UUID>> personChildrenMap = new HashMap<>();
        for (Family f : families) {
            List<UUID> kids = childrenMap.getOrDefault(f.getId(), List.of());
            personChildrenMap.computeIfAbsent(f.getParent1().getId(), k -> new ArrayList<>()).addAll(kids);
            if (f.getParent2() != null) {
                personChildrenMap.computeIfAbsent(f.getParent2().getId(), k -> new ArrayList<>()).addAll(kids);
            }
        }

        // Chỉ seed generation 1 cho người THỰC SỰ là root:
        // không có cha mẹ VÀ có cha mẹ trong cây (tức partner có cha mẹ → không phải in-law)
        // Logic đơn giản hơn: ưu tiên người không có cha mẹ VÀ partner CÓ cha mẹ không được seed
        // → chỉ seed người không có cha mẹ VÀ không có spouse nào có cha mẹ trong cây
        for (UUID id : allPersonIds) {
            if (hasParent.contains(id)) continue; // có cha mẹ → bỏ qua

            // Kiểm tra xem có spouse nào có cha mẹ trong cây không
            boolean spouseHasParent = spouseMap.getOrDefault(id, Set.of())
                    .stream().anyMatch(hasParent::contains);

            if (!spouseHasParent) {
                // Không có cha mẹ, không có spouse nào có cha mẹ → true root, seed gen 1
                generationMap.put(id, 1);
            }
            // Nếu spouse có cha mẹ → đây là in-law, sẽ nhận generation từ spouse qua BFS
        }

        // BFS từ seeds
        Queue<UUID> queue = new LinkedList<>(generationMap.keySet());
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            int currentGen = generationMap.get(current);

            // Đồng bộ spouse cùng generation (override nếu chưa có)
            for (UUID spouseId : spouseMap.getOrDefault(current, Set.of())) {
                if (!generationMap.containsKey(spouseId)) {
                    generationMap.put(spouseId, currentGen);
                    queue.add(spouseId);
                }
            }

            // Con = generation + 1
            for (UUID childId : personChildrenMap.getOrDefault(current, List.of())) {
                if (!generationMap.containsKey(childId)) {
                    generationMap.put(childId, currentGen + 1);
                    queue.add(childId);
                }
            }
        }

        // Isolated → generation 1
        allPersonIds.forEach(id -> generationMap.putIfAbsent(id, 1));

        return generationMap;
    }

    private UUID findRootPerson(List<Person> persons, List<Family> families,
                                Map<UUID, List<UUID>> childrenMap) {
        Set<UUID> hasParent = families.stream()
                .flatMap(f -> childrenMap.getOrDefault(f.getId(), List.of()).stream())
                .collect(Collectors.toSet());

        // spouseMap: personId → Set<spouseId>
        Map<UUID, Set<UUID>> spouseMap = new HashMap<>();
        for (Family f : families) {
            if (f.getParent2() != null) {
                spouseMap.computeIfAbsent(f.getParent1().getId(), k -> new HashSet<>())
                        .add(f.getParent2().getId());
                spouseMap.computeIfAbsent(f.getParent2().getId(), k -> new HashSet<>())
                        .add(f.getParent1().getId());
            }
        }

        Set<UUID> isParent = new HashSet<>();
        for (Family f : families) {
            isParent.add(f.getParent1().getId());
            if (f.getParent2() != null) isParent.add(f.getParent2().getId());
        }

        // Root lý tưởng: không có cha mẹ + là parent + không có spouse nào có cha mẹ
        Optional<UUID> idealRoot = persons.stream()
                .map(Person::getId)
                .filter(id -> !hasParent.contains(id))
                .filter(isParent::contains)
                .filter(id -> spouseMap.getOrDefault(id, Set.of())
                        .stream().noneMatch(hasParent::contains))
                .findFirst();

        if (idealRoot.isPresent()) return idealRoot.get();

        // Fallback: không có cha mẹ + là parent (kể cả in-law)
        Optional<UUID> fallback = persons.stream()
                .map(Person::getId)
                .filter(id -> !hasParent.contains(id))
                .filter(isParent::contains)
                .findFirst();

        if (fallback.isPresent()) return fallback.get();

        // Fallback cuối: không có cha mẹ
        return persons.stream()
                .map(Person::getId)
                .filter(id -> !hasParent.contains(id))
                .findFirst()
                .orElse(persons.isEmpty() ? null : persons.get(0).getId());
    }

    private TreeGraphResponse buildTreeGraph(UUID treeId) {
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
                        childrenMap.getOrDefault(f.getId(), Collections.emptyList())
                ))
                .toList();

        int totalGenerations = generationMap.values().stream()
                .mapToInt(Integer::intValue).max().orElse(1);

        // Root = person không có cha mẹ trong tree và có đời con
        UUID rootPersonId = findRootPerson(persons, families, childrenMap);

        return new TreeGraphResponse(
                personNodes,
                familyNodes,
                new TreeGraphResponse.Meta(persons.size(), totalGenerations, rootPersonId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CheckDeletableResponse checkDeletable(UUID treeId, UUID personId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);

        List<Person> allPersons = treePersonRepository
                .findByTreeIdAndDeletedAtIsNull(treeId)
                .stream().map(TreePerson::getPerson).toList();

        List<Family> allFamilies = familyRepository.findAllByTreeId(treeId);

        // childrenMap: familyId → List<childId>
        Map<UUID, List<UUID>> childrenMap = new HashMap<>();
        for (Family f : allFamilies) {
            childrenMap.put(f.getId(),
                    familyChildRepository.findChildrenByFamilyId(f.getId())
                            .stream().map(fc -> fc.getPerson().getId()).toList());
        }

        // childParentsMap: childId → Set<parentId>
        Map<UUID, Set<UUID>> childParentsMap = new HashMap<>();
        for (Family f : allFamilies) {
            for (UUID childId : childrenMap.getOrDefault(f.getId(), List.of())) {
                childParentsMap.computeIfAbsent(childId, k -> new HashSet<>());
                childParentsMap.get(childId).add(f.getParent1().getId());
                if (f.getParent2() != null) {
                    childParentsMap.get(childId).add(f.getParent2().getId());
                }
            }
        }

        // Families bị remove khi xóa person (person là parent)
        Set<UUID> removedFamilyIds = allFamilies.stream()
                .filter(f -> f.getParent1().getId().equals(personId)
                        || (f.getParent2() != null && f.getParent2().getId().equals(personId)))
                .map(Family::getId)
                .collect(Collectors.toSet());

        // Tất cả con của person bị xóa
        Set<UUID> childrenOfDeletedPerson = removedFamilyIds.stream()
                .flatMap(fid -> childrenMap.getOrDefault(fid, List.of()).stream())
                .collect(Collectors.toSet());

        // Remaining
        Set<UUID> remainingIds = allPersons.stream()
                .map(Person::getId)
                .filter(id -> !id.equals(personId))
                .collect(Collectors.toSet());

        List<Family> remainingFamilies = allFamilies.stream()
                .filter(f -> !removedFamilyIds.contains(f.getId()))
                .toList();

        // connectedToAnyFamily: ai còn trong ít nhất 1 family còn lại
        Set<UUID> connectedToAnyFamily = new HashSet<>();
        for (Family f : remainingFamilies) {
            connectedToAnyFamily.add(f.getParent1().getId());
            if (f.getParent2() != null) connectedToAnyFamily.add(f.getParent2().getId());
            childrenMap.getOrDefault(f.getId(), List.of()).forEach(connectedToAnyFamily::add);
        }

        // Các family bị remove nhưng CÓ CON sẽ được GIỮ LẠI (chỉ null parent)
        // → spouse + con trong family đó vẫn connected
        for (UUID fid : removedFamilyIds) {
            boolean hasKids = !childrenMap.getOrDefault(fid, List.of()).isEmpty();
            if (!hasKids) continue;

            Family f = allFamilies.stream()
                    .filter(fam -> fam.getId().equals(fid))
                    .findFirst().orElse(null);
            if (f == null) continue;

            UUID spouseId = f.getParent1().getId().equals(personId)
                    ? (f.getParent2() != null ? f.getParent2().getId() : null)
                    : f.getParent1().getId();
            if (spouseId != null) connectedToAnyFamily.add(spouseId);

            childrenMap.getOrDefault(fid, List.of()).forEach(connectedToAnyFamily::add);
        }

        // Kiểm tra từng con: còn parent nào khác không?
        Set<UUID> willBeOrphaned = new HashSet<>();
        for (UUID childId : childrenOfDeletedPerson) {
            Set<UUID> remainingParents = childParentsMap.getOrDefault(childId, Set.of())
                    .stream()
                    .filter(pid -> !pid.equals(personId))
                    .collect(Collectors.toSet());
            if (remainingParents.isEmpty()) {
                willBeOrphaned.add(childId);
            }
        }

        // Kiểm tra spouse: còn liên kết không?
        Set<UUID> spousesOfDeletedPerson = new HashSet<>();
        for (UUID fid : removedFamilyIds) {
            Family f = allFamilies.stream()
                    .filter(fam -> fam.getId().equals(fid))
                    .findFirst().orElse(null);
            if (f == null) continue;

            UUID spouseId = f.getParent1().getId().equals(personId)
                    ? (f.getParent2() != null ? f.getParent2().getId() : null)
                    : f.getParent1().getId();
            if (spouseId != null) spousesOfDeletedPerson.add(spouseId);
        }

        for (UUID spouseId : spousesOfDeletedPerson) {
            if (!connectedToAnyFamily.contains(spouseId)) {
                willBeOrphaned.add(spouseId);
            }
        }

        // Build parentChildrenMap sau khi xóa (dùng remainingFamilies + kept families)
        Map<UUID, List<UUID>> parentChildrenMap = new HashMap<>();

        // Từ remainingFamilies
        for (Family f : remainingFamilies) {
            List<UUID> kids = childrenMap.getOrDefault(f.getId(), List.of());
            if (kids.isEmpty()) continue;
            parentChildrenMap.computeIfAbsent(f.getParent1().getId(), k -> new ArrayList<>()).addAll(kids);
            if (f.getParent2() != null) {
                parentChildrenMap.computeIfAbsent(f.getParent2().getId(), k -> new ArrayList<>()).addAll(kids);
            }
        }

        // Từ removed families có con (được giữ lại với spouse còn lại)
        for (UUID fid : removedFamilyIds) {
            List<UUID> kids = childrenMap.getOrDefault(fid, List.of());
            if (kids.isEmpty()) continue;

            Family f = allFamilies.stream()
                    .filter(fam -> fam.getId().equals(fid))
                    .findFirst().orElse(null);
            if (f == null) continue;

            UUID spouseId = f.getParent1().getId().equals(personId)
                    ? (f.getParent2() != null ? f.getParent2().getId() : null)
                    : f.getParent1().getId();
            if (spouseId != null) {
                parentChildrenMap.computeIfAbsent(spouseId, k -> new ArrayList<>()).addAll(kids);
            }
        }

        // hasParentAfterDelete: ai có cha mẹ sau khi xóa
        Set<UUID> hasParentAfterDelete = new HashSet<>();
        for (Family f : remainingFamilies) {
            childrenMap.getOrDefault(f.getId(), List.of()).forEach(hasParentAfterDelete::add);
        }
        // Con trong removed families có con vẫn có parent (spouse còn lại)
        for (UUID fid : removedFamilyIds) {
            List<UUID> kids = childrenMap.getOrDefault(fid, List.of());
            if (!kids.isEmpty()) kids.forEach(hasParentAfterDelete::add);
        }

        // BFS từ valid roots
        Set<UUID> roots = remainingIds.stream()
                .filter(id -> !hasParentAfterDelete.contains(id))
                .filter(connectedToAnyFamily::contains)
                .filter(id -> !willBeOrphaned.contains(id))
                .collect(Collectors.toSet());

        Set<UUID> reachable = new HashSet<>(roots);
        Queue<UUID> queue = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            for (UUID child : parentChildrenMap.getOrDefault(current, List.of())) {
                if (reachable.add(child)) queue.add(child);
            }
        }

        // finalOrphans = willBeOrphaned + không reachable + có liên kết family
        Set<UUID> finalOrphans = new HashSet<>(willBeOrphaned);
        remainingIds.stream()
                .filter(id -> !reachable.contains(id))
                .filter(connectedToAnyFamily::contains)
                .forEach(finalOrphans::add);

        if (finalOrphans.isEmpty()) {
            return new CheckDeletableResponse(true, List.of(), List.of(), null);
        }

        List<UUID> orphanedIds = List.copyOf(finalOrphans);
        List<String> orphanedNames = orphanedIds.stream()
                .map(id -> personRepository.findById(id)
                        .map(p -> p.getLastName() + " " + p.getFirstName())
                        .orElse("Không xác định"))
                .toList();

        String message = "Xóa người này sẽ làm " + orphanedIds.size()
                + " người mất liên kết: " + String.join(", ", orphanedNames);

        return new CheckDeletableResponse(false, orphanedIds, orphanedNames, message);
    }

    @Override
    @Transactional
    public void hardDeletePerson(UUID treeId, UUID personId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        findPersonInTree(personId, treeId);

        CheckDeletableResponse check = checkDeletable(treeId, personId, requesterId);
        if (!check.deletable()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, check.message());
        }

        // 1. Xóa khỏi family_children (person là CON)
        familyChildRepository.deleteByPersonId(personId);

        // 2. Xử lý các family mà person là PARENT
        List<Family> familiesAsParent = familyRepository.findFamiliesAsParent(personId);
        for (Family f : familiesAsParent) {
            UUID otherParentId = f.getParent1().getId().equals(personId)
                    ? (f.getParent2() != null ? f.getParent2().getId() : null)
                    : f.getParent1().getId();

            boolean hasChildren = !familyChildRepository
                    .findChildrenByFamilyId(f.getId()).isEmpty();

            if (otherParentId != null && hasChildren) {
                // Còn parent kia + còn con → giữ family, chỉ null parent bị xóa
                if (f.getParent1().getId().equals(personId)) {
                    // parent1 bị xóa → swap: đưa parent2 lên parent1, null parent2
                    f.setParent1(f.getParent2());
                    f.setParent2(null);
                } else {
                    // parent2 bị xóa → chỉ null parent2
                    f.setParent2(null);
                }
                familyRepository.save(f);
            } else {
                // Không có parent kia hoặc không có con → xóa cả family
                familyChildRepository.deleteByFamilyId(f.getId());
                familyRepository.delete(f);
            }
        }

        // 3. Xóa khỏi tree_persons
        treePersonRepository.deleteByPersonId(personId);

        // 4. Hard delete person
        personRepository.deleteById(personId);
    }
}