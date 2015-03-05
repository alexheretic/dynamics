package alexh.weak;

import alexh.LiteJoiner;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

public abstract class ChildAbsence<Parent extends Dynamic> extends AbstractAbsence<Parent> implements IssueDescribingChild {

    protected ChildAbsence(Parent parent, Object key) {
        super(parent, key);
    }

    abstract protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, LinkedList<Object> ascendingKeyChainBeforeSelf);

    @Override
    public final String describeIssue(List<Object> childKeys) {
        LinkedList<Object> keyChainUntilSelf = DynamicChildLogic.using(this).getAscendingKeyChainWithRoot();
        keyChainUntilSelf.removeLast();

        LinkedList<Object> fullKeyChain = new LinkedList<>(keyChainUntilSelf);
        fullKeyChain.addLast("*"+ key +"*");
        fullKeyChain.addAll(childKeys);
        return describeIssue(fullKeyChain, keyChainUntilSelf);
    }

    @Override
    public Dynamic get(Object key) {
        return new DescriptionDeferringAbsence(this, key);
    }

    @Override
    public Object asObject() {
        throw new NoSuchElementException(describeIssue(emptyList()));
    }

    public static class Null extends ChildAbsence<Dynamic> {

        public Null(Dynamic parent, Object key) {
            super(parent, key);
        }

        @Override
        protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, LinkedList<Object> ascendingKeyChainBeforeSelf) {
            return format("null '%s' premature end of path %s", key, LiteJoiner.on(ARROW).join(ascendingMarkedKeyChain));
        }
    }

    public static class Missing<P extends Dynamic & TypeDescriber & AvailabilityDescriber> extends ChildAbsence<P> {

        public Missing(P parent, Object key) {
            super(parent, key);
        }

        @Override
        protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, LinkedList<Object> ascendingKeyChainBeforeSelf) {
            return format("'%s' key is missing in path %s, available %s: %s%s", key,
                LiteJoiner.on(ARROW).join(ascendingMarkedKeyChain), LiteJoiner.on(ARROW).join(ascendingKeyChainBeforeSelf),
                parent.describeType(), parent.describeAvailability());
        }
    }
}