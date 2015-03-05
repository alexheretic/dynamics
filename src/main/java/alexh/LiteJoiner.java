package alexh;

import java.util.Iterator;

/** Poor mans com.google.common.base.Joiner, a simple non-null handling replacement to eliminate Guava dependency */
public class LiteJoiner {

    public static LiteJoiner on(String separator) {
        return new LiteJoiner(separator);
    }

    private final String separator;

    private LiteJoiner(String separator) {
        this.separator = separator;
    }

    public String join(Iterable<?> parts) {
        final StringBuilder joined = new StringBuilder();
        final Iterator<?> partIterator = parts.iterator();
        while (partIterator.hasNext()) {
            joined.append(partIterator.next());
            if (partIterator.hasNext()) joined.append(separator);
        }
        return joined.toString();
    }
}
