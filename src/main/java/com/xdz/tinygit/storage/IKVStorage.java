package com.xdz.tinygit.storage;

public interface IKVStorage<K, V> {
    V load(K key);

    void store(K key, V value);

    boolean remove(K key);
}
