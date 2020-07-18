package com.qutifc.interfaces.components;

import java.util.ServiceLoader;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName="com.qutifc.contracts.token.components.TokenContract")
public interface Token {
	void setName(String name);
	String getName();
	void setValue(String value);
	String getValue();
	
	static Token get() {
		return ServiceLoader.load(Token.class).findFirst().get();
	}
}
