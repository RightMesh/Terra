package io.left.rightmesh.librxbus;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The RxBus is a simple event bus built over RxJava.
 *
 * @author Lucien Loiseau on 21/06/18.
 */
public class RxBus {
    private static final Object lock = new Object();
    private static RxBus instance = null;

    private Subject<Object> bus;
    private Map<Object, Map<String, Disposable>> subscriptions;

    private static RxBus getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new RxBus();
            }
            return instance;
        }
    }

    private RxBus() {
        bus = PublishSubject.create().toSerialized();
        subscriptions = new HashMap<>();
    }

    /**
     * Post a new event to the bus. Do consider that this call might be blocking until all consumer
     * consuming in the RxThread.POSTING thread has finished. (which is default).
     *
     * @param object event
     */
    public static void post(Object object) {
        synchronized (lock) {
            getInstance().bus.onNext(object);
        }
    }

    /**
     * filter a specific event.
     *
     * @param clazz event to filter
     * @return Observable stream of filtered events
     */
    public static Observable<?> filter(Class<?> clazz) {
        return getInstance().bus.ofType(clazz);
    }

    /**
     * register an Object to this bus.
     *
     * @param object to register
     */
    public static void register(Object object) {
        getInstance().registerObject(object);
    }

    private void registerObject(Object object) {
        synchronized (this) {
            if (getInstance().subscriptions.containsKey(object)) {
                return;
            }
            Map<String, Disposable> disposables = new HashMap<>();
            for (Method method : getAllMethods(object)) {
                if (hasSubscribeAnnotation(method)
                        && isAccessModifierPublic(method)
                        && isReturnTypeVoid(method)
                        && hasSingleParameter(method)) {
                    String key = method.getName() + "_" + method.getParameterTypes()[0].toString();
                    Class<?> clazz = method.getParameterTypes()[0];
                    if (!disposables.containsKey(key)) {
                        switch (method.getAnnotation(Subscribe.class).thread()) {
                            case MAIN:
                                // todo
                                break;
                            case IO:
                                disposables.put(key,
                                        filter(clazz)
                                                .observeOn(Schedulers.io())
                                                .subscribe(e -> method.invoke(object, e)));
                                break;
                            case COMPUTATION:
                                disposables.put(key,
                                        filter(clazz)
                                                .observeOn(Schedulers.computation())
                                                .subscribe(e -> method.invoke(object, e)));
                                break;
                            case NEW:
                                disposables.put(key,
                                        filter(clazz)
                                                .observeOn(Schedulers.newThread())
                                                .subscribe(e -> method.invoke(object, e)));
                                break;
                            case TRAMPOLINE:
                                disposables.put(key,
                                        filter(clazz)
                                                .observeOn(Schedulers.trampoline())
                                                .subscribe(e -> method.invoke(object, e)));
                                break;
                            default: // POSTING
                                disposables.put(key,
                                        filter(clazz)
                                                .subscribe(e -> method.invoke(object, e)));
                                break;
                        }
                    }
                }
            }
            if (disposables.size() > 0) {
                subscriptions.put(object, disposables);
            }
        }
    }

    /**
     * unregister an Object from this bus.
     *
     * @param object to unregister
     */
    public static void unregister(Object object) {
        getInstance().unregisterObject(object);
    }

    private void unregisterObject(Object object) {
        synchronized (this) {
            if (!getInstance().subscriptions.containsKey(object)) {
                return;
            }
            Map<String, Disposable> entries = subscriptions.get(object);
            for (Disposable disposable : entries.values()) {
                disposable.dispose();
            }
            entries.clear();
            subscriptions.remove(object);
        }
    }

    private List<Method> getAllMethods(Object object) {
        Set<Class<?>> classes = new HashSet<>();
        List<Class<?>> parents = new LinkedList<>();
        parents.add(object.getClass());
        while (!parents.isEmpty()) {
            Class<?> clazz = parents.remove(0);
            classes.add(clazz);

            Class<?> parentClass = clazz.getSuperclass();
            if (parentClass != null
                    && !parentClass.getName().startsWith("java.")
                    && !parentClass.getName().startsWith("javax.")
                    && !parentClass.getName().startsWith("android.")) {
                parents.add(parentClass);
            }
        }
        List<Method> ret = new LinkedList<>();
        for (Class<?> clazz : classes) {
            ret.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        }
        return ret;
    }

    private boolean hasSubscribeAnnotation(Method method) {
        Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
        return subscribeAnnotation != null;
    }

    private boolean isAccessModifierPublic(Method method) {
        return (method.getModifiers() & Modifier.PUBLIC) != 0;
    }

    private boolean isReturnTypeVoid(Method method) {
        return (method.getReturnType().equals(Void.TYPE));
    }

    private boolean hasSingleParameter(Method method) {
        return method.getParameterTypes().length == 1;
    }


}

