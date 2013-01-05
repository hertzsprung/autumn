package uk.co.datumedge.autumn;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;

public class SingletoniserTest {
	interface MyModule {
		Object myComponent();
		Object myParent();
		Object myChild();
	}

	static class TestMyModule implements MyModule {
		@Override public Object myComponent() {
			return new Object();
		}

		@Override public Object myParent() {
			return myChild();
		}

		@Override public Object myChild() {
			return new Object();
		}
	}

	@Test public void providesSameInstanceOnSuccessiveCalls() {
		MyModule myModule = Singletoniser.singletonise(MyModule.class, new TestMyModule());
		assertThat(myModule.myComponent(), both(notNullValue()).and(sameInstance(myModule.myComponent())));
	}

	@Test public void providesSameInstanceToMethodInSameModule() {
		MyModule myModule = Singletoniser.singletonise(MyModule.class, new TestMyModule());
		assertThat(myModule.myParent(), sameInstance(myModule.myChild()));
	}

	@Test(expected=SingletoniseException.class)
	public void failsToSingletoniseInterfaceHavingMethodWithNonEmptyArgumentList() {
		Singletoniser.singletonise(MyBrokenModuleWithParameter.class, new TestMyBrokenModuleWithParameter());
	}

	interface MyBrokenModuleWithParameter {
		Object myComponent(String parameter);
	}

	static class TestMyBrokenModuleWithParameter implements MyBrokenModuleWithParameter {
		@Override public Object myComponent(String parameter) {
			return new Object();
		}
	}

	@Test(expected=SingletoniseException.class)
	public void failsToSingletoniseImplementationHavingFinalMethod() {
		Singletoniser.singletonise(MyBrokenModuleWithFinalMethod.class, new TestMyBrokenModuleWithFinalMethod());
	}

	interface MyBrokenModuleWithFinalMethod {
		Object myComponent();
	}

	static class TestMyBrokenModuleWithFinalMethod implements MyBrokenModuleWithFinalMethod {
		@Override public final Object myComponent() {
			return new Object();
		}
	}

	@Test(expected=SingletoniseException.class)
	public void failsToSingletoniseInterfaceHavingMethodReturningVoid() {
		Singletoniser.singletonise(MyBrokenModuleWithVoidReturn.class, new TestMyBrokenModuleWithVoidReturn());
	}

	interface MyBrokenModuleWithVoidReturn {
		void myComponent();
	}

	static class TestMyBrokenModuleWithVoidReturn implements MyBrokenModuleWithVoidReturn {
		@Override public void myComponent() {
		}
	}
}
