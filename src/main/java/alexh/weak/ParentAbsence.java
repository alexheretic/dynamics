package alexh.weak;

import alexh.LiteJoiner;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static alexh.weak.DynamicChildLogic.using;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

public abstract class ParentAbsence<Parent extends Dynamic> extends AbstractAbsence<Parent> implements IssueDescribingChild {

    public ParentAbsence(Parent parent, Object key) {
        super(parent, key);
    }

    protected abstract String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, Object parentKey);

    @Override
    public final String describeIssue(List<Object> childKeys) {
        LinkedList<Object> keyChain = using(this).getAscendingKeyChainWithRoot();
        keyChain.set(keyChain.size() - 2, "*" + keyChain.get(keyChain.size() - 2).toString() + "*");
        keyChain.addAll(childKeys);
        return describeIssue(keyChain, parent.key().asObject());
    }

    @Override
    public Dynamic get(Object key) {
        return new DescriptionDeferringAbsence(this, key);
    }

    @Override
    public Object asObject() {
        throw new NoSuchElementException(describeIssue(emptyList()));
    }

    public static class Empty<P extends Dynamic & TypeDescriber> extends ParentAbsence<P> {

        public Empty(P parent, Object key) {
            super(parent, key);
        }

        @Override
        protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, Object parentKey) {
            return format("Empty %s '%s' premature end of path %s", parent.describeType(), parentKey, LiteJoiner.on(ARROW).join(ascendingMarkedKeyChain));
        }
    }

    public static class Barren<P extends Dynamic & TypeDescriber> extends ParentAbsence<P> {

        public Barren(P parent, Object key) {
            super(parent, key);
        }

        @Override
        protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, Object parentKey) {
            return format("%s '%s' premature end of path %s", parent.describeType(),
                parentKey, LiteJoiner.on(ARROW).join(ascendingMarkedKeyChain));
        }
    }
}