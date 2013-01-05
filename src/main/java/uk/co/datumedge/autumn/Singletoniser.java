package uk.co.datumedge.autumn;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

/**
 * Proxies classes to provide singleton instances of their methods' return values.
 */
public final class Singletoniser {
	private Singletoniser() { }

	/**
	 * Creates a proxy that implements an interface and subclasses a concrete instance of that interface. The proxy
	 * returns the same return value on successive invocations to the same method.
	 *
	 * <pre>public class SingletoniserExample {
	public interface Module {
		double random();
		double anotherRandom();
	}

	public static class ModuleImplementation implements Module {
		{@literal @}Override public double random() {
			return Math.random();
		}

		{@literal @}Override public double anotherRandom() {
			return random();
		}
	}

	public static void main(String[] args) {
		Module module = Singletoniser.singletonise(Module.class, new ModuleImplementation());
		System.out.println(module.random());
		System.out.println(module.random());
		System.out.println(module.anotherRandom());
		// all invocations print the same value
	}
}</pre>
	 * <p>All interface methods must have no parameters and have a non-void return type.
	 * The methods implemented by the implementation must be non-final.
	 *
	 * <p>A singleton object is cached the first time the method is invoked.
	 * The method may be invoked from another object or from the proxied object itself.
	 * Access to the singleton cache is synchronised, hence objects returned by this method are thread-safe.
	 *
	 * @param iface the interface to proxy
	 * @param implementation a concrete instance of the interface
	 * @return the singletonised implementation
	 * @throws SingletoniseException
	 *             if the module could not be singletonised
	 */
	public static <T> T singletonise(final Class<T> iface, final T implementation) {
		checkAllMethodsHaveNonVoidReturnType(iface);
		checkAllMethodsTakeNoParameters(iface);
		checkAllImplementedMethodsAreNonFinal(iface, implementation);

		MethodHandler methodHandler = new MethodHandler() {
			private final Map<Method, Object> singletons = new HashMap<Method, Object>();

			@Override public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
				synchronized (singletons) {
					if (!singletons.containsKey(thisMethod)) {
						singletons.put(thisMethod, proceed.invoke(self, args));
					}
					return singletons.get(thisMethod);
				}
			}
		};

		MethodFilter methodFilter = new MethodFilter() {
			@Override public boolean isHandled(Method candidateMethod) {
				for (Method interfaceMethod : iface.getMethods()) {
					if (areSame(candidateMethod, interfaceMethod)) {
						return true;
					}
				}
				return false;
			}
		};

		return createProxy(implementation, methodHandler, methodFilter);
	}

	private static <T> T createProxy(T implementation, MethodHandler methodHandler, MethodFilter methodFilter) {
		try {
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setSuperclass(implementation.getClass());
			proxyFactory.setFilter(methodFilter);
			Class<?> clazz = proxyFactory.createClass();
			@SuppressWarnings("unchecked") T proxy = (T) clazz.newInstance();
			((Proxy) proxy).setHandler(methodHandler);
			return proxy;
		} catch (InstantiationException e) {
			throw new SingletoniseException(e);
		} catch (IllegalAccessException e) {
			throw new SingletoniseException(e);
		}
	}

	private static <T> void checkAllMethodsHaveNonVoidReturnType(Class<T> iface) {
		for (Method method : iface.getMethods()) {
			if (method.getReturnType() == void.class) {
				throw new SingletoniseException(iface.getCanonicalName() + " declares method " + method.getName() + "() with void return type");
			}
		}
	}

	private static <T> void checkAllMethodsTakeNoParameters(Class<T> iface) {
		for (Method method : iface.getMethods()) {
			if (method.getParameterTypes().length != 0) {
				throw new SingletoniseException(iface.getCanonicalName() + " declares method " + method.getName() + "() with non-empty argument list");
			}
		}
	}

	private static <T> void checkAllImplementedMethodsAreNonFinal(Class<T> iface, T implementation) {
		for (Method ifaceMethod : iface.getMethods()) {
			if (isFinal(implementation.getClass(), ifaceMethod)) {
				throw new SingletoniseException(implementation.getClass().getCanonicalName() + " declares method " + ifaceMethod.getName() + "() as final");
			}
		}
	}

	private static boolean isFinal(Class<?> type, Method method) {
		try {
			return Modifier.isFinal(type.getMethod(method.getName(), method.getParameterTypes()).getModifiers());
		} catch (NoSuchMethodException e) {
			throw new SingletoniseException(e);
		}
	}

	private static boolean areSame(Method m1, Method m2) {
		return m1.getName().equals(m2.getName()) && Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes());
	}
}
