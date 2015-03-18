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
package alexh.weak;

import alexh.LiteJoiner;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static alexh.weak.DynamicChildLogic.using;
import static java.lang.String.format;

class DynamicList extends AbstractDynamic<List> implements Dynamic, TypeDescriber, AvailabilityDescriber {

    public DynamicList(List inner) {
        super(inner);
    }

    /** @return nullable */
    private Integer keyToIndex(Object key) {
        Integer index = null;

        if (key instanceof Integer) {
            index = (Integer) key;
        }
        else if (key instanceof Number) {
            index = ((Number) key).intValue();
        }
        else if (key instanceof String) {
            try { index = Integer.valueOf((String) key); }
            catch (NumberFormatException ex) {/* no need to handle directly */}
        }

        return index != null && index >= 0 && index < inner.size() ? index : null;
    }

    @Override
    public Dynamic get(Object key) {
        if (inner.isEmpty()) return new ParentAbsence.Empty<>(this, key);

        final Integer index = keyToIndex(key);
        if (index == null) return new ChildAbsence.Missing<>(this, key);

        final Object val = inner.get(index);
        return val != null ? DynamicChild.from(this, key, val) : new ChildAbsence.Null(this, key);
    }

    @Override
    public Stream<Dynamic> children() {
        return IntStream.range(0, inner.size()).mapToObj(this::get);
    }

    @Override
    public String describeType() {
        return "List";
    }

    @Override
    protected Object keyLiteral() {
        return ROOT_KEY;
    }

    @Override
    public String describeAvailability() {
        return inner.isEmpty() ? "[]" : format("[0..%d]", inner.size()-1);
    }

    @Override
    public String toString() {
        return keyLiteral() + ":" + describeType() + describeAvailability();
    }

    static class Child extends DynamicList implements DynamicChild {

        private final Dynamic parent;
        private final Object key;

        Child(Dynamic parent, Object key, List inner) {
            super(inner);
            this.parent = parent;
            this.key = key;
        }

        @Override
        public Dynamic parent() {
            return parent;
        }

        @Override
        public Object keyLiteral() {
            return key;
        }

        @Override
        public String toString() {
            return LiteJoiner.on(ARROW).join(using(this).getAscendingKeyChainWithRoot()) + ":" +
                describeType() + describeAvailability();
        }
    }
}
