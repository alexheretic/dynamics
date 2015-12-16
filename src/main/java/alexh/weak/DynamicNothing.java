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

import java.util.NoSuchElementException;
import java.util.stream.Stream;

enum DynamicNothing implements Dynamic, Describer {
    INSTANCE;

    @Override
    public Dynamic get(Object key) {
        return new ParentAbsence.Barren<>(this, key);
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public Object asObject() {
        throw new NoSuchElementException("null 'root' premature end of path *root*");
    }

    @Override
    public Stream<Dynamic> children() {
        return Stream.empty();
    }

    @Override
    public Dynamic key() {
        return DynamicChild.key(this, ROOT_KEY);
    }

    @Override
    public String describe() {
        return "null";
    }

    @Override
    public String toString() {
        return ROOT_KEY + ":" + describe();
    }
}
