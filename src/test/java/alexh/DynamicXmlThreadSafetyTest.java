package alexh;

import alexh.weak.Dynamic;
import alexh.weak.Weak;
import alexh.weak.XmlDynamic;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;

/**
 * Created by tp50217 on 23/06/2016.
 */
public class DynamicXmlThreadSafetyTest {

    private static final int TEST_CONCURRENCY = 100;
    private ExecutorService exe;

    @Before
    public void setup() {
        exe = Executors.newCachedThreadPool();
    }

    @After
    public void shutdown() {
        exe.shutdownNow();
    }

    @Test
    public void construction() {
        List<CompletableFuture<?>> results = IntStream.range(0, TEST_CONCURRENCY)
            .mapToObj(j -> CompletableFuture.supplyAsync(() -> {
                try {
                    return new XmlDynamic(DynamicXmlTest.XML);
                }
                catch (RuntimeException ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }, exe))
            .collect(toList());

        long errorCount = results.stream()
            .filter(CompletableFuture::isCompletedExceptionally)
            .count();

        assertTrue(errorCount + "/" + TEST_CONCURRENCY + " call(s) were exceptional", errorCount == 0);
    }

    @Test
    public void children() {
        Dynamic xml = new XmlDynamic("<xml><el att=\"123\" att2=\"hello\">234</el><el att=\"345\" att2=\"blah\">456</el></xml>").get("xml");

        List<CompletableFuture<?>> results = IntStream.range(0, TEST_CONCURRENCY)
            .mapToObj(j -> CompletableFuture.supplyAsync(() -> {
                try {
                    xml.children().forEach(el -> {
                        el.asObject(); // all children are always present, ie no exception
                        el.children().forEach(Weak::asObject); // so are the attributes
                    });
                    return null;
                }
                catch (RuntimeException ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }, exe))
            .collect(toList());

        long errorCount = results.stream()
            .filter(CompletableFuture::isCompletedExceptionally)
            .count();

        assertTrue(errorCount + "/" + TEST_CONCURRENCY + " call(s) were exceptional", errorCount == 0);
    }
}
