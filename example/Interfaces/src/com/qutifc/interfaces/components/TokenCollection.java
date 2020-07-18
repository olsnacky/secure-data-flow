package com.qutifc.interfaces.components;

import java.util.ServiceLoader;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName="com.qutifc.contracts.tokencollection.components.TokenCollectionContract")
public interface TokenCollection {
	void add(Token token);
	Token getNext();
	
	static TokenCollection get() {
		return ServiceLoader.load(TokenCollection.class).findFirst().get();
	}
}
