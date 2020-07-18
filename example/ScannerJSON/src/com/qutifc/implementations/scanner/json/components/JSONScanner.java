package com.qutifc.implementations.scanner.json.components;

import com.qutifc.interfaces.components.Scanner;
import com.qutifc.interfaces.components.Token;
import com.qutifc.interfaces.components.TokenCollection;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class JSONScanner implements Scanner {
	@MapsTo(FieldName = "t")
	private String text;
	@MapsTo(FieldName = "tc")
	private TokenCollection tokens;

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public TokenCollection getTokens() {
		return this.tokens;
	}

	@Override
	public void scan() {
		String text = this.text;
		this.tokens = TokenCollection.get();
		char[] chars = text.toCharArray();

		int i = 0;
		while (i < text.lastIndexOf("")) {
			char character = chars[i];
			if (character != ' ') {
				Token token = Token.get();

				if (character == '{') {
					token.setName("OpenObject");
				} else if (character == '}') {
					token.setName("CloseObject");
				} else if (character == '[') {
					token.setName("OpenArray");
				} else if (character == ']') {
					token.setName("CloseArray");
				} else if (character == ':') {
					token.setName("KeyValSep");
				} else if (character == ',') {
					token.setName("Separator");
				} else {
					// assume == '"'
					token.setName("String");
					String value = "";

					i = i + 1;
					character = chars[i];
					while (character != '"') {
						value = value + character;

						i = i + 1;
						character = chars[i];
					}

					token.setValue(value);
				}

				this.tokens.add(token);
			}
			i = i + 1;
		}
	}

	@Override
	public String type() {
		return "json";
	}
}
