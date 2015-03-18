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

public abstract class AbstractDynamic<T> implements Dynamic {

    protected final T inner;

    public AbstractDynamic(T inner) {
        this.inner = inner;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public Object asObject() {
        return inner;
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }

    protected abstract Object keyLiteral();

    @Override
    public Dynamic key() {
        return DynamicChild.key(this, keyLiteral());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDynamic other = (AbstractDynamic) o;
        return inner.equals(other.inner);
    }
}
