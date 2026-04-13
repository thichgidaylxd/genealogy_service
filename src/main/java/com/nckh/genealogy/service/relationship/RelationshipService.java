package com.nckh.genealogy.service.relationship;

import com.nckh.genealogy.dto.response.relationship.RelationshipResponse;
import com.nckh.genealogy.dto.response.relationship.RelationshipResponse.PersonNode;
import com.nckh.genealogy.entity.Person;
import com.nckh.genealogy.enums.Gender;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.repository.FamilyRepository;
import com.nckh.genealogy.repository.PersonRepository;
import com.nckh.genealogy.repository.TreePersonRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RelationshipService {

    PersonRepository personRepository;
    FamilyRepository familyRepository;
    TreePersonRepository treePersonRepository;
    RelationshipResolver resolver;

    // ── Graph node ────────────────────────────────────────────────────────────
    record GraphEdge(UUID personId, EdgeType type, UUID relatedPersonId) {}

    enum EdgeType {
        PARENT,   // relatedPersonId là cha/mẹ của personId
        CHILD,    // relatedPersonId là con của personId
        SPOUSE    // relatedPersonId là vợ/chồng của personId
    }

    // ── BFS path node ─────────────────────────────────────────────────────────
    record PathNode(UUID personId, UUID prevPersonId, EdgeType edgeFromPrev) {}

    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public RelationshipResponse getRelationship(UUID treeId, UUID personAId, UUID personBId) {

        // Validate cả 2 người đều trong tree
        if (!treePersonRepository.existsByTreeIdAndPersonId(treeId, personAId))
            throw new AppException(ErrorCode.PERSON_NOT_FOUND);
        if (!treePersonRepository.existsByTreeIdAndPersonId(treeId, personBId))
            throw new AppException(ErrorCode.PERSON_NOT_FOUND);

        Person personA = personRepository.findById(personAId)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));
        Person personB = personRepository.findById(personBId)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));

        // Build graph toàn bộ persons trong tree
        Map<UUID, List<GraphEdge>> graph = buildGraph(treeId);

        // BFS tìm đường đi ngắn nhất từ A đến B
        List<UUID> path = bfs(graph, personAId, personBId);

        if (path == null || path.isEmpty()) {
            return RelationshipResponse.builder()
                    .fromPerson(toNode(personA, null))
                    .toPerson(toNode(personB, null))
                    .relationshipFromA("Không có quan hệ")
                    .relationshipFromB("Không có quan hệ")
                    .generationDiff(0)
                    .path(Collections.emptyList())
                    .build();
        }

        // Phân tích path
        PathAnalysis analysis = analyzePath(graph, path, personA, personB);

        // Resolve tên quan hệ
        String relAtoB = resolveRelationship(analysis, (personB.getGender().equals(Gender.MALE)? Short.valueOf("1") : Short.valueOf("2")), (personA.getGender().equals(Gender.MALE)? Short.valueOf("1") : Short.valueOf("2")), false);
        String relBtoA = resolveRelationship(analysis.reverse(), (personA.getGender().equals(Gender.MALE)? Short.valueOf("1") : Short.valueOf("2")), (personB.getGender().equals(Gender.MALE)? Short.valueOf("1") : Short.valueOf("2")), true);

        // Build path nodes
        List<PersonNode> pathNodes = buildPathNodes(path, graph);

        return RelationshipResponse.builder()
                .fromPerson(toNode(personA, null))
                .toPerson(toNode(personB, null))
                .relationshipFromA(relAtoB)
                .relationshipFromB(relBtoA)
                .generationDiff(analysis.generationDiff())
                .path(pathNodes)
                .build();
    }

    // ── Build graph ───────────────────────────────────────────────────────────
    private Map<UUID, List<GraphEdge>> buildGraph(UUID treeId) {
        Map<UUID, List<GraphEdge>> graph = new HashMap<>();

        // Lấy tất cả families trong tree
        List<Object[]> families = familyRepository.findFamiliesInTree(treeId);

        for (Object[] row : families) {
            UUID familyId = (UUID) row[0];
            UUID parent1Id = (UUID) row[1];
            UUID parent2Id = (UUID) row[2]; // nullable

            // Parent1 ↔ Parent2 là vợ chồng
            if (parent2Id != null) {
                addEdge(graph, parent1Id, EdgeType.SPOUSE, parent2Id);
                addEdge(graph, parent2Id, EdgeType.SPOUSE, parent1Id);
            }

            // Children
            List<UUID> childrenIds = familyRepository.findChildrenIdsByFamilyId(familyId);
            for (UUID childId : childrenIds) {
                // parent → child
                addEdge(graph, parent1Id, EdgeType.CHILD, childId);
                if (parent2Id != null) addEdge(graph, parent2Id, EdgeType.CHILD, childId);

                // child → parent
                addEdge(graph, childId, EdgeType.PARENT, parent1Id);
                if (parent2Id != null) addEdge(graph, childId, EdgeType.PARENT, parent2Id);
            }
        }

        return graph;
    }

    private void addEdge(Map<UUID, List<GraphEdge>> graph, UUID from, EdgeType type, UUID to) {
        graph.computeIfAbsent(from, k -> new ArrayList<>())
                .add(new GraphEdge(from, type, to));
    }

    // ── BFS ───────────────────────────────────────────────────────────────────
    private List<UUID> bfs(Map<UUID, List<GraphEdge>> graph, UUID start, UUID end) {
        if (start.equals(end)) return List.of(start);

        Map<UUID, UUID> prev = new HashMap<>();
        Queue<UUID> queue = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        prev.put(start, null);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            List<GraphEdge> edges = graph.getOrDefault(current, Collections.emptyList());

            for (GraphEdge edge : edges) {
                UUID neighbor = edge.relatedPersonId();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    prev.put(neighbor, current);
                    if (neighbor.equals(end)) {
                        return reconstructPath(prev, start, end);
                    }
                    queue.add(neighbor);
                }
            }
        }

        return null; // không tìm thấy
    }

    private List<UUID> reconstructPath(Map<UUID, UUID> prev, UUID start, UUID end) {
        List<UUID> path = new ArrayList<>();
        UUID cur = end;
        while (cur != null) {
            path.add(0, cur);
            cur = prev.get(cur);
        }
        return path;
    }

    // ── Phân tích path ────────────────────────────────────────────────────────
    record PathAnalysis(
            int stepsUp,
            int stepsDown,
            int generationDiff,
            boolean throughFatherUp,
            boolean throughFatherDown,
            boolean olderThanA,
            boolean isSpouseRelation,
            boolean isInLawRelation,
            SpousePosition spousePosition  // ← thêm
    ) {
        enum SpousePosition {
            NONE,         // không có SPOUSE trong path
            AT_END,       // A→...→X↔B (B là dâu/rể của họ A)
            AT_START,     // A↔X→...→B (B là họ của vợ/chồng A)
            IN_MIDDLE     // A→...→X↔Y→...→B (thông gia 2 họ)
        }

        PathAnalysis reverse() {
            SpousePosition reversed = switch (spousePosition) {
                case AT_END -> SpousePosition.AT_START;
                case AT_START -> SpousePosition.AT_END;
                default -> spousePosition;
            };
            return new PathAnalysis(
                    stepsDown, stepsUp, -generationDiff,
                    throughFatherDown, throughFatherUp, !olderThanA,
                    isSpouseRelation, isInLawRelation, reversed
            );
        }
    }


    private PathAnalysis analyzePath(
            Map<UUID, List<GraphEdge>> graph,
            List<UUID> path,
            Person personA,
            Person personB
    ) {
        // Tìm tất cả vị trí SPOUSE trong path
        int spouseIndex = -1;
        for (int i = 0; i < path.size() - 1; i++) {
            EdgeType et = getEdgeType(graph, path.get(i), path.get(i + 1));
            if (et == EdgeType.SPOUSE) {
                spouseIndex = i;
                break;
            }
        }

        // Direct spouse
        if (path.size() == 2 && spouseIndex == 0) {
            return new PathAnalysis(0, 0, 0, true, true, false,
                    true, false, PathAnalysis.SpousePosition.NONE);
        }

        boolean olderThanA = false;
        if (personB.getDateOfBirth() != null && personA.getDateOfBirth() != null) {
            olderThanA = personB.getDateOfBirth().isBefore(personA.getDateOfBirth());
        }

        // Không có SPOUSE → quan hệ huyết thống thuần túy
        if (spouseIndex < 0) {
            return analyzeBloodPath(graph, path, olderThanA,
                    PathAnalysis.SpousePosition.NONE);
        }

        boolean spouseAtEnd   = (spouseIndex == path.size() - 2);
        boolean spouseAtStart = (spouseIndex == 0);

        // SPOUSE ở cuối: B là dâu/rể
        // Đếm steps từ A đến người kết hôn với B
        if (spouseAtEnd) {
            PathAnalysis blood = analyzeBloodPath(graph,
                    path.subList(0, spouseIndex + 1), olderThanA,
                    PathAnalysis.SpousePosition.AT_END);
            return new PathAnalysis(
                    blood.stepsUp(), blood.stepsDown(),
                    blood.stepsUp() - blood.stepsDown(),
                    blood.throughFatherUp(), blood.throughFatherDown(),
                    olderThanA, false, true,
                    PathAnalysis.SpousePosition.AT_END);
        }

        // SPOUSE ở đầu: B là họ hàng của vợ/chồng A
        // A↔Spouse→...→B: đếm steps từ Spouse đến B
        if (spouseAtStart) {
            PathAnalysis blood = analyzeBloodPath(graph,
                    path.subList(1, path.size()), olderThanA,
                    PathAnalysis.SpousePosition.AT_START);
            return new PathAnalysis(
                    blood.stepsUp(), blood.stepsDown(),
                    blood.stepsUp() - blood.stepsDown(),
                    blood.throughFatherUp(), blood.throughFatherDown(),
                    olderThanA, false, true,
                    PathAnalysis.SpousePosition.AT_START);
        }

        // SPOUSE ở giữa: thông gia 2 họ
        // Phần A: từ A đến X (người kết hôn phía A)
        PathAnalysis partA = analyzeBloodPath(graph,
                path.subList(0, spouseIndex + 1), olderThanA,
                PathAnalysis.SpousePosition.IN_MIDDLE);
        // Phần B: từ Y (người kết hôn phía B) đến B — nhưng đây là góc nhìn ngược
        PathAnalysis partB = analyzeBloodPath(graph,
                path.subList(spouseIndex + 1, path.size()), olderThanA,
                PathAnalysis.SpousePosition.IN_MIDDLE);

        // Từ góc nhìn của A: phần B bị đảo chiều
        int totalStepsUp   = partA.stepsUp()   + partB.stepsDown();
        int totalStepsDown = partA.stepsDown() + partB.stepsUp();

        return new PathAnalysis(
                totalStepsUp, totalStepsDown,
                totalStepsUp - totalStepsDown,
                partA.throughFatherUp(), partB.throughFatherUp(),
                olderThanA, false, true,
                PathAnalysis.SpousePosition.IN_MIDDLE);
    }

    // Helper: phân tích đoạn path thuần huyết thống
    private PathAnalysis analyzeBloodPath(
            Map<UUID, List<GraphEdge>> graph,
            List<UUID> path,
            boolean olderThanA,
            PathAnalysis.SpousePosition pos
    ) {
        int stepsUp = 0, stepsDown = 0;
        boolean goingUp = true;
        boolean throughFatherUp = true;
        boolean throughFatherDown = true;

        for (int i = 0; i < path.size() - 1; i++) {
            UUID cur  = path.get(i);
            UUID next = path.get(i + 1);
            EdgeType et = getEdgeType(graph, cur, next);

            if (et == EdgeType.PARENT) {
                stepsUp++;
                if (stepsUp == 1 && stepsDown == 0) {
                    // Bước lên đầu tiên — xác định nội/ngoại
                    Person nextP = personRepository.findById(next).orElse(null);
                    if (nextP != null) throughFatherUp = nextP.getGender().equals(Gender.MALE);
                }
            } else if (et == EdgeType.CHILD) {
                if (goingUp) {
                    goingUp = false;
                    // Tại LCA — xác định nội/ngoại phía B
                    Person lcaP = personRepository.findById(cur).orElse(null);
                    if (lcaP != null) throughFatherDown = lcaP.getGender().equals(Gender.MALE);
                }
                stepsDown++;
            }
        }

        return new PathAnalysis(stepsUp, stepsDown, stepsUp - stepsDown,
                throughFatherUp, throughFatherDown,
                olderThanA, false, false, pos);
    }


    private EdgeType getEdgeType(Map<UUID, List<GraphEdge>> graph, UUID from, UUID to) {
        return graph.getOrDefault(from, Collections.emptyList())
                .stream()
                .filter(e -> e.relatedPersonId().equals(to))
                .map(GraphEdge::type)
                .findFirst()
                .orElse(null);
    }

    // ── Resolve tên ───────────────────────────────────────────────────────────
    private String resolveRelationship(PathAnalysis analysis, short genderB, short genderA, boolean reversed) {
        if (analysis.isInLawRelation()) {
            return switch (analysis.spousePosition()) {
                case AT_END ->
                    // B là vợ/chồng của người cách A [stepsDown] bước
                        resolver.resolveDaughterInLaw(
                                genderB, genderA,
                                analysis.stepsDown(), analysis.stepsUp());
                case AT_START ->
                    // B là họ hàng của vợ/chồng A (nhìn từ phía vợ/chồng A)
                        resolver.resolveSpouseRelative(
                                genderB, genderA,
                                analysis.stepsUp(), analysis.stepsDown());
                case IN_MIDDLE ->
                    // Thông gia thực sự
                        resolver.resolveInLaw(
                                genderB, genderA,
                                analysis.stepsUp(), analysis.stepsDown());
                default -> "Họ hàng";
            };
        }
        return resolver.resolve(
                genderB,
                analysis.stepsUp(), analysis.stepsDown(),
                analysis.throughFatherUp(), analysis.throughFatherDown(),
                analysis.olderThanA(), analysis.isSpouseRelation(),
                genderA);
    }

    // ── Build path nodes ──────────────────────────────────────────────────────
    private List<PersonNode> buildPathNodes(List<UUID> path, Map<UUID, List<GraphEdge>> graph) {
        List<PersonNode> nodes = new ArrayList<>();
        List<Person> persons = personRepository.findAllById(path);
        Map<UUID, Person> personMap = new HashMap<>();
        persons.forEach(p -> personMap.put(p.getId(), p));

        for (int i = 0; i < path.size(); i++) {
            UUID id = path.get(i);
            Person p = personMap.get(id);
            if (p == null) continue;

            String relation = null;
            if (i > 0) {
                EdgeType edgeType = getEdgeType(graph, path.get(i - 1), id);
                relation = edgeTypeToLabel(edgeType, p.getGender().equals(Gender.MALE)? Short.valueOf("1") : Short.valueOf("2"));
            }

            nodes.add(toNode(p, relation));
        }

        return nodes;
    }

    private String edgeTypeToLabel(EdgeType edgeType, short gender) {
        if (edgeType == null) return null;
        boolean male = gender == 1;
        return switch (edgeType) {
            case PARENT -> male ? "Cha" : "Mẹ";
            case CHILD -> male ? "Con trai" : "Con gái";
            case SPOUSE -> male ? "Chồng" : "Vợ";
        };
    }

    private PersonNode toNode(Person p, String relation) {
        return PersonNode.builder()
                .id(p.getId())
                .fullName(p.getFirstName() + " " + p.getLastName())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .avatarUrl(p.getAvatarUrl())
                .gender(p.getGender().equals(Gender.MALE)? Short.valueOf("1") : Short.valueOf("2"))
                .relation(relation)
                .build();
    }
}