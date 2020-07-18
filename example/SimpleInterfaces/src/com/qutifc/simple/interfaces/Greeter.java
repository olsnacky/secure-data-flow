package com.qutifc.simple.interfaces;

import java.util.ServiceLoader;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName = "com.qutifc.simple.contracts.GreeterSC")
public interface Greeter {
	void setGreeting(String greeting);

	String getGreeting();

	static Greeter get() {
		return ServiceLoader.load(Greeter.class).findFirst().get();
	}
}