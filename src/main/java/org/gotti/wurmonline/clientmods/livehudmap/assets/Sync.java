package org.gotti.wurmonline.clientmods.livehudmap.assets;

import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public final class Sync {
    private Sync() {}
    
    private static final Set<String> LOCKED_KEYS = new HashSet<>();
    
    private static void lock(String key) throws InterruptedException {
        synchronized (LOCKED_KEYS) {
            while (!LOCKED_KEYS.add(key))
                LOCKED_KEYS.wait();
        }
    }
    private static void unlock(String key) {
        synchronized (LOCKED_KEYS) {
            LOCKED_KEYS.remove(key);
            LOCKED_KEYS.notifyAll();
        }
    }
    
    public static void run(String key, Runnable runnable) {
        try {
            Sync.lock(key);
            runnable.run();
        } catch (InterruptedException e) {
            LiveHudMapMod.log(e);
        } finally {
            Sync.unlock(key);
        }
    }
    public static <T> T run(String key, Callable<T> runnable) throws Exception {
        return Sync.runOr(key, runnable, null);
    }
    public static <T> T runOr(String key, Callable<T> runnable, T or) throws Exception {
        try {
            Sync.lock(key);
            return runnable.call();
        } catch (InterruptedException e) {
            LiveHudMapMod.log(e);
        } finally {
            Sync.unlock(key);
        }
        return or;
    }
}
