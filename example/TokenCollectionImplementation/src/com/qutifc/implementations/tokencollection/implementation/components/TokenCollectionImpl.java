package com.qutifc.implementations.tokencollection.implementation.components;

import com.qutifc.interfaces.components.Token;
import com.qutifc.interfaces.components.TokenCollection;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class TokenCollectionImpl implements TokenCollection {
	@MapsTo(FieldName = "t")
	private Token[] tokens;
	@MapsTo(FieldName = "state")
	private int tokenPosition;
	@MapsTo(FieldName = "state")
	private int tokenCount;
	@MapsTo(FieldName = "state")
	private int collectionSize;

	@Override
	public void add(Token token) {
		// create collection if it doens't exist
		if (this.collectionSize == 0) {
			this.collectionSize = 100;
			this.tokens = new Token[this.collectionSize];
		}

		// if we need more space, rebuild array
		if (this.tokenCount == this.collectionSize) {
			this.collectionSize = this.collectionSize * 2;
			Token[] newTokens = new Token[this.collectionSize];
			int j = 0;
			while (j < this.tokenCount) {
				newTokens[j] = this.tokens[j];
				j = j + 1;
			}
			this.tokens = newTokens;
		}

		this.tokens[this.tokenCount] = token;
		this.tokenCount = this.tokenCount + 1;
	}

	@Override
	public Token getNext() {
		if (this.tokenPosition < this.tokenCount) {
			Token token = this.tokens[this.tokenPosition];
			this.tokenPosition = this.tokenPosition + 1;
			return token;
		}

		return null;
	}
}
