package com.qou.h7se.sbr;

/**
 * Created by k0de9x on 9/13/2015.
 */
public class MutableVar<T> {

    public MutableVar() {
        this(null);
    }

    public MutableVar(T value) {
        this.value = value;
    }

    public T value;
}
