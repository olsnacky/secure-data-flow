package com.qutifc.implementations.token.implementation.components;

import com.qutifc.interfaces.components.Token;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class TokenImpl implements Token {
	@MapsTo(FieldName = "n")
	private String name;
	@MapsTo(FieldName = "v")
	private String value;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return this.value;
	}
}
