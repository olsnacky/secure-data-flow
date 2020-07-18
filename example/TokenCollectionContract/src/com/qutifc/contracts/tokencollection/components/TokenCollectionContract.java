package com.qutifc.contracts.tokencollection.components;

import com.qutifc.interfaces.components.Token;
import com.qutifc.interfaces.components.TokenCollection;

public class TokenCollectionContract implements TokenCollection {
	private Token[] t;
	private int state;

	@Override
	public void add(Token token) {
		this.state = 0;
		if (this.state > 0) {
			this.t = new Token[this.state];
		}
		this.t[this.state] = token;
		this.t[0] = this.t[0];
	}

	@Override
	public Token getNext() {
		this.state = 0;
		return this.t[this.state];
	}
}
