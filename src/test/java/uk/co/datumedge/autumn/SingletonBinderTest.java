package uk.co.datumedge.autumn;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;

public class SingletonBinderTest {
	interface MyModule {
		Object myComponent();
	}
	
	static class TestMyModule implements MyModule {
		@Override public Object myComponent() {
			return new Object();
		}
	}
	
	@Test public void providesSameInstanceOnSuccessiveCalls() {
		MyModule myModule = SingletonBinder.bind(MyModule.class, new TestMyModule());
		assertThat(myModule.myComponent(), both(notNullValue()).and(sameInstance(myModule.myComponent())));
	}
}