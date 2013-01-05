package uk.co.datumedge.autumn;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

public class SingletonBinder {
	public static <MODULE> MODULE bind(final Class<MODULE> iface, final MODULE implementation) {
		checkAllMethodsTakeNoParameters(iface);

		MethodHandler methodHandler = new MethodHandler() {
			private final Map<Method, Object> components = new HashMap<Method, Object>();

			@Override public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
				synchronized (components) {
					if (!components.containsKey(thisMethod)) {
						components.put(thisMethod, proceed.invoke(self, args));
					}
					return components.get(thisMethod);
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

	private static <MODULE> MODULE createProxy(final MODULE implementation, MethodHandler methodHandler,
			MethodFilter methodFilter) {
		try {
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setSuperclass(implementation.getClass());
			proxyFactory.setFilter(methodFilter);
			Class<?> clazz = proxyFactory.createClass();
			@SuppressWarnings("unchecked") MODULE proxy = (MODULE) clazz.newInstance();
			((Proxy) proxy).setHandler(methodHandler);
			return proxy;
		} catch (InstantiationException e) {
			throw new BindException(e);
		} catch (IllegalAccessException e) {
			throw new BindException(e);
		}
	}

	private static <MODULE> void checkAllMethodsTakeNoParameters(final Class<MODULE> iface) {
		for (Method method : iface.getMethods()) {
			if (method.getParameterTypes().length != 0) {
				throw new BindException("Module " + iface.getCanonicalName() + " declares method " + method.getName() + "() with non-empty argument list");
			}
		}
	}

	private static boolean areSame(Method m1, Method m2) {
		return m1.getName().equals(m2.getName()) && Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes());
	}
}
