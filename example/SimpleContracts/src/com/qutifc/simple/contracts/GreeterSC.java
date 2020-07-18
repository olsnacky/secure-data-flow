package com.qutifc.simple.contracts;

import com.qutifc.simple.interfaces.Greeter;

public class GreeterSC implements Greeter {
	private String g;

	@Override
	public void setGreeting(String greeting) {
		this.g = greeting;
	}

	@Override
	public String getGreeting() {
		return this.g;
	}
}