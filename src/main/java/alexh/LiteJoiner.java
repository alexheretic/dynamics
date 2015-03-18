/*
 * Copyright 2015 Alex Butler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
