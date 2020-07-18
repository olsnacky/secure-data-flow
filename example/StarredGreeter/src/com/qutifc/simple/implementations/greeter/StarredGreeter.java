package com.qutifc.simple.implementations.greeter;

import com.qutifc.securitycontracts.annotations.MapsTo;
import com.qutifc.simple.interfaces.Greeter;

public class StarredGreeter implements Greeter {
	@MapsTo(FieldName = "g")
	private String greeting;

	@Override
	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	@Override
	public String getGreeting() {
		return "*" + this.greeting + "*";
	}
}