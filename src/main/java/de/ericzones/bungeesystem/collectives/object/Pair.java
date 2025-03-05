// Created by Eric B. 02.02.2021 16:04
package de.ericzones.bungeesystem.collectives.object;

public class Pair<Key, Value> {
    protected Key firstObject;
    protected Value secondObject;

    public Pair(Key firstObject, Value secondObject) {
        this.firstObject = firstObject;
        this.secondObject = secondObject;
    }

    public Pair() {
    }

    public Key getFirstObject() {
        return this.firstObject;
    }

    public Value getSecondObject() {
        return this.secondObject;
    }

    public void setFirstObject(Key firstObject) {
        this.firstObject = firstObject;
    }

    public void setSecondObject(Value secondObject) {
        this.secondObject = secondObject;
    }

    public String toString() {
        return "Pair(firstObject="+this.firstObject+", secondObject="+this.secondObject+")";
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (!(object instanceof Pair)) {
            return false;
        } else {
            Pair<?, ?> other = (Pair)object;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$first = this.getFirstObject();
                Object other$first = other.getFirstObject();
                if (this$first == null) {
                    if (other$first != null) {
                        return false;
                    }
                } else if (!this$first.equals(other$first)) {
                    return false;
                }

                Object this$second = this.getSecondObject();
                Object other$second = other.getSecondObject();
                if (this$second == null) {
                    if (other$second != null) {
                        return false;
                    }
                } else if (!this$second.equals(other$second)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object object) {
        return object instanceof Pair;
    }

    public int hashCode() {
        boolean PRIME = true;
        int result = 1;
        Object $first = this.getFirstObject();
        result = result * 59 + ($first == null ? 43 : $first.hashCode());
        Object $second = this.getSecondObject();
        result = result * 59 + ($second == null ? 43 : $second.hashCode());
        return result;
    }

}
