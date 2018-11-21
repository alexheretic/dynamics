package alexh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import alexh.weak.Dynamic;
import alexh.weak.XmlDynamic;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DynamicXmlErrorMessageTest {

    private static String errorMessage(Runnable runnable) {
        try  { runnable.run(); }
        catch (NoSuchElementException e) { return e.getMessage(); }
        throw new AssertionError("Runnable did not error as expected");
    }

    private Dynamic dy;

    @BeforeEach
    public void setupMap() {
        dy = new XmlDynamic(
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
        assertThat(message).as("Should contain the key name").contains("foo");
        assertThat(message).as("Should describe key as missing").containsIgnoringCase("missing");
        assertThat(message).as("Should describe the key location").contains("msg->*foo*");
        assertThat(message).as("Should describe the available keys").contains("key1").contains("key5");
        System.out.println(message);
    }

    @Test
    public void message_nestedMissingFromMap() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("bar").asObject());
        assertThat(message).as("Should contain the key name").contains("bar");
        assertThat(message.toLowerCase()).as("Should describe key as missing").contains("missing");
        assertThat(message).as("Should describe the key location").contains("key1->key3->*bar*");
        assertThat(message)
            .as("Should describe the available keys")
            .contains("key4")
            .contains("key6")
            .contains("key7");
        assertThat(message.toLowerCase()).as("Should describe the type that was missing the key").contains("xml");
        System.out.println(message);
    }

    @Test
    public void message_nestedMissingLongChain() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("barrr").get("A")
            .get("B").get("C").get("D").get("E").get("F").get("G").get("H").get("I").get("J")
            .get("K").get("L").get("M").get("N").get("O").get("P").get("Q").get("R").get("S")
            .get("T").get("U").get("V").get("W").get("X").get("Y").get("Z").asObject());
        assertThat(message).as("Should contain the key name").contains("barrr");
        assertThat(message).as("Should describe the key location").contains("key1->key3->*barrr*->A->B");
        assertThat(message.toLowerCase()).as("Should describe the type that was missing the key").contains("xml");
        System.out.println(message);
    }

    @Test
    public void message_nestedEmptyMap() {
        String message = errorMessage(() -> dy.get("key1").get("key3").get("key7").get("key8").asObject());
        assertThat(message).as("Should contain the key name").contains("'key7'");
        assertThat(message.toLowerCase()).as("Should describe key as a premature end").contains("premature end");
        assertThat(message).as("Should describe the key location").contains("key1->key3->*key7*->key8");
        assertThat(message.toLowerCase()).as("Should describe the emptiness").contains("empty-xml");
        System.out.println(message);
    }

    @Test
    public void message_nestedListArrayOutOfBounds() {
        String message = errorMessage(() -> dy.get("key5[4]").asObject());
        assertThat(message).as("Should contain the key name").contains("'key5[4]'");
        assertThat(message.toLowerCase()).as("Should describe key as missing").contains("missing");
        assertThat(message).as("Should describe the key location").contains("*key5[4]*");
        assertThat(message.toLowerCase()).as("Should describe the type that was missing the key").contains("xml");
        assertThat(message).as("Should describe the available keys").contains("key5[0..3]");
        System.out.println(message);
    }

    @Test
    public void message_nestedPrematureEndPointGet() {
        String message = errorMessage(() -> dy.get("key5[1]").get("foo").asObject());
        assertThat(message).as("Should contain the key name").contains("'key5[1]'");
        assertThat(message.toLowerCase()).as("Should describe key as missing").contains("premature end");
        assertThat(message).as("Should describe the key location").contains("*key5[1]*->foo");
        assertThat(message.toLowerCase()).as("Should describe the end point").contains("xml");
        System.out.println(message);
    }
}
