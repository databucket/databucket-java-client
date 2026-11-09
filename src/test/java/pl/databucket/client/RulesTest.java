package pl.databucket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RulesTest {

    private final ObjectMapper mapper = Databucket.defaultMapper();

    private String json(Rules rules) throws Exception {
        return mapper.writeValueAsString(rules.toBody());
    }

    @Test
    void oneRuleIsSentAsLogicWithoutAnd() throws Exception {
        assertEquals("{\"logic\":{\"==\":[{\"var\":\"$.city\"},\"Paris\"]}}",
                json(Rules.where(Rule.property("$.city").eq("Paris"))));
    }

    @Test
    void propertyWithoutPrefixGetsIt() throws Exception {
        assertEquals("{\"logic\":{\"==\":[{\"var\":\"$.address.city\"},\"Paris\"]}}",
                json(Rules.where(Rule.property("address.city").eq("Paris"))));
    }

    @Test
    void severalRulesAreCombinedWithAnd() throws Exception {
        assertEquals("{\"logic\":{\"and\":[{\">\":[{\"var\":\"$.age\"},30]},{\"in\":[{\"var\":\"tagId\"},[1,2]]}]}}",
                json(Rules.where(Rule.property("$.age").gt(30), Rule.tagId().in(1, 2))));
    }

    @Test
    void groupsNegationAndNull() throws Exception {
        Rules rules = Rules.where(Rule.or(Rule.owner().isNull(), Rule.owner().eq(Rule.CURRENT_USER)),
                Rule.not(Rule.property("$.status").like("del%")));
        assertEquals("{\"logic\":{\"and\":[{\"or\":[{\"is\":[{\"var\":\"owner\"},null]},"
                        + "{\"==\":[{\"var\":\"owner\"},\"@currentUser\"]}]},"
                        + "{\"!\":{\"like\":[{\"var\":\"$.status\"},\"del%\"]}}]}}",
                json(rules));
    }

    @Test
    void betweenEmptyAndFunctions() throws Exception {
        assertEquals("{\"logic\":{\"<=\":[1,{\"var\":\"$.n\"},5]}}", json(Rules.where(Rule.property("$.n").between(1, 5))));
        assertEquals("{\"logic\":{\"!\":{\"var\":\"$.email\"}}}", json(Rules.where(Rule.property("$.email").isEmpty())));
        assertEquals("{\"logic\":{\"!!\":{\"var\":\"owner\"}}}", json(Rules.where(Rule.owner().isNotEmpty())));
        assertEquals("{\"logic\":{\">\":[{\"var\":\"$.orders.length()\"},2]}}",
                json(Rules.where(Rule.property("$.orders").length().gt(2))));
        assertEquals("{\"logic\":{\"==\":[{\"var\":\"$.tmp.exists()\"},true]}}",
                json(Rules.where(Rule.property("$.tmp").exists())));
    }

    @Test
    void instantIsSentAsIsoText() throws Exception {
        assertEquals("{\"logic\":{\">\":[{\"var\":\"createdAt\"},\"2026-01-02T03:04:05Z\"]}}",
                json(Rules.where(Rule.field(Field.CREATED_AT).gt(Instant.parse("2026-01-02T03:04:05Z")))));
    }

    @Test
    void functionsOnlyOnProperties() {
        assertThrows(IllegalStateException.class, () -> Rule.owner().exists());
    }

    @Test
    void rawRulesAndLogicPassThrough() throws Exception {
        assertEquals("{\"rules\":[[\"$.city\",\"==\",\"Paris\"]]}",
                json(Rules.fromRules(List.of(List.of("$.city", "==", "Paris")))));
        assertEquals("{\"logic\":{\"==\":[{\"var\":\"id\"},1]}}",
                json(Rules.fromLogic(Map.of("==", List.of(Map.of("var", "id"), 1)))));
    }

    @Test
    void allIsEmpty() throws Exception {
        assertTrue(Rules.ALL.isEmpty());
        assertTrue(Rules.where().isEmpty());
        assertEquals("{}", json(Rules.ALL));
    }

    @Test
    void changeBody() throws Exception {
        assertEquals("{\"tagId\":4,\"reserved\":false,\"propertiesToSet\":{\"$.a.b\":1},\"propertiesToRemove\":[\"$.tmp\"]}",
                mapper.writeValueAsString(Change.create().tagId(4L).reserved(false).set("a.b", 1).remove("$.tmp").toBody()));
    }

    @Test
    void sort() {
        assertEquals("desc(id)", Sort.desc(Field.ID));
        assertEquals("asc($.name)", Sort.asc("$.name"));
    }
}
