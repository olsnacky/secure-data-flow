package com.qutifc.contracts.token.components;

import com.qutifc.interfaces.components.Token;

public class TokenContract implements Token {
	private String n;
	private String v;

	@Override
	public void setName(String name) {
		this.n = name;
	}

	@Override
	public String getName() {
		return this.n;
	}

	@Override
	public void setValue(String value) {
		this.v = value;
	}

	@Override
	public String getValue() {
		return this.v;
	}

}
