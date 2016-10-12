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

import static alexh.weak.DynamicChildLogic.using;
import static java.lang.String.format;
import alexh.LiteJoiner;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class DynamicList extends AbstractDynamic<List> implements Dynamic, Describer {

    public DynamicList(List inner) {
        super(inner);
    }

    @Override
    public Dynamic get(Object key) {
        if (inner.isEmpty()) return new ParentAbsence.Empty<>(this, key);

        Integer index = Optional.ofNullable(key)
            .flatMap(k -> Converter.convert(k).maybe().intoInteger())
            .orElse(null);
        if (index == null) return new ChildAbsence.Missing<>(this, key);

        if (index < 0 || index >= inner.size()) return new ChildAbsence.Missing<>(this, index);

        final Object val = inner.get(index);
        return val != null ? DynamicChild.from(this, index, val) : new ChildAbsence.Null(this, index);
    }

    @Override
    public Stream<Dynamic> children() {
        return IntStream.range(0, inner.size()).mapToObj(this::get);
    }

    @Override
    public String describe() {
        final String type = "List";
        switch(inner.size()) {
            case 0: return "Empty-" + type;
            case 1: return type + "[0]";
            case 2: return type + "[0, 1]";
            default: return format("%s[0..%d]", type, inner.size()-1);
        }
    }

    @Override
    protected Object keyLiteral() {
        return ROOT_KEY;
    }

    @Override
    public String toString() {
        return keyLiteral() + ":" + describe();
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
            return LiteJoiner.on(ARROW).join(using(this).getAscendingKeyChainWithRoot()) + ":" + describe();
        }
    }
}
