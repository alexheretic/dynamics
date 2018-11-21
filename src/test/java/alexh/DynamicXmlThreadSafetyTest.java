package alexh;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import alexh.weak.Dynamic;
import alexh.weak.XmlDynamic;
import com.google.common.collect.ImmutableMultimap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DynamicXmlThreadSafetyTest {

    private static int TEST_CONCURRENCY = 200;
    private static final String XML =
        "<xml xmlns:s=\"http://example.com/rootspace/something\">" +
            "<el att2=\"hello\" s:att=\"123\">234</el>" +
            "<s:el att=\"345\" att2=\"blah\">456</s:el>" +
        "</xml>";

    /** key to #asObject() value expectation */
    private static final ImmutableMultimap<Object, String> expectedValues = ImmutableMultimap.<Object, String>builder()
        .put("xml", "<el att2=\"hello\" xmlns:s=\"http://example.com/rootspace/something\" s:att=\"123\">234</el>" +
            "<s:el xmlns:s=\"http://example.com/rootspace/something\" att=\"345\" att2=\"blah\">456</s:el>")
        .put("@s", "http://example.com/rootspace/something")
        .put("el", "234")
        .put("@att", "123")
        .put("@att2", "hello")
        .put("el[1]", "456")
        .put("@att", "345")
        .put("@att2", "blah")
        .build();

    private ExecutorService exe;

    @BeforeEach
    public void setup() {
        exe = Executors.newCachedThreadPool();
    }

    @AfterEach
    public void shutdown() {
        exe.shutdownNow();
    }

    /** Construction of a XmlDynamic from a string should be thread-safe */
    @Test
    public void construction() {
        List<CompletableFuture<RuntimeException>> results = IntStream.range(0, TEST_CONCURRENCY)
            .mapToObj(j -> CompletableFuture.supplyAsync(() -> {
                try {
                    new XmlDynamic(XML);
                    return null;
                }
                catch (RuntimeException ex) {
                    return ex;
                }
            }, exe))
            .collect(toList());

        List<RuntimeException> errors = results.stream()
            .map(CompletableFuture::join)
            .filter(ex -> ex != null)
            .collect(toList());

        if (!errors.isEmpty())
            errors.get(0).printStackTrace();
        assertTrue(errors.isEmpty(), errors.size() + "/" + TEST_CONCURRENCY + " call(s) were exceptional");
    }

    /** All read access to an XmlDynamic should be thread-safe */
    @Test
    public void children() throws Throwable {
        final XmlDynamic xml = new XmlDynamic(XML);

        List<CompletableFuture<Throwable>> results = IntStream.range(0, TEST_CONCURRENCY)
            .mapToObj(iteration -> CompletableFuture.supplyAsync(() -> {
                try {
                    Object rootKey = xml.key().asObject();
                    Object rootVal = xml.asObject();

                    assertThat(rootKey).isEqualTo("root");
                    assertThat(rootVal).isEqualTo(XML);
                    callAllXmlDynamicMethodsOn(xml);

                    // try both orders of children streaming
                    Stream<Dynamic> allChildren = iteration % 2 == 0 ? xml.allChildrenDepthFirst() :
                        xml.allChildrenBreadthFirst();

                    long count = allChildren.map(child -> {
                        // all children & keys are always present, ie no exception
                        Object val = child.asObject();
                        Object key = child.key().asObject();

                        assertThat(key)
                            .as("child.key().asObject()")
                            .isIn(expectedValues.keySet());
                        assertThat(val)
                            .as("child.asObject() (for '" + key + "')")
                            .isIn(expectedValues.get(key));

                        callAllXmlDynamicMethodsOn(child);
                        return 1;
                    }).count();

                    assertThat(count).isEqualTo(8);
                    return null;
                }
                catch (Throwable ex) { return ex; }
            }, exe))
            .collect(toList());

        List<Throwable> errors = results.stream()
            .map(CompletableFuture::join)
            .filter(r -> r != null)
            .collect(toList());

        if (!errors.isEmpty()) {
            System.err.println(errors.size() + "/" + TEST_CONCURRENCY + " call(s) were exceptional");
            throw errors.get(0);
        }
    }

    private void callAllXmlDynamicMethodsOn(Dynamic d) {
        XmlDynamic xml = (XmlDynamic) d;
        xml.describe();
        xml.toString();
        xml.fullXml();
        xml.hashCode();
    }

    /** Manual use to try and force rare race conditions */
//    @Test
    public void longTest() throws Throwable {
        TEST_CONCURRENCY = 5000;

        final Duration runAtLeast = Duration.ofMinutes(5);
        final LocalDateTime before = LocalDateTime.now();

        while (Duration.between(before, LocalDateTime.now()).compareTo(runAtLeast) < 0) {
            construction();
            children();
        }
    }
}
