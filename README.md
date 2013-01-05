A microscopically small alternative to Spring Framework that creates singleton objects.
The library is released under the [MIT license](http://www.opensource.org/licenses/mit-license.php).

Installation
============

Installing from Maven Central
-----------------------------
    <dependency>
    	<groupId>uk.co.datumedge</groupId>
    	<artifactId>autumn</artifactId>
    	<version>1.0</version>
    </dependency>


Installing from source
-----------------------------
    mvn install

Getting started
===============

```java
public interface Module {
	double random();
	double anotherRandom();
}

public static class ModuleImplementation implements Module {
	@Override public double random() {
		return Math.random();
	}

	@Override public double anotherRandom() {
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
```
    
Resources
=========
 * [autumn website](http://datumedge.co.uk/autumn/)
 * [API documentation](http://datumedge.co.uk/autumn/apidocs/index.html)
