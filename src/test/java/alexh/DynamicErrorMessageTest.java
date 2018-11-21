package alexh;

import static alexh.DynamicCollectionTest.hashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import alexh.weak.Dynamic;
import alexh.weak.Weak;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DynamicErrorMessageTest {

    private static String noSuchElementErrorMessage(Runnable runnable) {
        try  { runnable.run(); }
        catch (NoSuchElementException e) { return e.getMessage(); }
        throw new AssertionError("Runnable did not error as expected");
    }

    private static String classCastErrorMessage(Runnable runnable) {
        try  { runnable.run(); }
        catch (ClassCastException e) { return e.getMessage(); }
        throw new AssertionError("Runnable did not error as expected");
    }

    private Dynamic dy;

    @BeforeEach
    public void setupMap() {
        dy = Dynamic.from(new Fluent.HashMap<>()
            .append("key1", new Fluent.HashMap<>()
                .append("key2", "hello")
                .append("key3", new Fluent.HashMap<>()
                    .append("key4", 123)
                    .append("key6", null)
                    .append("key7", emptyMap())))
            .append("key5", asList(1, 2, 3, 4))
            .append("key8", emptyList())
            .append("key9", asList("hello", null, null, null))
            .append("key10", hashSet(null, "set-value"))
            .append("key11", new LinkedBlockingDeque<>()));
        assertNotNull(dy);
    }

    @Test
    public void message_topLevelMissing() {
        String message = noSuchElementErrorMessage(() -> dy.get("foo").asObject());
        assertThat(message).as("message for top level child missing")
            .contains("foo")
            .containsIgnoringCase("missing")
            .contains("root->*foo*")
            // available keys
            .contains("key1")
            .contains("key5");
        System.out.println(message);
    }

    @Test
    public void message_nestedMissingFromMap() {
        String message = noSuchElementErrorMessage(() -> dy.get("key1").get("key3").get("bar").asObject());

        assertThat(message).as("message for a child missing from a nested Map")
            .contains("bar")
            .containsIgnoringCase("missing")
            .contains("key1->key3->*bar*")

            .as("message with available keys")
            .contains("key4", "key7", "key6")

            .as("message with type")
            .contains("Map");
        System.out.println(message);
    }

    @Test
    public void message_nestedMissingLongChain() {
        String message = noSuchElementErrorMessage(() -> dy.get("key1").get("key3").get("barrr").get("A")
            .get("B").get("C").get("D").get("E").get("F").get("G").get("H").get("I").get("J")
            .get("K").get("L").get("M").get("N").get("O").get("P").get("Q").get("R").get("S")
            .get("T").get("U").get("V").get("W").get("X").get("Y").get("Z").asObject());

        assertThat(message).as("message for long chain of missing children")
            .contains("barrr")
            .contains("key1->key3->*barrr*->A->B")
            .contains("Map");
        System.out.println(message);
    }

    @Test
    public void message_nestedMissingFromObject() {
        String message = noSuchElementErrorMessage(() -> dy.get("key1").get("key3").get("key4").get("blah").get("bar").asObject());

        assertThat(message).as("message for missing child from Object")
            .contains("'key4'")
            .contains("premature end")
            .contains("key1->key3->*key4*->blah->bar")
            .contains("Integer");
        System.out.println(message);
    }

    @Test
    public void message_nestedMapNull() {
        String message = noSuchElementErrorMessage(() -> dy.get("key1").get("key3").get("key6").get("key7").get("key8").asObject());

        assertThat(message).as("message for a nested null within a Map")
            .contains("'key6'")
            .contains("premature end")
            .contains("key1->key3->*key6*->key7->key8")
            .contains("null");
        System.out.println(message);
    }

    @Test
    public void message_nestedListNull() {
        String message = noSuchElementErrorMessage(() -> dy.get("key9").get(3).asObject());

        assertThat(message).as("message for a null inside a nested List")
            .contains("'3'")
            .contains("premature end")
            .contains("key9->*3*")
            .contains("null");
        System.out.println(message);
    }

    @Test
    public void message_nestedListNullChildren() {
        String message = noSuchElementErrorMessage(() -> dy.get("key9").get(3).get("foo").get("bar").asObject());

        assertThat(message).as("message for a child of a nested list null-value")
            .contains("'3'")
            .contains("premature end")
            .contains("key9->*3*->foo->bar")
            .contains("null");
        System.out.println(message);
    }

    @Test
    public void message_nestedEmptyMap() {
        String message = noSuchElementErrorMessage(() -> dy.get("key1").get("key3").get("key7").get("key8").asObject());

        assertThat(message).as("message for a child of a nested empty map 'key7'")
            .contains("'key7'")
            .contains("premature end")
            .contains("key1->key3->*key7*->key8")
            .containsIgnoringCase("empty-map");
        System.out.println(message);
    }

    @Test
    public void message_nestedListArrayOutOfBounds() {
        String message = noSuchElementErrorMessage(() -> dy.get("key5").get(4).asObject());

        assertThat(message).as("message for child of Integer at index 4 of nested list 'key5'")
            .contains("'4'")
            .contains("missing")
            .contains("key5->*4*")
            .contains("List")
            .contains("0..3");
        System.out.println(message);
    }

    @Test
    public void message_nestedListNonIntegerGet() {
        String message = noSuchElementErrorMessage(() -> dy.get("key5").get("foo").asObject());

        assertThat(message).as("message for a nested list 'key5' non numeric get")
            .contains("'foo'")
            .contains("missing")
            .contains("key5->*foo*")
            .contains("List")
            .contains("0..3");
        System.out.println(message);
    }

    @Test
    public void message_nestedEmptyList() {
        String message = noSuchElementErrorMessage(() -> dy.get("key8").get(4).get("bar").asObject());

        assertThat(message).as("message for getting index 4 of nested empty list 'key8'")
            .contains("'key8'", "premature end", "*key8*->4->bar")
            .containsIgnoringCase("empty-list");
        System.out.println(message);
    }

    @Test
    public void message_dynamicFromNull() {
        String message = noSuchElementErrorMessage(() -> Dynamic.from(null).asObject());

        assertThat(message).as("message for casting assertion null -> object")
            .contains("premature end", "root", "null");
        System.out.println(message);
    }

    @Test
    public void message_dynamicFromNullNested() {
        String message = noSuchElementErrorMessage(() -> Dynamic.from(null).get("foo").get("bar").get(33).asObject());

        assertThat(message).as("message for chain of gets on a null root")
            .contains("'root'", "premature end", "*root*->foo->bar->33", "null");
        System.out.println(message);
    }

    @Test
    public void message_nestedCollectionGet() {
        String message = noSuchElementErrorMessage(() -> dy.get("key10").get(33).asObject());

        assertThat(message).as("message for get from Collection 'key10'")
            .contains("'33'")
            .contains("missing")
            .contains("key10->*33*")
            .contains("Set")
            .contains("size:2");
        System.out.println(message);
    }

    @Test
    public void message_nestedEmptyCollection() {
        String message = noSuchElementErrorMessage(() -> dy.get("key11").get("foo").asObject());

        assertThat(message).as("message for get from empty collection 'key11'")
            .contains("'key11'", "premature end", "*key11*->foo")
            .containsIgnoringCase("Empty-Collection");
        System.out.println(message);
    }

    @Test
    public void miscastRoot() {
        String message = classCastErrorMessage(() -> dy.as(Long.class));

        assertThat(message).as("message for get from miscast root -> Long")
            .contains("'root'", "miscast", "alexh.Fluent$HashMap", "java.lang.Long")
            .contains("Avoid by checking `if (aDynamic.is(Long.class)) ...` or using `aDynamic.maybe().as(Long.class)`");
        System.out.println(message);
    }

    @Test
    public void miscastChild() {
        String message = classCastErrorMessage(() -> dy.dget("key1.key2").asList());

        assertThat(message).as("message for get from miscast key2 -> List")
            .contains("'key2'", "miscast", "root->key1->*key2*", "java.lang.String", "java.util.List")
            .contains("Avoid by checking `if (aDynamic.is(List.class)) ...` or using `aDynamic.maybe().as(List.class)`");
        System.out.println(message);
    }

    @Test
    public void miscastWeak() {
        Weak weak = new Weak() {
            @Override
            public boolean isPresent() {
                return true;
            }
            @Override
            public Object asObject() {
                return "some string";
            }
        };

        String message = classCastErrorMessage(() -> weak.as(Integer.class));
        assertThat(message).as("basic weak message for miscasting")
            .contains("java.lang.String", "java.lang.Integer");
        System.out.println(message);
    }
}
