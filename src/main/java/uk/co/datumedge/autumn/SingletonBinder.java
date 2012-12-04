package uk.co.datumedge.autumn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class SingletonBinder {
	@SuppressWarnings("unchecked")
	public static <MODULE> MODULE bind(final Class<MODULE> iface, final MODULE implementation) {
		InvocationHandler invocationHandler = new InvocationHandler() {
			private final Map<Method, Object> components = new HashMap<Method, Object>();

			{
				try {
					for (Method method : iface.getMethods()) {
						if (method.getParameterTypes().length != 0) {
							throw new BindException("Module " + iface.getCanonicalName() + " declares method " + method.getName() + "() with non-empty argument list");
						}
						
						components.put(method, method.invoke(implementation));
					}
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return components.get(method);
			}
		};
		
		return (MODULE) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{iface}, invocationHandler);
	}
}
