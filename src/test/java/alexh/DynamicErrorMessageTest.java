package alexh;

import alexh.weak.Dynamic;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DynamicErrorMessageTest {

    private static String errorMessage(Runnable runnable) {
        try  { runnable.run(); }
        catch (NoSuchElementException e) { return e.getMessage(); }
        throw new AssertionError("Runnable did not error as expected");
    }

    private Dynamic dy;

    @Before
    public void setupMap() {
        dy = Dynamic.from(new HashMap() {{
            put("key1", new HashMap() {{
                put("key2", "hello");
                put("key3", new HashMap() {{
                    put("key4", 123);
                    put("key6", null);
                    put("key7", emptyMap());
                }});
            }});
            put("key5", asList(1, 2, 3, 4));
            put("key8", emptyList());
            put("key9", asList("hello", null, null, null));
        }});
        assertNotNull(dy);
    }

    @Test
    public void message_topLevelMissing() {
        String message = errorMessage(() -> dy.get("foo").asObject());
        assertThat("Should contain the key name", message, containsString("foo"));
        assertThat("Should describe key as missing", message.toLowerCase(), containsString("missing"));
        assertThat("Should describe the key location", message, containsString("root->*foo*"));
        assertThat("Should describe the available keys", message, allOf(containsString("key1"), containsString("key5")));
        System.out.println(message);
    }

    @Test
    public void message_nestedMissingFromMap() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("bar").asObject());
        assertThat("Should contain the key name", message, containsString("bar"));
        assertThat("Should describe key as missing", message.toLowerCase(), containsString("missing"));
        assertThat("Should describe the key location", message, containsString("key1->key3->*bar*"));
        assertThat("Should describe the available keys", message,
            allOf(containsString("key4"), containsString("key6"), containsString("key7")));
        assertThat("Should describe the type that was missing the key", message.toLowerCase(), containsString("map"));
        System.out.println(message);
    }

    @Test
    public void message_nestedMissingLongChain() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("barrr").get("A")
            .get("B").get("C").get("D").get("E").get("F").get("G").get("H").get("I").get("J")
            .get("K").get("L").get("M").get("N").get("O").get("P").get("Q").get("R").get("S")
            .get("T").get("U").get("V").get("W").get("X").get("Y").get("Z").asObject());
        assertThat("Should contain the key name", message, containsString("barrr"));
        assertThat("Should describe the key location", message, containsString("key1->key3->*barrr*->A->B"));
        assertThat("Should describe the type that was missing the key", message.toLowerCase(), containsString("map"));
        System.out.println(message);
    }

    @Test
    public void message_nestedMissingFromObject() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("key4").get("blah").get("bar").asObject());
        assertThat("Should contain the key name", message, containsString("'key4'"));
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("key1->key3->*key4*->blah->bar"));
        assertThat("Should describe the non-map type", message, containsString("Integer"));
        System.out.println(message);
    }

    @Test
    public void message_nestedMapNull() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("key6").get("key7").get("key8").asObject());
        assertThat("Should contain the key name", message, containsString("'key6'"));
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("key1->key3->*key6*->key7->key8"));
        assertThat("Should describe the emptiness", message.toLowerCase(), containsString("null"));
        System.out.println(message);
    }

    @Test
    public void message_nestedListNull() {
        String message = errorMessage(() -> dy.get("key9").get(3).asObject());
        assertThat("Should contain the key name", message, containsString("'3'"));
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("key9->*3*"));
        assertThat("Should describe the emptiness", message.toLowerCase(), containsString("null"));
        System.out.println(message);
    }

    @Test
    public void message_nestedListNullChildren() {
        String message = errorMessage(() -> dy.get("key9").get(3).get("foo").get("bar").asObject());
        assertThat("Should contain the key name", message, containsString("'3'"));
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("key9->*3*->foo->bar"));
        assertThat("Should describe the emptiness", message.toLowerCase(), containsString("null"));
        System.out.println(message);
    }

    @Test
    public void message_nestedEmptyMap() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("key7").get("key8").asObject());
        assertThat("Should contain the key name", message, containsString("'key7'"));
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("key1->key3->*key7*->key8"));
        assertThat("Should describe the emptiness", message.toLowerCase(), containsString("empty map"));
        System.out.println(message);
    }

    @Test
    public void message_nestedListArrayOutOfBounds() {
        String message = errorMessage(() -> dy.get("key5").get(4).asObject());
        assertThat("Should contain the key name", message, containsString("'4'"));
        assertThat("Should describe key as missing", message.toLowerCase(), containsString("missing"));
        assertThat("Should describe the key location", message, containsString("key5->*4*"));
        assertThat("Should describe the type that was missing the key", message.toLowerCase(), containsString("list"));
        assertThat("Should describe the available keys", message, containsString("0..3"));
        System.out.println(message);
    }

    @Test
    public void message_nestedListNonIntegerGet() {
        String message = errorMessage(() -> dy.get("key5").get("foo").asObject());
        assertThat("Should contain the key name", message, containsString("'foo'"));
        assertThat("Should describe key as missing", message.toLowerCase(), containsString("missing"));
        assertThat("Should describe the key location", message, containsString("key5->*foo*"));
        assertThat("Should describe the type that was missing the key", message.toLowerCase(), containsString("list"));
        assertThat("Should describe the available keys", message, containsString("0..3"));
        System.out.println(message);
    }

    @Test
    public void message_nestedEmptyList() {
        String message = errorMessage(() -> dy.get("key8").get(4).get("bar").asObject());
        assertThat("Should contain the key name", message, containsString("'key8'"));
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("*key8*->4->bar"));
        assertThat("Should describe the emptiness", message.toLowerCase(), containsString("empty list"));
        System.out.println(message);
    }

    @Test
    public void message_dynamicFromNull() {
        String message = errorMessage(() -> Dynamic.from(null).asObject());
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("root"));
        assertThat("Should describe the emptiness", message.toLowerCase(), containsString("null"));
        System.out.println(message);
    }

    @Test
    public void message_dynamicFromNullNested() {
        String message = errorMessage(() -> Dynamic.from(null).get("foo").get("bar").get(33).asObject());
        assertThat("Should contain the key name", message.toLowerCase(), containsString("'root'"));
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("*root*->foo->bar->33"));
        assertThat("Should describe the emptiness", message.toLowerCase(), containsString("null"));
        System.out.println(message);
    }
}
