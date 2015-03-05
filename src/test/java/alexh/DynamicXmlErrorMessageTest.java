package alexh;

import alexh.weak.Dynamic;
import org.junit.Before;
import org.junit.Test;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DynamicXmlErrorMessageTest {

    private static String errorMessage(Runnable runnable) {
        try  { runnable.run(); }
        catch (NoSuchElementException e) { return e.getMessage(); }
        throw new AssertionError("Runnable did not error as expected");
    }

    private Dynamic dy;

    @Before
    public void setupMap() {
        dy = Dynamic.fromXml(
            "<msg>" +
              "<key1>" +
                "<key2>hello</key2>" +
                "<key3>" +
                  "<key4>123</key4>" +
                  "<key6/>" +
                  "<key7></key7>" +
                "</key3>" +
              "</key1>" +
              "<key5>1</key5>" +
              "<key5>2</key5>" +
              "<key5>3</key5>" +
              "<key5>4</key5>" +
              "<key9>hello</key9>" +
              "<key9/>" +
              "<key9/>" +
              "<key9></key9>" +
            "</msg>"
        ).get("msg");

        assertNotNull(dy);
    }

    @Test
    public void message_topLevelMissing() {
        String message = errorMessage(() -> dy.get("foo").asObject());
        assertThat("Should contain the key name", message, containsString("foo"));
        assertThat("Should describe key as missing", message.toLowerCase(), containsString("missing"));
        assertThat("Should describe the key location", message, containsString("msg->*foo*"));
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
        assertThat("Should describe the type that was missing the key", message.toLowerCase(), containsString("xml"));
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
        assertThat("Should describe the type that was missing the key", message.toLowerCase(), containsString("xml"));
        System.out.println(message);
    }

    @Test
    public void message_nestedEmptyMap() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("key7").get("key8").asObject());
        assertThat("Should contain the key name", message, containsString("'key7'"));
        assertThat("Should describe key as a premature end", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("key1->key3->*key7*->key8"));
        assertThat("Should describe the emptiness", message.toLowerCase(), containsString("empty xml"));
        System.out.println(message);
    }

    @Test
    public void message_nestedListArrayOutOfBounds() {
        String message = errorMessage(() -> dy.get("key5[4]").asObject());
        assertThat("Should contain the key name", message, containsString("'key5[4]'"));
        assertThat("Should describe key as missing", message.toLowerCase(), containsString("missing"));
        assertThat("Should describe the key location", message, containsString("*key5[4]*"));
        assertThat("Should describe the type that was missing the key", message.toLowerCase(), containsString("xml"));
        assertThat("Should describe the available keys", message, containsString("key5[0..3]"));
        System.out.println(message);
    }

    @Test
    public void message_nestedPrematureEndPointGet() {
        String message = errorMessage(() -> dy.get("key5[1]").get("foo").asObject());
        assertThat("Should contain the key name", message, containsString("'key5[1]'"));
        assertThat("Should describe key as missing", message.toLowerCase(), containsString("premature end"));
        assertThat("Should describe the key location", message, containsString("*key5[1]*->foo"));
        assertThat("Should describe the end point", message.toLowerCase(), containsString("xml"));
        System.out.println(message);
    }
}
