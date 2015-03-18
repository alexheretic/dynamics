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
import java.util.Map;
import java.util.stream.Stream;

import static alexh.weak.DynamicChildLogic.using;

class DynamicMap extends AbstractDynamic<Map<?, ?>> implements Dynamic, TypeDescriber, AvailabilityDescriber {

    public DynamicMap(Map<?, ?> inner) {
        super(inner);
    }

    @Override
    public Dynamic get(Object childKey) {
        if (inner.isEmpty()) return new ParentAbsence.Empty<>(this, childKey);
        if (!inner.containsKey(childKey)) {
            if (childKey instanceof String) {
                for (Map.Entry<?, ?> entry : inner.entrySet()) {
                    if (childKey.equals(entry.getKey().toString()))
                        return entry.getValue() != null ? DynamicChild.from(this, childKey, entry.getValue()) :
                            new ChildAbsence.Null(this, childKey);
                }
            }

            final String keyString = childKey.toString();
            if (inner.containsKey(keyString)) {
                final Object val = inner.get(keyString);
                return val != null ? DynamicChild.from(this, keyString, val) : new ChildAbsence.Null(this, keyString);
            }

            return new ChildAbsence.Missing<>(this, childKey);
        }
        final Object val = inner.get(childKey);
        return val != null ? DynamicChild.from(this, childKey, val) : new ChildAbsence.Null(this, childKey);
    }

    @Override
    public Stream<Dynamic> children() {
        return inner.keySet().stream().map(this::get);
    }

    @Override
    public String describeType() {
        return "Map";
    }

    @Override
    public String describeAvailability() {
        return inner.keySet().toString();
    }

    @Override
    public Object keyLiteral() {
        return ROOT_KEY;
    }

    @Override
    public String toString() {
        return keyLiteral() + ":"+ describeType() + describeAvailability();
    }

    static class Child extends DynamicMap implements DynamicChild {

        private final Dynamic parent;
        private final Object key;

        Child(Dynamic parent, Object key, Map inner) {
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
